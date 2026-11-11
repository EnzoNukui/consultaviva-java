package br.com.fiap.bo;

import br.com.fiap.dao.AgendamentoDAO;
import br.com.fiap.dao.HistoricoEventoDAO;
import br.com.fiap.dao.HorarioAtendimentoDAO;
import br.com.fiap.dao.MembroEquipeDAO;
import br.com.fiap.dao.PacienteDAO;
import br.com.fiap.dao.PropostaAtendimentoDAO;
import br.com.fiap.dao.SolicitacaoAjudaDAO;
import br.com.fiap.exception.AcessoNegadoException;
import br.com.fiap.exception.RegraNegocioException;
import br.com.fiap.to.AgendamentoTO;
import br.com.fiap.to.HistoricoEventoTO;
import br.com.fiap.to.HorarioAtendimentoTO;
import br.com.fiap.to.MembroEquipeTO;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Set;

public class AgendamentoBO {
    private static final ZoneId ZONA_NEGOCIO = ZoneId.of("America/Sao_Paulo");
    private static final Set<String> RESPOSTAS = Set.of("VOU_COMPARECER", "PRECISO_DE_AJUDA",
            "NAO_CONSIGO_COMPARECER");

    private AgendamentoDAO agendamentoDAO = new AgendamentoDAO();
    private PacienteDAO pacienteDAO = new PacienteDAO();
    private HorarioAtendimentoDAO horarioDAO = new HorarioAtendimentoDAO();
    private MembroEquipeDAO membroEquipeDAO = new MembroEquipeDAO();
    private SolicitacaoAjudaDAO solicitacaoDAO = new SolicitacaoAjudaDAO();
    private PropostaAtendimentoDAO propostaDAO = new PropostaAtendimentoDAO();
    private HistoricoEventoDAO historicoDAO = new HistoricoEventoDAO();
    private MembroEquipeBO membroEquipeBO = new MembroEquipeBO();

    public ArrayList<AgendamentoTO> listarTodos() {
        return agendamentoDAO.listarTodos();
    }

    public AgendamentoTO buscarPorId(Long idAgendamento) {
        return idValido(idAgendamento) ? agendamentoDAO.buscarPorId(idAgendamento) : null;
    }

    public AgendamentoTO buscarDoPaciente(Long idAgendamento, Long idPacienteAutenticado) {
        AgendamentoTO agendamento = buscarPorId(idAgendamento);
        if (agendamento != null) {
            exigirTitular(agendamento, idPacienteAutenticado);
        }
        return agendamento;
    }

    public AgendamentoTO inserir(AgendamentoTO dados, Long idMembroEquipe) {
        exigirApoio(idMembroEquipe);
        if (dados == null || !idValido(dados.getIdPaciente()) || !idValido(dados.getIdHorario())
                || pacienteDAO.buscarPorId(dados.getIdPaciente()) == null) {
            return null;
        }
        if (!"PRESENCIAL".equals(dados.getModalidade()) && !"VIRTUAL".equals(dados.getModalidade())) {
            throw new RegraNegocioException("A modalidade do agendamento é inválida.");
        }

        return TransacaoJdbc.executar(conexao -> {
            if (!validarVaga(conexao, dados.getIdHorario(), null)) {
                return null;
            }
            AgendamentoTO agendamento = new AgendamentoTO(null, dados.getModalidade(), "AGENDADO",
                    "SEM_RESPOSTA", dados.getIdPaciente(), dados.getIdHorario());
            agendamento = agendamentoDAO.inserir(conexao, agendamento);
            if (agendamento == null || !registrarHistorico(conexao, agendamento, "AGENDAMENTO_CRIADO",
                    "Agendamento experimental cadastrado.", "EQUIPE", idMembroEquipe)) {
                return null;
            }
            return agendamento;
        });
    }

