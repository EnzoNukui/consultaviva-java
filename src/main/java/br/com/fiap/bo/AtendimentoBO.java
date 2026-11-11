package br.com.fiap.bo;

import br.com.fiap.dao.AgendamentoDAO;
import br.com.fiap.dao.HistoricoEventoDAO;
import br.com.fiap.dao.HorarioAtendimentoDAO;
import br.com.fiap.dao.PlanoComparecimentoDAO;
import br.com.fiap.dao.PreparoVirtualDAO;
import br.com.fiap.dao.PropostaAtendimentoDAO;
import br.com.fiap.dao.SincronizacaoAgendaDAO;
import br.com.fiap.dao.SolicitacaoAjudaDAO;
import br.com.fiap.exception.AcessoNegadoException;
import br.com.fiap.exception.RegraNegocioException;
import br.com.fiap.to.AgendamentoTO;
import br.com.fiap.to.HistoricoEventoTO;
import br.com.fiap.to.HorarioAtendimentoTO;
import br.com.fiap.to.PlanoComparecimentoTO;
import br.com.fiap.to.PreparoVirtualTO;
import br.com.fiap.to.PropostaAtendimentoTO;
import br.com.fiap.to.SincronizacaoAgendaTO;
import br.com.fiap.to.SolicitacaoAjudaTO;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

public class AtendimentoBO {
    private static final ZoneId ZONA_NEGOCIO = ZoneId.of("America/Sao_Paulo");

    private AgendamentoDAO agendamentoDAO = new AgendamentoDAO();
    private HorarioAtendimentoDAO horarioDAO = new HorarioAtendimentoDAO();
    private SolicitacaoAjudaDAO solicitacaoDAO = new SolicitacaoAjudaDAO();
    private PropostaAtendimentoDAO propostaDAO = new PropostaAtendimentoDAO();
    private PlanoComparecimentoDAO planoDAO = new PlanoComparecimentoDAO();
    private PreparoVirtualDAO preparoDAO = new PreparoVirtualDAO();
    private HistoricoEventoDAO historicoDAO = new HistoricoEventoDAO();
    private SincronizacaoAgendaDAO sincronizacaoDAO = new SincronizacaoAgendaDAO();

    public ArrayList<PlanoComparecimentoTO> listarPlanos(Long idAgendamento, Long idPacienteAutenticado) {
        if (buscarAgendamento(idAgendamento, idPacienteAutenticado) == null) {
            return new ArrayList<>();
        }
        return planoDAO.listarPorAgendamento(idAgendamento);
    }

    public PlanoComparecimentoTO buscarPlano(Long idPlano, Long idPacienteAutenticado) {
        PlanoComparecimentoTO plano = idValido(idPlano) ? planoDAO.buscarPorId(idPlano) : null;
        if (plano == null || buscarAgendamento(plano.getIdAgendamento(), idPacienteAutenticado) == null) {
            return null;
        }
        return plano;
    }

    public PlanoComparecimentoTO inserirPlano(PlanoComparecimentoTO dados, Long idPacienteAutenticado) {
        return salvarPlano(dados, idPacienteAutenticado, null);
    }

    public PlanoComparecimentoTO atualizarPlano(PlanoComparecimentoTO dados, Long idPacienteAutenticado) {
        if (dados == null) {
            return null;
        }
        PlanoComparecimentoTO atual = buscarPlano(dados.getIdPlano(), idPacienteAutenticado);
        return atual == null ? null : salvarPlano(dados, idPacienteAutenticado, atual);
    }

    public boolean excluirPlano(Long idPlano, Long idPacienteAutenticado) {
        return buscarPlano(idPlano, idPacienteAutenticado) != null && planoDAO.excluir(idPlano);
    }

    public ArrayList<PreparoVirtualTO> listarPreparos(Long idAgendamento, Long idPacienteAutenticado) {
        if (buscarAgendamento(idAgendamento, idPacienteAutenticado) == null) {
            return new ArrayList<>();
        }
        return preparoDAO.listarPorAgendamento(idAgendamento);
    }

    public PreparoVirtualTO buscarPreparo(Long idPreparo, Long idPacienteAutenticado) {
        PreparoVirtualTO preparo = idValido(idPreparo) ? preparoDAO.buscarPorId(idPreparo) : null;
        if (preparo == null || buscarAgendamento(preparo.getIdAgendamento(), idPacienteAutenticado) == null) {
            return null;
        }
        return preparo;
    }

    public PreparoVirtualTO registrarPreparoVirtual(PreparoVirtualTO dados, Long idPacienteAutenticado) {
        return salvarPreparo(dados, idPacienteAutenticado, null);
    }

