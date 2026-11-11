package br.com.fiap.bo;

import br.com.fiap.dao.AgendamentoDAO;
import br.com.fiap.dao.DisponibilidadeDAO;
import br.com.fiap.dao.HistoricoEventoDAO;
import br.com.fiap.dao.HorarioAtendimentoDAO;
import br.com.fiap.dao.MembroEquipeDAO;
import br.com.fiap.dao.PropostaAtendimentoDAO;
import br.com.fiap.dao.SolicitacaoAjudaDAO;
import br.com.fiap.exception.AcessoNegadoException;
import br.com.fiap.exception.RegraNegocioException;
import br.com.fiap.to.AgendamentoTO;
import br.com.fiap.to.DisponibilidadeTO;
import br.com.fiap.to.HistoricoEventoTO;
import br.com.fiap.to.HorarioAtendimentoTO;
import br.com.fiap.to.MembroEquipeTO;
import br.com.fiap.to.PropostaAtendimentoTO;
import br.com.fiap.to.SolicitacaoAjudaTO;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PropostaAtendimentoBO {
    private static final ZoneId ZONA_NEGOCIO = ZoneId.of("America/Sao_Paulo");
    private static final List<String> ORDEM_ALTERNATIVAS = List.of("MANUTENCAO", "REMARCACAO", "CONVERSAO",
            "REMARCACAO_CONVERSAO");

    private PropostaAtendimentoDAO propostaDAO = new PropostaAtendimentoDAO();
    private SolicitacaoAjudaDAO solicitacaoDAO = new SolicitacaoAjudaDAO();
    private AgendamentoDAO agendamentoDAO = new AgendamentoDAO();
    private HorarioAtendimentoDAO horarioDAO = new HorarioAtendimentoDAO();
    private DisponibilidadeDAO disponibilidadeDAO = new DisponibilidadeDAO();
    private MembroEquipeDAO membroEquipeDAO = new MembroEquipeDAO();
    private HistoricoEventoDAO historicoDAO = new HistoricoEventoDAO();
    private MembroEquipeBO membroEquipeBO = new MembroEquipeBO();

    public ArrayList<PropostaAtendimentoTO> listarTodos() {
        return propostaDAO.listarTodos();
    }

    public PropostaAtendimentoTO buscarPorId(Long idProposta) {
        return idValido(idProposta) ? propostaDAO.buscarPorId(idProposta) : null;
    }

    public ArrayList<PropostaAtendimentoTO> listarPorSolicitacao(Long idSolicitacao, Long idPacienteAutenticado) {
        SolicitacaoAjudaTO solicitacao = idValido(idSolicitacao) ? solicitacaoDAO.buscarPorId(idSolicitacao) : null;
        if (solicitacao == null) {
            return new ArrayList<>();
        }
        AgendamentoTO agendamento = agendamentoDAO.buscarPorId(solicitacao.getIdAgendamento());
        if (agendamento == null) {
            return new ArrayList<>();
        }
        exigirTitular(agendamento, idPacienteAutenticado);
        return TransacaoJdbc.executar(conexao -> {
            ArrayList<PropostaAtendimentoTO> propostas = propostaDAO.listarPorSolicitacao(conexao, idSolicitacao);
            if (propostas != null) {
                propostas.removeIf(p -> "EM_ANALISE".equals(p.getStatusProposta()) || p.getDataInicioReserva() == null);
            }
            return propostas;
        });
    }

    public ArrayList<PropostaAtendimentoTO> gerarPropostas(Long idSolicitacao, Long idAnalista) {
        exigirApoio(idAnalista);
        return operarSolicitacao(idSolicitacao, (conexao, solicitacao, agendamento) -> {
            LocalDateTime instante = agora();
            HorarioAtendimentoTO original = verificarAnalise(solicitacao, agendamento, instante);
            if (original == null) {
                return null;
            }
            ArrayList<DisponibilidadeTO> disponibilidades = disponibilidadeDAO.listarPorSolicitacao(conexao,
                    idSolicitacao);
            if (disponibilidades == null) {
                return null;
            }
            ArrayList<PropostaAtendimentoTO> novas = new ArrayList<>();
            if (disponibilidades.isEmpty()) {
                return novas;
            }
            ArrayList<HorarioAtendimentoTO> candidatos = horarioDAO.listarCandidatos(conexao, original.getIdUnidade(),
                    instante, agendamento.getIdAgendamento());
            ArrayList<PropostaAtendimentoTO> existentes = propostaDAO.listarPorSolicitacao(conexao, idSolicitacao);
            if (candidatos == null || existentes == null) {
                return null;
            }

            for (HorarioAtendimentoTO candidato : candidatos) {
                if (!original.getIdMembroEquipe().equals(candidato.getIdMembroEquipe())) {
                    continue;
                }
                Boolean reservado = propostaDAO.existeReservaAtivaPorHorario(conexao, candidato.getIdHorario(),
                        instante, null);
                if (reservado == null) {
                    return null;
                }
                if (reservado) {
                    continue;
                }
                for (String modalidade : List.of("PRESENCIAL", "VIRTUAL")) {
                    if (pacienteDisponivel(disponibilidades, candidato, modalidade)
                            && !jaTemAlternativa(existentes, candidato.getIdHorario(), modalidade, instante)) {
                        novas.add(criarProposta(solicitacao, agendamento, candidato, modalidade, idAnalista, instante));
                    }
                }
            }

            // A busca no DAO já ordena horários por início. A ordenação estável prioriza o tipo de alternativa.
            novas.sort(Comparator.comparingInt(p -> ORDEM_ALTERNATIVAS.indexOf(p.getTipoAlternativa())));
            for (PropostaAtendimentoTO proposta : novas) {
                if (propostaDAO.inserir(conexao, proposta) == null || !registrarHistorico(conexao, agendamento,
                        proposta, "PROPOSTA_CRIADA", proposta.getJustificativa(), "EQUIPE", idAnalista)) {
                    return null;
                }
            }
            return novas;
        });
    }

    public PropostaAtendimentoTO atualizar(PropostaAtendimentoTO dados, Long idAnalista) {
        exigirApoio(idAnalista);
        if (dados == null || !idValido(dados.getIdProposta())) {
            return null;
        }
        String justificativa = dados.getJustificativa();
        if (justificativa == null || justificativa.isBlank() || justificativa.length() > 500) {
            throw new RegraNegocioException("A justificativa deve ter de 1 a 500 caracteres.");
        }
        return operarProposta(dados.getIdProposta(), (conexao, proposta, solicitacao, agendamento) -> {
            exigirEstado(proposta, "EM_ANALISE");
            if (verificarAnalise(solicitacao, agendamento, agora()) == null) {
                return null;
            }
            proposta.setJustificativa(justificativa);
            return propostaDAO.atualizar(conexao, proposta);
        });
    }

    public PropostaAtendimentoTO autorizar(Long idProposta, Long idAutorizador, boolean autorizada) {
        if (!membroEquipeBO.podeAutorizarModalidade(idAutorizador)) {
            throw new AcessoNegadoException("A operação exige atribuição de autorização de modalidade.");
        }
        return operarProposta(idProposta, (conexao, proposta, solicitacao, agendamento) -> {
            exigirEstado(proposta, "EM_ANALISE");
            LocalDateTime instante = agora();
            if (verificarAnalise(solicitacao, agendamento, instante) == null) {
                return null;
            }
            if (proposta.getModalidade().equals(agendamento.getModalidade())
                    || !"PENDENTE".equals(proposta.getStatusAutorizacao())) {
                throw new RegraNegocioException("A proposta não tem uma conversão com autorização pendente.");
            }
            proposta.setStatusAutorizacao(autorizada ? "AUTORIZADA" : "NEGADA");
            proposta.setDataAutorizacao(instante);
            proposta.setIdAutorizador(idAutorizador);
            if (!autorizada) {
                proposta.setStatusProposta("CANCELADA");
            }
            return salvarComHistorico(conexao, agendamento, proposta, "AUTORIZACAO_PROPOSTA",
                    "Autorização de modalidade: " + proposta.getStatusAutorizacao(), "EQUIPE", idAutorizador);
        });
    }

    public PropostaAtendimentoTO ofertar(Long idProposta, Long idAnalista, LocalDateTime dataLimiteAceite) {
        exigirApoio(idAnalista);
        if (dataLimiteAceite == null) {
            throw new RegraNegocioException("Informe o prazo de aceite da oferta.");
        }
        LocalDateTime limite = dataLimiteAceite.withNano(0);
        return operarProposta(idProposta, (conexao, proposta, solicitacao, agendamento) -> {
            exigirEstado(proposta, "EM_ANALISE");
            HorarioAtendimentoTO original = verificarAnalise(solicitacao, agendamento, agora());
            if (original == null) {
                return null;
            }
            exigirAutorizacao(proposta, agendamento);
            HorarioAtendimentoTO candidato = validarCandidato(conexao, proposta, agendamento, original);
            if (candidato == null) {
                return null;
            }
            LocalDateTime instante = agora();
            if (!limite.isAfter(instante) || limite.isAfter(original.getDataInicio())
                    || limite.isAfter(candidato.getDataInicio())) {
                throw new RegraNegocioException("O prazo de aceite deve ser futuro e terminar até o início dos horários.");
            }
            proposta.setStatusProposta("OFERTADA");
            proposta.setDataInicioReserva(instante);
            proposta.setDataFimReserva(limite);
            proposta.setDataLimiteAceite(limite);
            return salvarComHistorico(conexao, agendamento, proposta, "PROPOSTA_OFERTADA",
                    "Proposta ofertada com reserva até " + limite, "EQUIPE", idAnalista);
        });
    }

    public PropostaAtendimentoTO registrarResposta(Long idProposta, Long idPacienteAutenticado, boolean aceita) {
        return operarProposta(idProposta, (conexao, proposta, solicitacao, agendamento) -> {
            exigirTitular(agendamento, idPacienteAutenticado);
            LocalDateTime instante = agora();
            exigirOfertaVigente(proposta, instante);
            if (verificarAnalise(solicitacao, agendamento, instante) == null) {
                return null;
            }
            if (!"PENDENTE".equals(proposta.getStatusAceitePaciente())) {
                throw new RegraNegocioException("A proposta já recebeu uma resposta do paciente.");
            }
            proposta.setStatusAceitePaciente(aceita ? "ACEITA" : "RECUSADA");
            proposta.setDataResposta(instante);
            if (!aceita) {
                proposta.setStatusProposta("CANCELADA");
            }
            return salvarComHistorico(conexao, agendamento, proposta, "RESPOSTA_PROPOSTA",
                    "Resposta do paciente: " + proposta.getStatusAceitePaciente(), "PACIENTE", null);
        });
    }

    public PropostaAtendimentoTO confirmar(Long idProposta, Long idMembroEquipe) {
        exigirApoio(idMembroEquipe);
        return operarProposta(idProposta, (conexao, proposta, solicitacao, agendamento) -> {
            LocalDateTime instante = agora();
            exigirOfertaVigente(proposta, instante);
            if (!"ACEITA".equals(proposta.getStatusAceitePaciente()) || proposta.getDataResposta() == null) {
                throw new RegraNegocioException("A confirmação exige aceite do paciente.");
            }
            HorarioAtendimentoTO original = verificarAnalise(solicitacao, agendamento, instante);
            if (original == null) {
                return null;
            }
            exigirAutorizacao(proposta, agendamento);
            HorarioAtendimentoTO candidato = validarCandidato(conexao, proposta, agendamento, original);
            if (candidato == null) {
                return null;
            }

            // A aquisição do bloqueio pode levar tempo. Confere os limites novamente antes de gravar.
            instante = agora();
            exigirOfertaVigente(proposta, instante);
            if (!original.getDataInicio().isAfter(instante) || !candidato.getDataInicio().isAfter(instante)) {
                throw new RegraNegocioException("O horário original ou proposto já começou.");
            }
            return concluirConfirmacao(conexao, proposta, solicitacao, agendamento, instante, idMembroEquipe);
        });
    }

    public PropostaAtendimentoTO expirar(Long idProposta, Long idMembroEquipe) {
        exigirApoio(idMembroEquipe);
        return operarProposta(idProposta, (conexao, proposta, solicitacao, agendamento) -> {
            exigirEstado(proposta, "OFERTADA");
            if (!prazoEncerrado(proposta, agora())) {
                throw new RegraNegocioException("O prazo da oferta ainda está vigente.");
            }
            proposta.setStatusProposta("EXPIRADA");
            return salvarComHistorico(conexao, agendamento, proposta, "PROPOSTA_EXPIRADA",
                    "Prazo da oferta encerrado.", "EQUIPE", idMembroEquipe);
        });
    }

    public boolean excluir(Long idProposta, Long idMembroEquipe) {
        exigirApoio(idMembroEquipe);
        return operarProposta(idProposta, (conexao, proposta, solicitacao, agendamento) -> {
            if (!"EM_ANALISE".equals(proposta.getStatusProposta()) && !"OFERTADA".equals(proposta.getStatusProposta())) {
                throw new RegraNegocioException("A proposta já está encerrada.");
            }
            proposta.setStatusProposta("CANCELADA");
            return salvarComHistorico(conexao, agendamento, proposta, "PROPOSTA_CANCELADA",
                    "Proposta cancelada pela equipe.", "EQUIPE", idMembroEquipe);
        }) != null;
    }

    private PropostaAtendimentoTO concluirConfirmacao(Connection conexao, PropostaAtendimentoTO proposta,
                                                       SolicitacaoAjudaTO solicitacao, AgendamentoTO agendamento,
                                                       LocalDateTime instante, Long idMembroEquipe) {
        ArrayList<PropostaAtendimentoTO> alternativas = propostaDAO.listarPorSolicitacao(conexao,
                solicitacao.getIdSolicitacao());
        if (alternativas == null) {
            return null;
        }
        Long idHorarioOriginal = agendamento.getIdHorario();
        String modalidadeOriginal = agendamento.getModalidade();
        agendamento.setIdHorario(proposta.getIdHorario());
        agendamento.setModalidade(proposta.getModalidade());
        agendamento.setStatusRespostaPaciente("VOU_COMPARECER");
        proposta.setStatusProposta("CONFIRMADA");
        proposta.setDataConfirmacao(instante);
        solicitacao.setStatusSolicitacao("RESOLVIDA");
        solicitacao.setDataEncerramento(instante);

        if (agendamentoDAO.atualizar(conexao, agendamento) == null || propostaDAO.atualizar(conexao, proposta) == null
                || solicitacaoDAO.atualizar(conexao, solicitacao) == null) {
            return null;
        }
        for (PropostaAtendimentoTO alternativa : alternativas) {
            if (!proposta.getIdProposta().equals(alternativa.getIdProposta())
                    && ("EM_ANALISE".equals(alternativa.getStatusProposta())
                    || "OFERTADA".equals(alternativa.getStatusProposta()))) {
                alternativa.setStatusProposta("CANCELADA");
                if (propostaDAO.atualizar(conexao, alternativa) == null) {
                    return null;
                }
            }
        }
        String descricao = "Horário " + idHorarioOriginal + " (" + modalidadeOriginal + ") confirmado como horário "
                + proposta.getIdHorario() + " (" + proposta.getModalidade() + ").";
        return registrarHistorico(conexao, agendamento, proposta, "RESGATE_CONFIRMADO", descricao, "EQUIPE",
                idMembroEquipe) ? proposta : null;
    }

    private HorarioAtendimentoTO validarCandidato(Connection conexao, PropostaAtendimentoTO proposta,
                                                   AgendamentoTO agendamento, HorarioAtendimentoTO original) {
        HorarioAtendimentoTO candidato = horarioDAO.buscarPorIdParaAtualizar(conexao, proposta.getIdHorario());
        if (candidato == null) {
            return null;
        }
        MembroEquipeTO profissional = membroEquipeDAO.buscarPorIdParaAtualizar(conexao, candidato.getIdMembroEquipe());
        if (profissional == null) {
            return null;
        }
        LocalDateTime instante = agora();
        if (!candidato.getDataInicio().isAfter(instante) || !"S".equals(profissional.getAtendimento())
                || !original.getIdUnidade().equals(candidato.getIdUnidade())
                || !original.getIdMembroEquipe().equals(candidato.getIdMembroEquipe())
                || !tipoAlternativa(agendamento, candidato, proposta.getModalidade()).equals(proposta.getTipoAlternativa())) {
            throw new RegraNegocioException("O horário proposto não é mais compatível com o agendamento original.");
        }
        ArrayList<DisponibilidadeTO> disponibilidades = disponibilidadeDAO.listarPorSolicitacao(conexao,
                proposta.getIdSolicitacao());
        if (disponibilidades == null) {
            return null;
        }
        if (!pacienteDisponivel(disponibilidades, candidato, proposta.getModalidade())) {
            throw new RegraNegocioException("O horário proposto não atende à disponibilidade do paciente.");
        }
        if (agendamentoDAO.existeAgendamentoAtivoPorHorario(conexao, candidato.getIdHorario(),
                agendamento.getIdAgendamento())) {
            throw new RegraNegocioException("O horário proposto já está ocupado.");
        }
        Boolean reserva = propostaDAO.existeReservaAtivaPorHorario(conexao, candidato.getIdHorario(), instante,
                proposta.getIdProposta());
        if (Boolean.TRUE.equals(reserva)) {
            throw new RegraNegocioException("O horário está reservado para outra proposta.");
        }
        return reserva == null ? null : candidato;
    }

    private HorarioAtendimentoTO verificarAnalise(SolicitacaoAjudaTO solicitacao, AgendamentoTO agendamento,
                                                  LocalDateTime instante) {
        if (!"EM_ANALISE".equals(solicitacao.getStatusSolicitacao()) || !"AGENDADO".equals(agendamento.getStatusAgendamento())) {
            throw new RegraNegocioException("A operação exige solicitação em análise e agendamento ativo.");
        }
        HorarioAtendimentoTO original = horarioDAO.buscarPorId(agendamento.getIdHorario());
        if (original != null && !original.getDataInicio().isAfter(instante)) {
            throw new RegraNegocioException("O agendamento original já começou.");
        }
        return original;
    }

    private PropostaAtendimentoTO criarProposta(SolicitacaoAjudaTO solicitacao, AgendamentoTO agendamento,
                                                HorarioAtendimentoTO horario, String modalidade, Long idAnalista,
                                                LocalDateTime instante) {
        String tipo = tipoAlternativa(agendamento, horario, modalidade);
        String autorizacao = modalidade.equals(agendamento.getModalidade()) ? "NAO_APLICAVEL" : "PENDENTE";
        return new PropostaAtendimentoTO(null, tipo, modalidade,
                "Horário compatível com a disponibilidade declarada pelo paciente.", instante,
                "EM_ANALISE", autorizacao, null, "PENDENTE", null, null, null, null, null,
                solicitacao.getIdSolicitacao(), horario.getIdHorario(), idAnalista, null);
    }

    private String tipoAlternativa(AgendamentoTO agendamento, HorarioAtendimentoTO horario, String modalidade) {
        boolean mudaHorario = !agendamento.getIdHorario().equals(horario.getIdHorario());
        if (!modalidade.equals(agendamento.getModalidade())) {
            return mudaHorario ? "REMARCACAO_CONVERSAO" : "CONVERSAO";
        }
        return mudaHorario ? "REMARCACAO" : "MANUTENCAO";
    }

    private boolean pacienteDisponivel(List<DisponibilidadeTO> disponibilidades, HorarioAtendimentoTO horario,
                                        String modalidade) {
        for (DisponibilidadeTO disponibilidade : disponibilidades) {
            if ((modalidade.equals(disponibilidade.getModalidade()) || "AMBAS".equals(disponibilidade.getModalidade()))
                    && !horario.getDataInicio().isBefore(disponibilidade.getDataInicio())
                    && !horario.getDataFim().isAfter(disponibilidade.getDataFim())) {
                return true;
            }
        }
        return false;
    }

    private boolean jaTemAlternativa(List<PropostaAtendimentoTO> propostas, Long idHorario, String modalidade,
                                     LocalDateTime instante) {
        for (PropostaAtendimentoTO proposta : propostas) {
            if (idHorario.equals(proposta.getIdHorario()) && modalidade.equals(proposta.getModalidade())
                    && ("EM_ANALISE".equals(proposta.getStatusProposta())
                    || ("OFERTADA".equals(proposta.getStatusProposta()) && !prazoEncerrado(proposta, instante)))) {
                return true;
            }
        }
        return false;
    }

    private void exigirAutorizacao(PropostaAtendimentoTO proposta, AgendamentoTO agendamento) {
        if (proposta.getModalidade().equals(agendamento.getModalidade())) {
            if (!"NAO_APLICAVEL".equals(proposta.getStatusAutorizacao())) {
                throw new RegraNegocioException("A modalidade da proposta não corresponde à autorização registrada.");
            }
        } else if (!"AUTORIZADA".equals(proposta.getStatusAutorizacao()) || proposta.getDataAutorizacao() == null
                || !membroEquipeBO.podeAutorizarModalidade(proposta.getIdAutorizador())) {
            throw new RegraNegocioException("A conversão exige autorização profissional.");
        }
    }

    private void exigirOfertaVigente(PropostaAtendimentoTO proposta, LocalDateTime instante) {
        exigirEstado(proposta, "OFERTADA");
        if (prazoEncerrado(proposta, instante) || proposta.getDataInicioReserva() == null
                || proposta.getDataInicioReserva().isAfter(instante)) {
            throw new RegraNegocioException("A proposta está expirada ou sem reserva vigente.");
        }
    }

    private boolean prazoEncerrado(PropostaAtendimentoTO proposta, LocalDateTime instante) {
        return proposta.getDataLimiteAceite() == null || !proposta.getDataLimiteAceite().isAfter(instante)
                || proposta.getDataFimReserva() == null || !proposta.getDataFimReserva().isAfter(instante);
    }

    private void exigirEstado(PropostaAtendimentoTO proposta, String situacao) {
        if (!situacao.equals(proposta.getStatusProposta())) {
            throw new RegraNegocioException("A operação exige uma proposta no estado " + situacao + ".");
        }
    }

    private void exigirApoio(Long idMembroEquipe) {
        if (!membroEquipeBO.podeApoiarAdministrativamente(idMembroEquipe)) {
            throw new AcessoNegadoException("A operação exige atribuição de apoio administrativo.");
        }
    }

    private void exigirTitular(AgendamentoTO agendamento, Long idPacienteAutenticado) {
        if (!idValido(idPacienteAutenticado) || !agendamento.getIdPaciente().equals(idPacienteAutenticado)) {
            throw new AcessoNegadoException("A proposta pertence a outro paciente.");
        }
    }

    private PropostaAtendimentoTO salvarComHistorico(Connection conexao, AgendamentoTO agendamento,
                                                       PropostaAtendimentoTO proposta, String tipo, String descricao,
                                                       String origem, Long idMembroEquipe) {
        if (propostaDAO.atualizar(conexao, proposta) == null || !registrarHistorico(conexao, agendamento,
                proposta, tipo, descricao, origem, idMembroEquipe)) {
            return null;
        }
        return proposta;
    }

    private boolean registrarHistorico(Connection conexao, AgendamentoTO agendamento, PropostaAtendimentoTO proposta,
                                       String tipo, String descricao, String origem, Long idMembroEquipe) {
        HistoricoEventoTO evento = new HistoricoEventoTO(null, agora(), tipo, descricao, origem,
                agendamento.getIdAgendamento(), proposta.getIdSolicitacao(), proposta.getIdProposta(), idMembroEquipe);
        return historicoDAO.inserir(conexao, evento) != null;
    }

    private <T> T operarSolicitacao(Long idSolicitacao, OperacaoSolicitacao<T> operacao) {
        SolicitacaoAjudaTO referencia = idValido(idSolicitacao) ? solicitacaoDAO.buscarPorId(idSolicitacao) : null;
        if (referencia == null) {
            return null;
        }
        // Ordem compartilhada com as outras BOs: agendamento, solicitação, proposta e horário.
        return TransacaoJdbc.executar(conexao -> {
            AgendamentoTO agendamento = agendamentoDAO.buscarPorIdParaAtualizar(conexao, referencia.getIdAgendamento());
            if (agendamento == null) {
                return null;
            }
            SolicitacaoAjudaTO solicitacao = solicitacaoDAO.buscarPorIdParaAtualizar(conexao, idSolicitacao);
            if (solicitacao == null || !agendamento.getIdAgendamento().equals(solicitacao.getIdAgendamento())) {
                return null;
            }
            return operacao.aplicar(conexao, solicitacao, agendamento);
        });
    }

    private PropostaAtendimentoTO operarProposta(Long idProposta, OperacaoProposta operacao) {
        PropostaAtendimentoTO referencia = buscarPorId(idProposta);
        if (referencia == null) {
            return null;
        }
        return operarSolicitacao(referencia.getIdSolicitacao(), (conexao, solicitacao, agendamento) -> {
            PropostaAtendimentoTO proposta = propostaDAO.buscarPorIdParaAtualizar(conexao, idProposta);
            if (proposta == null || !solicitacao.getIdSolicitacao().equals(proposta.getIdSolicitacao())) {
                return null;
            }
            return operacao.aplicar(conexao, proposta, solicitacao, agendamento);
        });
    }

    private boolean idValido(Long id) {
        return id != null && id > 0;
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(ZONA_NEGOCIO).withNano(0);
    }

    @FunctionalInterface
    private interface OperacaoSolicitacao<T> {
        T aplicar(Connection conexao, SolicitacaoAjudaTO solicitacao, AgendamentoTO agendamento);
    }

    @FunctionalInterface
    private interface OperacaoProposta {
        PropostaAtendimentoTO aplicar(Connection conexao, PropostaAtendimentoTO proposta,
                                       SolicitacaoAjudaTO solicitacao, AgendamentoTO agendamento);
    }
}
