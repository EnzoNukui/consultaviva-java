package br.com.fiap.bo;

import br.com.fiap.dao.AgendamentoDAO;
import br.com.fiap.dao.ConnectionFactory;
import br.com.fiap.dao.HorarioAtendimentoDAO;
import br.com.fiap.dao.MembroEquipeDAO;
import br.com.fiap.dao.PropostaAtendimentoDAO;
import br.com.fiap.dao.UnidadeDAO;
import br.com.fiap.exception.RegraNegocioException;
import br.com.fiap.to.HorarioAtendimentoTO;
import br.com.fiap.to.MembroEquipeTO;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

public class HorarioAtendimentoBO {
    private static final ZoneId ZONA_NEGOCIO = ZoneId.of("America/Sao_Paulo");

    private HorarioAtendimentoDAO horarioDAO = new HorarioAtendimentoDAO();
    private UnidadeDAO unidadeDAO = new UnidadeDAO();
    private MembroEquipeDAO membroEquipeDAO = new MembroEquipeDAO();
    private AgendamentoDAO agendamentoDAO = new AgendamentoDAO();
    private PropostaAtendimentoDAO propostaDAO = new PropostaAtendimentoDAO();

    public ArrayList<HorarioAtendimentoTO> listarTodos() {
        return horarioDAO.listarTodos();
    }

    public HorarioAtendimentoTO buscarPorId(Long idHorario) {
        if (idHorario == null || idHorario <= 0) {
            return null;
        }
        return horarioDAO.buscarPorId(idHorario);
    }

    public ArrayList<HorarioAtendimentoTO> listarPorUnidade(Long idUnidade) {
        if (idUnidade == null || idUnidade <= 0) {
            return new ArrayList<>();
        }
        return horarioDAO.listarPorUnidade(idUnidade);
    }

    public ArrayList<HorarioAtendimentoTO> listarPorMembroEquipe(Long idMembroEquipe) {
        if (idMembroEquipe == null || idMembroEquipe <= 0) {
            return new ArrayList<>();
        }
        return horarioDAO.listarPorMembroEquipe(idMembroEquipe);
    }

    public ArrayList<HorarioAtendimentoTO> listarDisponiveis() {
        return horarioDAO.listarDisponiveis(LocalDateTime.now(ZONA_NEGOCIO));
    }

    public HorarioAtendimentoTO inserir(HorarioAtendimentoTO horario) {
        return salvar(horario, false);
    }

    public HorarioAtendimentoTO atualizar(HorarioAtendimentoTO horario) {
        if (horario == null || horario.getIdHorario() == null || horario.getIdHorario() <= 0) {
            return null;
        }
        return salvar(horario, true);
    }

    public boolean excluir(Long idHorario) {
        return idHorario != null && idHorario > 0 && horarioDAO.excluir(idHorario);
    }

    private HorarioAtendimentoTO salvar(HorarioAtendimentoTO horario, boolean atualizacao) {
        if (horario == null || horario.getIdUnidade() == null || horario.getIdUnidade() <= 0
                || horario.getIdMembroEquipe() == null || horario.getIdMembroEquipe() <= 0) {
            return null;
        }
        if (horario.getDataInicio() == null || horario.getDataFim() == null
                || !horario.getDataFim().isAfter(horario.getDataInicio())) {
            throw new RegraNegocioException("O fim do horário deve ser posterior ao início.");
        }
        if (unidadeDAO.buscarPorId(horario.getIdUnidade()) == null) {
            return null;
        }

        Connection conexao = ConnectionFactory.getNewConnection();
        if (conexao == null) {
            return null;
        }

        Long idOriginal = horario.getIdHorario();
        HorarioAtendimentoTO salvo = null;
        try (conexao) {
            conexao.setAutoCommit(false);
            try {
                HorarioAtendimentoTO resultado = gravarHorario(conexao, horario, atualizacao);
                if (resultado == null) {
                    conexao.rollback();
                } else {
                    conexao.commit();
                    salvo = resultado;
                }
            } catch (SQLException | RuntimeException e) {
                try {
                    conexao.rollback();
                } catch (SQLException erroRollback) {
                    e.addSuppressed(erroRollback);
                }
                if (e instanceof RegraNegocioException regraNegocio) {
                    throw regraNegocio;
                }
                System.out.println("Erro ao salvar horário: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Erro na conexão da transação do horário: " + e.getMessage());
        }

        if (salvo == null && !atualizacao) {
            horario.setIdHorario(idOriginal);
        }
        return salvo;
    }

    private HorarioAtendimentoTO gravarHorario(Connection conexao, HorarioAtendimentoTO horario,
                                               boolean atualizacao) {
        Long idHorarioAtual = atualizacao ? horario.getIdHorario() : null;
        if (atualizacao) {
            if (horarioDAO.buscarPorIdParaAtualizar(conexao, idHorarioAtual) == null) {
                return null;
            }
            Boolean possuiAgendamento = agendamentoDAO.existeAgendamentoPorHorario(conexao, idHorarioAtual);
            if (possuiAgendamento == null) {
                return null;
            }
            if (possuiAgendamento) {
                throw new RegraNegocioException("Um horário vinculado a agendamento não pode ser alterado.");
            }
            Boolean reservado = propostaDAO.existeReservaAtivaPorHorario(conexao, idHorarioAtual,
                    LocalDateTime.now(ZONA_NEGOCIO).withNano(0), null);
            if (reservado == null) {
                return null;
            }
            if (reservado) {
                throw new RegraNegocioException("Um horário com reserva vigente não pode ser alterado.");
            }
        }

        MembroEquipeTO membroEquipe = membroEquipeDAO.buscarPorIdParaAtualizar(conexao, horario.getIdMembroEquipe());
        if (membroEquipe == null) {
            return null;
        }
        if (!"S".equals(membroEquipe.getAtendimento())) {
            throw new RegraNegocioException("O membro da equipe não tem atribuição de atendimento.");
        }

        Boolean existeSobreposicao = horarioDAO.existeSobreposicao(conexao, horario, idHorarioAtual);
        if (existeSobreposicao == null) {
            return null;
        }
        if (existeSobreposicao) {
            throw new RegraNegocioException("O membro da equipe já possui um horário nesse intervalo.");
        }

        if (atualizacao) {
            return horarioDAO.atualizar(conexao, horario);
        }
        return horarioDAO.inserir(conexao, horario);
    }
}