    public PreparoVirtualTO atualizarPreparo(PreparoVirtualTO dados, Long idPacienteAutenticado) {
        if (dados == null) {
            return null;
        }
        PreparoVirtualTO atual = buscarPreparo(dados.getIdPreparo(), idPacienteAutenticado);
        return atual == null ? null : salvarPreparo(dados, idPacienteAutenticado, atual);
    }

    public boolean excluirPreparo(Long idPreparo, Long idPacienteAutenticado) {
        return buscarPreparo(idPreparo, idPacienteAutenticado) != null && preparoDAO.excluir(idPreparo);
    }

    public ArrayList<HistoricoEventoTO> listarHistorico(Long idAgendamento, Long idPacienteAutenticado) {
        if (buscarAgendamento(idAgendamento, idPacienteAutenticado) == null) {
            return new ArrayList<>();
        }
        return historicoDAO.listarPorAgendamento(idAgendamento);
    }

    public ArrayList<SincronizacaoAgendaTO> listarSincronizacoes(Long idAgendamento, Long idPacienteAutenticado) {
        if (buscarAgendamento(idAgendamento, idPacienteAutenticado) == null) {
            return new ArrayList<>();
        }
        return sincronizacaoDAO.listarPorAgendamento(idAgendamento);
    }

    private PlanoComparecimentoTO salvarPlano(PlanoComparecimentoTO dados, Long idPacienteAutenticado,
                                              PlanoComparecimentoTO atual) {
        if (dados == null) {
            return null;
        }
        Long idAgendamento = atual == null ? dados.getIdAgendamento() : atual.getIdAgendamento();
        Long idProposta = atual == null ? dados.getIdProposta() : atual.getIdProposta();
        if (!idValido(idAgendamento)) {
            return null;
        }
        validarPlano(dados);

        return TransacaoJdbc.executar(conexao -> {
            AgendamentoTO agendamento = agendamentoDAO.buscarPorIdParaAtualizar(conexao, idAgendamento);
            if (agendamento == null) {
                return null;
            }
            exigirTitular(agendamento, idPacienteAutenticado);
            HorarioAtendimentoTO horario = buscarHorarioDeApoio(agendamento, idProposta, "PRESENCIAL");
            if (horario == null) {
                return null;
            }

            LocalDateTime referencia = horario.getDataInicio();
            LocalDateTime saida = referencia.minusMinutes(dados.getDuracaoMinutos() + dados.getMargemMinutos());
            PlanoComparecimentoTO plano = new PlanoComparecimentoTO(atual == null ? null : atual.getIdPlano(),
                    agora(), dados.getOrigemTrajeto(), dados.getDestinoTrajeto(), dados.getMeioTransporte(),
                    "Estimativa informada", dados.getDuracaoMinutos(), dados.getMargemMinutos(), referencia,
                    saida, "S", idAgendamento, idProposta);
            if (atual == null) {
                return planoDAO.inserir(conexao, plano);
            }
            return planoDAO.atualizar(conexao, plano);
        });
    }

    private PreparoVirtualTO salvarPreparo(PreparoVirtualTO dados, Long idPacienteAutenticado,
                                          PreparoVirtualTO atual) {
        if (dados == null) {
            return null;
        }
        Long idAgendamento = atual == null ? dados.getIdAgendamento() : atual.getIdAgendamento();
        Long idProposta = atual == null ? dados.getIdProposta() : atual.getIdProposta();
        if (!idValido(idAgendamento)) {
            return null;
        }
        validarPreparo(dados);

        return TransacaoJdbc.executar(conexao -> {
            AgendamentoTO agendamento = agendamentoDAO.buscarPorIdParaAtualizar(conexao, idAgendamento);
            if (agendamento == null) {
                return null;
            }
            exigirTitular(agendamento, idPacienteAutenticado);
            if (buscarHorarioDeApoio(agendamento, idProposta, "VIRTUAL") == null) {
                return null;
            }

            PreparoVirtualTO preparo = new PreparoVirtualTO(atual == null ? null : atual.getIdPreparo(),
                    agora(), dados.getStatusPermissaoMicrofone(), dados.getStatusPermissaoCamera(),
                    dados.getStatusCamera(), dados.getStatusMicrofone(), dados.getStatusConexao(),
                    dados.getObservacao(), idAgendamento, idProposta);
            if (atual == null) {
                return preparoDAO.inserir(conexao, preparo);
            }
            return preparoDAO.atualizar(conexao, preparo);
        });
    }