    public AgendamentoTO atualizar(AgendamentoTO dados, Long idMembroEquipe) {
        exigirApoio(idMembroEquipe);
        if (dados == null || !idValido(dados.getIdAgendamento()) || !idValido(dados.getIdHorario())) {
            return null;
        }

        return TransacaoJdbc.executar(conexao -> {
            AgendamentoTO atual = agendamentoDAO.buscarPorIdParaAtualizar(conexao, dados.getIdAgendamento());
            if (atual == null) {
                return null;
            }
            exigirAgendado(atual);
            if (atual.getIdHorario().equals(dados.getIdHorario())) {
                return atual;
            }
            HorarioAtendimentoTO original = horarioDAO.buscarPorId(atual.getIdHorario());
            if (original == null) {
                return null;
            }
            if (!original.getDataInicio().isAfter(agora())) {
                throw new RegraNegocioException("Só é possível remarcar agendamentos que ainda não começaram.");
            }
            if (solicitacaoDAO.existeSolicitacaoAtivaPorAgendamento(conexao, atual.getIdAgendamento(), null)) {
                throw new RegraNegocioException("Conclua a solicitação de ajuda antes de remarcar diretamente.");
            }
            if (!validarVaga(conexao, dados.getIdHorario(), atual.getIdAgendamento())) {
                return null;
            }
            atual.setIdHorario(dados.getIdHorario());
            atual.setStatusRespostaPaciente("SEM_RESPOSTA");
            if (agendamentoDAO.atualizar(conexao, atual) == null || !registrarHistorico(conexao, atual,
                    "AGENDAMENTO_REMARCADO", "Horário alterado pela equipe.", "EQUIPE", idMembroEquipe)) {
                return null;
            }
            return atual;
        });
    }

    public boolean excluir(Long idAgendamento, Long idMembroEquipe) {
        return cancelar(idAgendamento, idMembroEquipe) != null;
    }

    public AgendamentoTO registrarResposta(Long idAgendamento, Long idPacienteAutenticado, String resposta) {
        if (!idValido(idAgendamento)) {
            return null;
        }
        if (resposta == null || !RESPOSTAS.contains(resposta)) {
            throw new RegraNegocioException("A resposta do paciente é inválida.");
        }

        return TransacaoJdbc.executar(conexao -> {
            AgendamentoTO agendamento = agendamentoDAO.buscarPorIdParaAtualizar(conexao, idAgendamento);
            if (agendamento == null) {
                return null;
            }
            exigirTitular(agendamento, idPacienteAutenticado);
            exigirAgendado(agendamento);
            HorarioAtendimentoTO horario = horarioDAO.buscarPorId(agendamento.getIdHorario());
            if (horario == null) {
                return null;
            }
            if (!horario.getDataInicio().isAfter(agora())) {
                throw new RegraNegocioException("O agendamento já começou.");
            }
            agendamento.setStatusRespostaPaciente(resposta);
            if (agendamentoDAO.atualizar(conexao, agendamento) == null || !registrarHistorico(conexao,
                    agendamento, "RESPOSTA_AGENDAMENTO", "Resposta registrada: " + resposta, "PACIENTE", null)) {
                return null;
            }
            return agendamento;
        });
    }

    public AgendamentoTO cancelar(Long idAgendamento, Long idMembroEquipe) {
        exigirApoio(idMembroEquipe);
        return alterarSituacao(idAgendamento, idMembroEquipe, "CANCELADO", false);
    }

    public AgendamentoTO cancelarPeloPaciente(Long idAgendamento, Long idPacienteAutenticado) {
        return alterarSituacao(idAgendamento, idPacienteAutenticado, "CANCELADO", true);
    }

    public AgendamentoTO registrarRealizacao(Long idAgendamento, Long idMembroEquipe) {
        exigirAtendimento(idMembroEquipe);
        return alterarSituacao(idAgendamento, idMembroEquipe, "REALIZADO", false);
    }

    public AgendamentoTO registrarFalta(Long idAgendamento, Long idMembroEquipe) {
        exigirAtendimento(idMembroEquipe);
        return alterarSituacao(idAgendamento, idMembroEquipe, "FALTOU", false);
    }

