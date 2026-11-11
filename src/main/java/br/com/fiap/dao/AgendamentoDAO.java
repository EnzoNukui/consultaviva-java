package br.com.fiap.dao;

import br.com.fiap.to.AgendamentoTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class AgendamentoDAO {

    public ArrayList<AgendamentoTO> listarTodos() {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<AgendamentoTO> registros = new ArrayList<>();
        String sql = "select * from tb_agendamento order by CD_AGENDAMENTO";
        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                AgendamentoTO agendamento = new AgendamentoTO();
                agendamento.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                agendamento.setModalidade(rs.getString("DS_MODALIDADE"));
                agendamento.setStatusAgendamento(rs.getString("ST_AGENDAMENTO"));
                agendamento.setStatusRespostaPaciente(rs.getString("ST_RESPOSTA_PACIENTE"));
                agendamento.setIdPaciente(rs.getLong("CD_PACIENTE"));
                agendamento.setIdHorario(rs.getLong("CD_HORARIO"));
                registros.add(agendamento);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar agendamentos: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    public AgendamentoTO buscarPorId(Long idAgendamento) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "select * from tb_agendamento where CD_AGENDAMENTO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idAgendamento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AgendamentoTO agendamento = new AgendamentoTO();
                    agendamento.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                    agendamento.setModalidade(rs.getString("DS_MODALIDADE"));
                    agendamento.setStatusAgendamento(rs.getString("ST_AGENDAMENTO"));
                    agendamento.setStatusRespostaPaciente(rs.getString("ST_RESPOSTA_PACIENTE"));
                    agendamento.setIdPaciente(rs.getLong("CD_PACIENTE"));
                    agendamento.setIdHorario(rs.getLong("CD_HORARIO"));
                    return agendamento;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar agendamento: " + e.getMessage());
        } finally {
            ConnectionFactory.closeConnection();
        }
        return null;
    }

    // A BO mantém a transação aberta até concluir a operação que depende deste bloqueio.
    public AgendamentoTO buscarPorIdParaAtualizar(Connection conexao, Long idAgendamento) {
        try {
            if (conexao.getAutoCommit()) {
                System.out.println("O bloqueio do registro exige uma transação com autoCommit desativado.");
                return null;
            }
            String sql = "select * from tb_agendamento where CD_AGENDAMENTO = ? for update";
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setLong(1, idAgendamento);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        AgendamentoTO agendamento = new AgendamentoTO();
                        agendamento.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                        agendamento.setModalidade(rs.getString("DS_MODALIDADE"));
                        agendamento.setStatusAgendamento(rs.getString("ST_AGENDAMENTO"));
                        agendamento.setStatusRespostaPaciente(rs.getString("ST_RESPOSTA_PACIENTE"));
                        agendamento.setIdPaciente(rs.getLong("CD_PACIENTE"));
                        agendamento.setIdHorario(rs.getLong("CD_HORARIO"));
                        return agendamento;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao bloquear agendamento: " + e.getMessage());
        }
        return null;
    }

    public AgendamentoTO inserir(Connection conexao, AgendamentoTO agendamento) {
        String sql = "insert into tb_agendamento (DS_MODALIDADE, ST_AGENDAMENTO, ST_RESPOSTA_PACIENTE, "
                + "CD_PACIENTE, CD_HORARIO) values (?, ?, ?, ?, ?)";
        // A coluna identity é omitida do INSERT e solicitada explicitamente ao driver Oracle.
        try (PreparedStatement ps = conexao.prepareStatement(sql, new String[]{"CD_AGENDAMENTO"})) {
            ps.setString(1, agendamento.getModalidade());
            ps.setString(2, agendamento.getStatusAgendamento());
            ps.setString(3, agendamento.getStatusRespostaPaciente());
            ps.setLong(4, agendamento.getIdPaciente());
            ps.setLong(5, agendamento.getIdHorario());
            if (ps.executeUpdate() == 0) {
                return null;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    System.out.println("O Oracle não retornou o identificador de tb_agendamento.");
                    return null;
                }
                long idGerado = rs.getLong(1);
                if (rs.wasNull()) {
                    System.out.println("O Oracle retornou um identificador nulo para tb_agendamento.");
                    return null;
                }
                agendamento.setIdAgendamento(idGerado);
            }
            return agendamento;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir agendamento: " + e.getMessage());
            return null;
        }
    }

    public AgendamentoTO atualizar(Connection conexao, AgendamentoTO agendamento) {
        String sql = "update tb_agendamento set DS_MODALIDADE = ?, ST_AGENDAMENTO = ?, "
                + "ST_RESPOSTA_PACIENTE = ?, CD_PACIENTE = ?, CD_HORARIO = ? where CD_AGENDAMENTO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, agendamento.getModalidade());
            ps.setString(2, agendamento.getStatusAgendamento());
            ps.setString(3, agendamento.getStatusRespostaPaciente());
            ps.setLong(4, agendamento.getIdPaciente());
            ps.setLong(5, agendamento.getIdHorario());
            ps.setLong(6, agendamento.getIdAgendamento());
            return ps.executeUpdate() > 0 ? agendamento : null;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar agendamento: " + e.getMessage());
            return null;
        }
    }

    public boolean excluir(Long idAgendamento) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return false;
        }
        String sql = "delete from tb_agendamento where CD_AGENDAMENTO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idAgendamento);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir agendamento: " + e.getMessage());
            return false;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public ArrayList<AgendamentoTO> listarPorPaciente(Long idPaciente) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<AgendamentoTO> registros = new ArrayList<>();
        String sql = "select * from tb_agendamento where CD_PACIENTE = ? order by CD_AGENDAMENTO";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idPaciente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AgendamentoTO agendamento = new AgendamentoTO();
                    agendamento.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                    agendamento.setModalidade(rs.getString("DS_MODALIDADE"));
                    agendamento.setStatusAgendamento(rs.getString("ST_AGENDAMENTO"));
                    agendamento.setStatusRespostaPaciente(rs.getString("ST_RESPOSTA_PACIENTE"));
                    agendamento.setIdPaciente(rs.getLong("CD_PACIENTE"));
                    agendamento.setIdHorario(rs.getLong("CD_HORARIO"));
                    registros.add(agendamento);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar agendamentos por paciente: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    public ArrayList<AgendamentoTO> listarPorHorario(Long idHorario) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<AgendamentoTO> registros = new ArrayList<>();
        String sql = "select * from tb_agendamento where CD_HORARIO = ? order by CD_AGENDAMENTO";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idHorario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AgendamentoTO agendamento = new AgendamentoTO();
                    agendamento.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                    agendamento.setModalidade(rs.getString("DS_MODALIDADE"));
                    agendamento.setStatusAgendamento(rs.getString("ST_AGENDAMENTO"));
                    agendamento.setStatusRespostaPaciente(rs.getString("ST_RESPOSTA_PACIENTE"));
                    agendamento.setIdPaciente(rs.getLong("CD_PACIENTE"));
                    agendamento.setIdHorario(rs.getLong("CD_HORARIO"));
                    registros.add(agendamento);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar agendamentos por horário: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    // Consulta os vínculos em qualquer estado. Null indica falha na consulta.
    public Boolean existeAgendamentoPorHorario(Connection conexao, Long idHorario) {
        String sql = "select 1 from tb_agendamento where CD_HORARIO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idHorario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar os agendamentos do horário: " + e.getMessage());
            return null;
        }
    }

    // A verificação de ocupação deve ocorrer após bloquear o horário na mesma conexão.
    public boolean existeAgendamentoAtivoPorHorario(Connection conexao, Long idHorario,
                                                    Long idAgendamentoIgnorado) {
        String sql = "select count(*) from tb_agendamento where CD_HORARIO = ? and ST_AGENDAMENTO = "
                + "'AGENDADO'";
        if (idAgendamentoIgnorado != null) {
            sql += " and CD_AGENDAMENTO <> ?";
        }
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idHorario);
            if (idAgendamentoIgnorado != null) {
                ps.setLong(2, idAgendamentoIgnorado);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar a ocupação do horário: " + e.getMessage());
            // Impede um novo cadastro quando não foi possível verificar o conflito.
            return true;
        }
    }
}