    private HorarioAtendimentoTO buscarHorarioDeApoio(AgendamentoTO agendamento, Long idProposta, String modalidade) {
        if (!"AGENDADO".equals(agendamento.getStatusAgendamento())) {
            throw new RegraNegocioException("O agendamento precisa estar ativo para registrar o apoio.");
        }
        Long idHorario = agendamento.getIdHorario();
        String modalidadeAtual = agendamento.getModalidade();
        if (idProposta != null) {
            PropostaAtendimentoTO proposta = idValido(idProposta) ? propostaDAO.buscarPorId(idProposta) : null;
            if (proposta == null) {
                throw new RegraNegocioException("A proposta informada não existe.");
            }
            SolicitacaoAjudaTO solicitacao = solicitacaoDAO.buscarPorId(proposta.getIdSolicitacao());
            if (solicitacao == null) {
                return null;
            }
            if (!agendamento.getIdAgendamento().equals(solicitacao.getIdAgendamento())) {
                throw new AcessoNegadoException("A proposta não pertence ao agendamento informado.");
            }
            if (!"CONFIRMADA".equals(proposta.getStatusProposta()) && !ofertaVigente(proposta)) {
                throw new RegraNegocioException("O registro de apoio exige uma proposta confirmada ou ofertada e vigente.");
            }
            if ("CONFIRMADA".equals(proposta.getStatusProposta())
                    && (!idHorario.equals(proposta.getIdHorario()) || !modalidadeAtual.equals(proposta.getModalidade()))) {
                throw new RegraNegocioException("A proposta confirmada não corresponde mais ao agendamento atual.");
            }
            idHorario = proposta.getIdHorario();
            modalidadeAtual = proposta.getModalidade();
        }
        if (!modalidade.equals(modalidadeAtual)) {
            throw new RegraNegocioException("O registro de apoio exige a modalidade " + modalidade + ".");
        }
        HorarioAtendimentoTO horario = horarioDAO.buscarPorId(idHorario);
        if (horario != null && !horario.getDataInicio().isAfter(agora())) {
            throw new RegraNegocioException("O horário de atendimento já começou.");
        }
        return horario;
    }

    private boolean ofertaVigente(PropostaAtendimentoTO proposta) {
        LocalDateTime instante = agora();
        return "OFERTADA".equals(proposta.getStatusProposta()) && proposta.getDataInicioReserva() != null
                && !proposta.getDataInicioReserva().isAfter(instante) && proposta.getDataFimReserva() != null
                && proposta.getDataFimReserva().isAfter(instante) && proposta.getDataLimiteAceite() != null
                && proposta.getDataLimiteAceite().isAfter(instante);
    }

    private AgendamentoTO buscarAgendamento(Long idAgendamento, Long idPacienteAutenticado) {
        AgendamentoTO agendamento = idValido(idAgendamento) ? agendamentoDAO.buscarPorId(idAgendamento) : null;
        if (agendamento != null) {
            exigirTitular(agendamento, idPacienteAutenticado);
        }
        return agendamento;
    }

    private void exigirTitular(AgendamentoTO agendamento, Long idPacienteAutenticado) {
        if (!idValido(idPacienteAutenticado) || !agendamento.getIdPaciente().equals(idPacienteAutenticado)) {
            throw new AcessoNegadoException("O atendimento pertence a outro paciente.");
        }
    }

    private void validarPlano(PlanoComparecimentoTO plano) {
        if (!textoValido(plano.getOrigemTrajeto(), 250) || !textoValido(plano.getDestinoTrajeto(), 250)
                || !textoValido(plano.getMeioTransporte(), 60) || plano.getDuracaoMinutos() == null
                || plano.getDuracaoMinutos() <= 0 || plano.getDuracaoMinutos() > 9999
                || plano.getMargemMinutos() == null || plano.getMargemMinutos() < 0 || plano.getMargemMinutos() > 999) {
            throw new RegraNegocioException("Informe origem, destino, transporte, duração positiva e margem válida.");
        }
    }

    private void validarPreparo(PreparoVirtualTO preparo) {
        if (!textoValido(preparo.getStatusPermissaoMicrofone(), 15)
                || !textoValido(preparo.getStatusPermissaoCamera(), 15) || !textoValido(preparo.getStatusCamera(), 15)
                || !textoValido(preparo.getStatusMicrofone(), 15) || !textoValido(preparo.getStatusConexao(), 15)
                || (preparo.getObservacao() != null && preparo.getObservacao().length() > 500)) {
            throw new RegraNegocioException("Informe os resultados da verificação de câmera, microfone e conexão.");
        }
    }

    private boolean textoValido(String valor, int limite) {
        return valor != null && !valor.isBlank() && valor.length() <= limite;
    }

    private boolean idValido(Long id) {
        return id != null && id > 0;
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(ZONA_NEGOCIO).withNano(0);
    }
}