    private AgendamentoTO alterarSituacao(Long idAgendamento, Long idAtor, String situacao, boolean paciente) {
        if (!idValido(idAgendamento)) {
            return null;
        }
        return TransacaoJdbc.executar(conexao -> {
            AgendamentoTO agendamento = agendamentoDAO.buscarPorIdParaAtualizar(conexao, idAgendamento);
            if (agendamento == null) {
                return null;
            }
            if (paciente) {
                exigirTitular(agendamento, idAtor);
            }
            exigirAgendado(agendamento);
            HorarioAtendimentoTO horario = horarioDAO.buscarPorIdParaAtualizar(conexao, agendamento.getIdHorario());
            if (horario == null) {
                return null;
            }

            LocalDateTime instante = agora();
            if ("CANCELADO".equals(situacao)) {
                if (!horario.getDataInicio().isAfter(instante)) {
                    throw new RegraNegocioException("Só é possível cancelar antes do início do agendamento.");
                }
            } else {
                if (!horario.getIdMembroEquipe().equals(idAtor)) {
                    throw new AcessoNegadoException("Somente o profissional do horário pode registrar o resultado.");
                }
                LocalDateTime limite = "FALTOU".equals(situacao) ? horario.getDataFim() : horario.getDataInicio();
                if (instante.isBefore(limite)) {
                    throw new RegraNegocioException("Ainda não é possível registrar o resultado do agendamento.");
                }
            }

            agendamento.setStatusAgendamento(situacao);
            if (agendamentoDAO.atualizar(conexao, agendamento) == null || !registrarHistorico(conexao,
                    agendamento, "AGENDAMENTO_" + situacao, "Situação registrada: " + situacao,
                    paciente ? "PACIENTE" : "EQUIPE", paciente ? null : idAtor)) {
                return null;
            }
            return agendamento;
        });
    }

    private boolean validarVaga(Connection conexao, Long idHorario, Long idAgendamentoIgnorado) {
        HorarioAtendimentoTO horario = horarioDAO.buscarPorIdParaAtualizar(conexao, idHorario);
        if (horario == null) {
            return false;
        }
        MembroEquipeTO profissional = membroEquipeDAO.buscarPorIdParaAtualizar(conexao, horario.getIdMembroEquipe());
        if (profissional == null) {
            return false;
        }
        LocalDateTime instante = agora();
        if (!horario.getDataInicio().isAfter(instante) || !"S".equals(profissional.getAtendimento())) {
            throw new RegraNegocioException("O horário não está apto para um novo agendamento.");
        }
        if (agendamentoDAO.existeAgendamentoAtivoPorHorario(conexao, idHorario, idAgendamentoIgnorado)) {
            throw new RegraNegocioException("O horário já está ocupado.");
        }
        Boolean reserva = propostaDAO.existeReservaAtivaPorHorario(conexao, idHorario, instante, null);
        if (Boolean.TRUE.equals(reserva)) {
            throw new RegraNegocioException("O horário está reservado para uma proposta.");
        }
        return reserva != null;
    }

    private boolean registrarHistorico(Connection conexao, AgendamentoTO agendamento, String tipo,
                                        String descricao, String origem, Long idMembroEquipe) {
        HistoricoEventoTO evento = new HistoricoEventoTO(null, agora(), tipo, descricao, origem,
                agendamento.getIdAgendamento(), null, null, idMembroEquipe);
        return historicoDAO.inserir(conexao, evento) != null;
    }

    private void exigirApoio(Long idMembroEquipe) {
        if (!membroEquipeBO.podeApoiarAdministrativamente(idMembroEquipe)) {
            throw new AcessoNegadoException("A operação exige atribuição de apoio administrativo.");
        }
    }

    private void exigirAtendimento(Long idMembroEquipe) {
        if (!membroEquipeBO.podeAtender(idMembroEquipe)) {
            throw new AcessoNegadoException("A operação exige atribuição de atendimento.");
        }
    }

    private void exigirTitular(AgendamentoTO agendamento, Long idPacienteAutenticado) {
        if (!idValido(idPacienteAutenticado) || !agendamento.getIdPaciente().equals(idPacienteAutenticado)) {
            throw new AcessoNegadoException("O agendamento pertence a outro paciente.");
        }
    }

    private void exigirAgendado(AgendamentoTO agendamento) {
        if (!"AGENDADO".equals(agendamento.getStatusAgendamento())) {
            throw new RegraNegocioException("O agendamento já está encerrado.");
        }
    }

    private boolean idValido(Long id) {
        return id != null && id > 0;
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(ZONA_NEGOCIO).withNano(0);
    }
}
