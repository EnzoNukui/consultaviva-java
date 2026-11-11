package br.com.fiap.bo;

import br.com.fiap.dao.AgendamentoDAO;
import br.com.fiap.dao.DisponibilidadeDAO;
import br.com.fiap.dao.HistoricoEventoDAO;
import br.com.fiap.dao.HorarioAtendimentoDAO;
import br.com.fiap.dao.PropostaAtendimentoDAO;
import br.com.fiap.dao.SolicitacaoAjudaDAO;
import br.com.fiap.dao.SolicitacaoDificuldadeDAO;
import br.com.fiap.dao.TipoDificuldadeDAO;
import br.com.fiap.exception.AcessoNegadoException;
import br.com.fiap.exception.RegraNegocioException;
import br.com.fiap.to.AgendamentoTO;
import br.com.fiap.to.DisponibilidadeTO;
import br.com.fiap.to.HistoricoEventoTO;
import br.com.fiap.to.HorarioAtendimentoTO;
import br.com.fiap.to.PropostaAtendimentoTO;
import br.com.fiap.to.SolicitacaoAjudaTO;
import br.com.fiap.to.SolicitacaoDificuldadeTO;
import br.com.fiap.to.TipoDificuldadeTO;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SolicitacaoAjudaBO {
    private static final ZoneId ZONA_NEGOCIO = ZoneId.of("America/Sao_Paulo");
    private static final Set<String> MODALIDADES_DISPONIBILIDADE = Set.of("PRESENCIAL", "VIRTUAL", "AMBAS");

    private SolicitacaoAjudaDAO solicitacaoDAO = new SolicitacaoAjudaDAO();
    private AgendamentoDAO agendamentoDAO = new AgendamentoDAO();
    private HorarioAtendimentoDAO horarioDAO = new HorarioAtendimentoDAO();
    private TipoDificuldadeDAO tipoDificuldadeDAO = new TipoDificuldadeDAO();
    private SolicitacaoDificuldadeDAO dificuldadeDAO = new SolicitacaoDificuldadeDAO();
    private DisponibilidadeDAO disponibilidadeDAO = new DisponibilidadeDAO();
    private PropostaAtendimentoDAO propostaDAO = new PropostaAtendimentoDAO();
    private HistoricoEventoDAO historicoDAO = new HistoricoEventoDAO();
    private MembroEquipeBO membroEquipeBO = new MembroEquipeBO();

    public ArrayList<SolicitacaoAjudaTO> listarTodos() {
        return solicitacaoDAO.listarTodos();
    }

    public SolicitacaoAjudaTO buscarPorId(Long idSolicitacao) {
        return idValido(idSolicitacao) ? solicitacaoDAO.buscarPorId(idSolicitacao) : null;
    }

    public SolicitacaoAjudaTO buscarDoPaciente(Long idSolicitacao, Long idPacienteAutenticado) {
        SolicitacaoAjudaTO solicitacao = buscarPorId(idSolicitacao);
        if (solicitacao == null) {
            return null;
        }
        AgendamentoTO agendamento = agendamentoDAO.buscarPorId(solicitacao.getIdAgendamento());
        if (agendamento == null) {
            return null;
        }
        exigirTitular(agendamento, idPacienteAutenticado);
        return solicitacao;
    }

    public ArrayList<SolicitacaoAjudaTO> listarPorAgendamento(Long idAgendamento, Long idPacienteAutenticado) {
        AgendamentoTO agendamento = idValido(idAgendamento) ? agendamentoDAO.buscarPorId(idAgendamento) : null;
        if (agendamento == null) {
            return new ArrayList<>();
        }
        exigirTitular(agendamento, idPacienteAutenticado);
        return solicitacaoDAO.listarPorAgendamento(idAgendamento);
    }

    public ArrayList<TipoDificuldadeTO> listarTiposDificuldade() {
        return tipoDificuldadeDAO.listarTodos();
    }

    public ArrayList<SolicitacaoDificuldadeTO> listarDificuldades(Long idSolicitacao, Long idPacienteAutenticado) {
        if (buscarDoPaciente(idSolicitacao, idPacienteAutenticado) == null) {
            return new ArrayList<>();
        }
        return dificuldadeDAO.listarPorSolicitacao(idSolicitacao);
    }

    public ArrayList<DisponibilidadeTO> listarDisponibilidades(Long idSolicitacao, Long idPacienteAutenticado) {
        if (buscarDoPaciente(idSolicitacao, idPacienteAutenticado) == null) {
            return new ArrayList<>();
        }
        return TransacaoJdbc.executar(conexao -> disponibilidadeDAO.listarPorSolicitacao(conexao, idSolicitacao));
    }

    public SolicitacaoAjudaTO inserir(SolicitacaoAjudaTO dados, Long idPacienteAutenticado,
                                    List<Long> idsDificuldade, List<DisponibilidadeTO> disponibilidades) {
        if (dados == null || !idValido(dados.getIdAgendamento())) {
            return null;
        }
        validarRelato(dados.getRelato());
        Set<Long> dificuldades = idsDificuldade == null ? Set.of() : new LinkedHashSet<>(idsDificuldade);
        List<DisponibilidadeTO> intervalos = disponibilidades == null ? List.of() : disponibilidades;
        for (Long idDificuldade : dificuldades) {
            if (!idValido(idDificuldade) || tipoDificuldadeDAO.buscarPorId(idDificuldade) == null) {
                throw new RegraNegocioException("O tipo de dificuldade informado não existe.");
            }
        }
        for (DisponibilidadeTO intervalo : intervalos) {
            validarDisponibilidade(intervalo);
        }

        return TransacaoJdbc.executar(conexao -> {
            AgendamentoTO agendamento = agendamentoDAO.buscarPorIdParaAtualizar(conexao, dados.getIdAgendamento());
            if (agendamento == null) {
                return null;
            }
            exigirTitular(agendamento, idPacienteAutenticado);
            if (!verificarAgendamentoFuturo(agendamento)) {
                return null;
            }
            if (solicitacaoDAO.existeSolicitacaoAtivaPorAgendamento(conexao, agendamento.getIdAgendamento(), null)) {
                throw new RegraNegocioException("Já existe uma solicitação ativa para este agendamento.");
            }
            if (intervalos.isEmpty()) {
                throw new RegraNegocioException("Informe ao menos uma disponibilidade para abrir a solicitação.");
            }

            SolicitacaoAjudaTO solicitacao = new SolicitacaoAjudaTO(null, agora(), null, dados.getRelato(),
                    "ABERTA", agendamento.getIdAgendamento(), null);
            solicitacao = solicitacaoDAO.inserir(conexao, solicitacao);
            if (solicitacao == null) {
                return null;
            }
            Long idSolicitacao = solicitacao.getIdSolicitacao();
            for (Long idDificuldade : dificuldades) {
                SolicitacaoDificuldadeTO vinculo = new SolicitacaoDificuldadeTO(idSolicitacao, idDificuldade);
                if (dificuldadeDAO.inserir(conexao, vinculo) == null) {
                    return null;
                }
            }
            for (DisponibilidadeTO intervalo : intervalos) {
                DisponibilidadeTO disponibilidade = new DisponibilidadeTO(null, intervalo.getDataInicio(),
                        intervalo.getDataFim(), intervalo.getModalidade(), idSolicitacao);
                if (disponibilidadeDAO.inserir(conexao, disponibilidade) == null) {
                    return null;
                }
            }
            return registrarHistorico(conexao, solicitacao, "SOLICITACAO_ABERTA", "Solicitação de ajuda aberta.",
                    "PACIENTE", null) ? solicitacao : null;
        });
    }

    public SolicitacaoAjudaTO atualizar(SolicitacaoAjudaTO dados, Long idPacienteAutenticado) {
        if (dados == null) {
            return null;
        }
        validarRelato(dados.getRelato());
        return operar(dados.getIdSolicitacao(), (conexao, solicitacao, agendamento) -> {
            exigirTitular(agendamento, idPacienteAutenticado);
            exigirAberta(solicitacao);
            if (!verificarAgendamentoFuturo(agendamento)) {
                return null;
            }
            solicitacao.setRelato(dados.getRelato());
            return solicitacaoDAO.atualizar(conexao, solicitacao);
        });
    }

    public DisponibilidadeTO adicionarDisponibilidade(Long idSolicitacao, Long idPacienteAutenticado,
                                                      DisponibilidadeTO dados) {
        validarDisponibilidade(dados);
        return operar(idSolicitacao, (conexao, solicitacao, agendamento) -> {
            exigirTitular(agendamento, idPacienteAutenticado);
            exigirAberta(solicitacao);
            if (!verificarAgendamentoFuturo(agendamento)) {
                return null;
            }
            DisponibilidadeTO disponibilidade = new DisponibilidadeTO(null, dados.getDataInicio(),
                    dados.getDataFim(), dados.getModalidade(), solicitacao.getIdSolicitacao());
            DisponibilidadeTO inserida = disponibilidadeDAO.inserir(conexao, disponibilidade);
            if (inserida == null || !registrarHistorico(conexao, solicitacao, "DISPONIBILIDADE_ADICIONADA",
                    "Disponibilidade adicionada à solicitação.", "PACIENTE", null)) {
                return null;
            }
            return inserida;
        });
    }

    public SolicitacaoDificuldadeTO adicionarDificuldade(Long idSolicitacao, Long idPacienteAutenticado,
                                                       Long idDificuldade) {
        if (!idValido(idDificuldade) || tipoDificuldadeDAO.buscarPorId(idDificuldade) == null) {
            throw new RegraNegocioException("O tipo de dificuldade informado não existe.");
        }
        return operar(idSolicitacao, (conexao, solicitacao, agendamento) -> {
            exigirTitular(agendamento, idPacienteAutenticado);
            exigirAberta(solicitacao);
            if (!verificarAgendamentoFuturo(agendamento)) {
                return null;
            }
            SolicitacaoDificuldadeTO vinculo = new SolicitacaoDificuldadeTO(
                    solicitacao.getIdSolicitacao(), idDificuldade);
            SolicitacaoDificuldadeTO inserido = dificuldadeDAO.inserir(conexao, vinculo);
            if (inserido == null || !registrarHistorico(conexao, solicitacao, "DIFICULDADE_ADICIONADA",
                    "Dificuldade adicionada à solicitação.", "PACIENTE", null)) {
                return null;
            }
            return inserido;
        });
    }

    public SolicitacaoDificuldadeTO excluirDificuldade(Long idSolicitacao, Long idPacienteAutenticado,
                                                       Long idDificuldade) {
        if (!idValido(idDificuldade)) {
            return null;
        }
        return operar(idSolicitacao, (conexao, solicitacao, agendamento) -> {
            exigirTitular(agendamento, idPacienteAutenticado);
            exigirAberta(solicitacao);
            if (!verificarAgendamentoFuturo(agendamento)) {
                return null;
            }
            SolicitacaoDificuldadeTO vinculo = dificuldadeDAO.buscarPorIds(conexao,
                    solicitacao.getIdSolicitacao(), idDificuldade);
            if (vinculo == null || !dificuldadeDAO.excluir(conexao,
                    solicitacao.getIdSolicitacao(), idDificuldade)) {
                return null;
            }
            if (!registrarHistorico(conexao, solicitacao, "DIFICULDADE_REMOVIDA",
                    "Dificuldade removida da solicitação.", "PACIENTE", null)) {
                return null;
            }
            return vinculo;
        });
    }

    public DisponibilidadeTO atualizarDisponibilidade(Long idSolicitacao, Long idDisponibilidade,
                                                      Long idPacienteAutenticado, DisponibilidadeTO dados) {
        if (!idValido(idDisponibilidade)) {
            return null;
        }
        validarDisponibilidade(dados);
        return operar(idSolicitacao, (conexao, solicitacao, agendamento) -> {
            exigirTitular(agendamento, idPacienteAutenticado);
            exigirAberta(solicitacao);
            if (!verificarAgendamentoFuturo(agendamento)) {
                return null;
            }
            DisponibilidadeTO disponibilidade = disponibilidadeDAO.buscarPorId(conexao, idDisponibilidade,
                    solicitacao.getIdSolicitacao());
            if (disponibilidade == null) {
                return null;
            }
            disponibilidade.setDataInicio(dados.getDataInicio());
            disponibilidade.setDataFim(dados.getDataFim());
            disponibilidade.setModalidade(dados.getModalidade());
            DisponibilidadeTO atualizada = disponibilidadeDAO.atualizar(conexao, disponibilidade);
            if (atualizada == null || !registrarHistorico(conexao, solicitacao, "DISPONIBILIDADE_ATUALIZADA",
                    "Disponibilidade da solicitação atualizada.", "PACIENTE", null)) {
                return null;
            }
            return atualizada;
        });
    }

    public DisponibilidadeTO excluirDisponibilidade(Long idSolicitacao, Long idDisponibilidade,
                                                    Long idPacienteAutenticado) {
        if (!idValido(idDisponibilidade)) {
            return null;
        }
        return operar(idSolicitacao, (conexao, solicitacao, agendamento) -> {
            exigirTitular(agendamento, idPacienteAutenticado);
            exigirAberta(solicitacao);
            if (!verificarAgendamentoFuturo(agendamento)) {
                return null;
            }
            DisponibilidadeTO disponibilidade = disponibilidadeDAO.buscarPorId(conexao, idDisponibilidade,
                    solicitacao.getIdSolicitacao());
            if (disponibilidade == null || !disponibilidadeDAO.excluir(conexao, idDisponibilidade,
                    solicitacao.getIdSolicitacao())) {
                return null;
            }
            if (!registrarHistorico(conexao, solicitacao, "DISPONIBILIDADE_REMOVIDA",
                    "Disponibilidade removida da solicitação.", "PACIENTE", null)) {
                return null;
            }
            return disponibilidade;
        });
    }

    public SolicitacaoAjudaTO iniciarAnalise(Long idSolicitacao, Long idMembroEquipe) {
        exigirApoio(idMembroEquipe);
        return operar(idSolicitacao, (conexao, solicitacao, agendamento) -> {
            exigirAberta(solicitacao);
            if (!verificarAgendamentoFuturo(agendamento)) {
                return null;
            }
            solicitacao.setStatusSolicitacao("EM_ANALISE");
            solicitacao.setIdMembroEquipe(idMembroEquipe);
            if (solicitacaoDAO.atualizar(conexao, solicitacao) == null || !registrarHistorico(conexao,
                    solicitacao, "ANALISE_INICIADA", "Análise da solicitação iniciada.", "EQUIPE", idMembroEquipe)) {
                return null;
            }
            return solicitacao;
        });
    }

    public SolicitacaoAjudaTO encerrar(Long idSolicitacao, Long idMembroEquipe, String situacao) {
        exigirApoio(idMembroEquipe);
        if (!"SEM_SOLUCAO".equals(situacao) && !"CANCELADA".equals(situacao)) {
            throw new RegraNegocioException("Use SEM_SOLUCAO ou CANCELADA para encerrar manualmente.");
        }
        return operar(idSolicitacao, (conexao, solicitacao, agendamento) ->
                concluirEncerramento(conexao, solicitacao, situacao, "EQUIPE", idMembroEquipe));
    }

    public boolean excluir(Long idSolicitacao, Long idPacienteAutenticado) {
        return operar(idSolicitacao, (conexao, solicitacao, agendamento) -> {
            exigirTitular(agendamento, idPacienteAutenticado);
            return concluirEncerramento(conexao, solicitacao, "CANCELADA", "PACIENTE", null);
        }) != null;
    }

    private SolicitacaoAjudaTO concluirEncerramento(Connection conexao, SolicitacaoAjudaTO solicitacao,
                                                   String situacao, String origem, Long idMembroEquipe) {
        String atual = solicitacao.getStatusSolicitacao();
        if (!"ABERTA".equals(atual) && !"EM_ANALISE".equals(atual)) {
            throw new RegraNegocioException("A solicitação já está encerrada.");
        }
        if ("SEM_SOLUCAO".equals(situacao) && !"EM_ANALISE".equals(atual)) {
            throw new RegraNegocioException("Inicie a análise antes de encerrar a solicitação sem solução.");
        }
        ArrayList<PropostaAtendimentoTO> propostas = propostaDAO.listarPorSolicitacao(conexao,
                solicitacao.getIdSolicitacao());
        if (propostas == null) {
            return null;
        }
        for (PropostaAtendimentoTO proposta : propostas) {
            if ("EM_ANALISE".equals(proposta.getStatusProposta()) || "OFERTADA".equals(proposta.getStatusProposta())) {
                proposta.setStatusProposta("CANCELADA");
                if (propostaDAO.atualizar(conexao, proposta) == null) {
                    return null;
                }
            }
        }
        solicitacao.setStatusSolicitacao(situacao);
        solicitacao.setDataEncerramento(agora());
        if (solicitacaoDAO.atualizar(conexao, solicitacao) == null || !registrarHistorico(conexao,
                solicitacao, "SOLICITACAO_ENCERRADA", "Solicitação encerrada: " + situacao, origem, idMembroEquipe)) {
            return null;
        }
        return solicitacao;
    }

    private <T> T operar(Long idSolicitacao, OperacaoSolicitacao<T> operacao) {
        SolicitacaoAjudaTO referencia = buscarPorId(idSolicitacao);
        if (referencia == null) {
            return null;
        }
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

    private boolean verificarAgendamentoFuturo(AgendamentoTO agendamento) {
        if (!"AGENDADO".equals(agendamento.getStatusAgendamento())) {
            throw new RegraNegocioException("O agendamento já está encerrado.");
        }
        HorarioAtendimentoTO horario = horarioDAO.buscarPorId(agendamento.getIdHorario());
        if (horario == null) {
            return false;
        }
        if (!horario.getDataInicio().isAfter(agora())) {
            throw new RegraNegocioException("O agendamento já começou.");
        }
        return true;
    }

    private void exigirAberta(SolicitacaoAjudaTO solicitacao) {
        if (!"ABERTA".equals(solicitacao.getStatusSolicitacao())) {
            throw new RegraNegocioException("A operação exige uma solicitação aberta.");
        }
    }

    private void exigirTitular(AgendamentoTO agendamento, Long idPacienteAutenticado) {
        if (!idValido(idPacienteAutenticado) || !agendamento.getIdPaciente().equals(idPacienteAutenticado)) {
            throw new AcessoNegadoException("A solicitação pertence a outro paciente.");
        }
    }

    private void exigirApoio(Long idMembroEquipe) {
        if (!membroEquipeBO.podeApoiarAdministrativamente(idMembroEquipe)) {
            throw new AcessoNegadoException("A operação exige atribuição de apoio administrativo.");
        }
    }

    private void validarDisponibilidade(DisponibilidadeTO disponibilidade) {
        if (disponibilidade == null || disponibilidade.getDataInicio() == null || disponibilidade.getDataFim() == null
                || !disponibilidade.getDataFim().isAfter(disponibilidade.getDataInicio())
                || disponibilidade.getModalidade() == null
                || !MODALIDADES_DISPONIBILIDADE.contains(disponibilidade.getModalidade())) {
            throw new IllegalArgumentException(
                    "A disponibilidade deve ter um intervalo válido e uma modalidade aceita.");
        }
    }

    private void validarRelato(String relato) {
        if (relato != null && relato.length() > 500) {
            throw new RegraNegocioException("O relato deve ter até 500 caracteres.");
        }
    }

    private boolean registrarHistorico(Connection conexao, SolicitacaoAjudaTO solicitacao, String tipo,
                                       String descricao, String origem, Long idMembroEquipe) {
        HistoricoEventoTO evento = new HistoricoEventoTO(null, agora(), tipo, descricao, origem,
                solicitacao.getIdAgendamento(), solicitacao.getIdSolicitacao(), null, idMembroEquipe);
        return historicoDAO.inserir(conexao, evento) != null;
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
}
