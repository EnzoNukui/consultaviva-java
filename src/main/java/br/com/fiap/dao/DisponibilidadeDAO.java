package br.com.fiap.dao;

import br.com.fiap.to.DisponibilidadeTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class DisponibilidadeDAO {

    public ArrayList<DisponibilidadeTO> listarTodos() {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<DisponibilidadeTO> registros = new ArrayList<>();
        String sql = "select * from tb_disponibilidade order by CD_DISPONIBILIDADE";
        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DisponibilidadeTO disponibilidade = new DisponibilidadeTO();
                disponibilidade.setIdDisponibilidade(rs.getLong("CD_DISPONIBILIDADE"));
                Timestamp dataInicio = rs.getTimestamp("DT_INICIO");
                disponibilidade.setDataInicio(dataInicio == null ? null : dataInicio.toLocalDateTime());
                Timestamp dataFim = rs.getTimestamp("DT_FIM");
                disponibilidade.setDataFim(dataFim == null ? null : dataFim.toLocalDateTime());
                disponibilidade.setModalidade(rs.getString("DS_MODALIDADE"));
                disponibilidade.setIdSolicitacao(rs.getLong("CD_SOLICITACAO"));
                registros.add(disponibilidade);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar disponibilidades: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    public DisponibilidadeTO buscarPorId(Long idDisponibilidade) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "select * from tb_disponibilidade where CD_DISPONIBILIDADE = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idDisponibilidade);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DisponibilidadeTO disponibilidade = new DisponibilidadeTO();
                    disponibilidade.setIdDisponibilidade(rs.getLong("CD_DISPONIBILIDADE"));
                    Timestamp dataInicio = rs.getTimestamp("DT_INICIO");
                    disponibilidade.setDataInicio(dataInicio == null ? null : dataInicio.toLocalDateTime());
                    Timestamp dataFim = rs.getTimestamp("DT_FIM");
                    disponibilidade.setDataFim(dataFim == null ? null : dataFim.toLocalDateTime());
                    disponibilidade.setModalidade(rs.getString("DS_MODALIDADE"));
                    disponibilidade.setIdSolicitacao(rs.getLong("CD_SOLICITACAO"));
                    return disponibilidade;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar disponibilidade: " + e.getMessage());
        } finally {
            ConnectionFactory.closeConnection();
        }
        return null;
    }

    public DisponibilidadeTO inserir(Connection conexao, DisponibilidadeTO disponibilidade) {
        String sql = "insert into tb_disponibilidade (DT_INICIO, DT_FIM, DS_MODALIDADE, CD_SOLICITACAO) values "
                + "(?, ?, ?, ?)";
        // A coluna identity é omitida do INSERT e solicitada explicitamente ao driver Oracle.
        try (PreparedStatement ps = conexao.prepareStatement(sql, new String[]{"CD_DISPONIBILIDADE"})) {
            ps.setTimestamp(1, disponibilidade.getDataInicio() == null
                    ? null : Timestamp.valueOf(disponibilidade.getDataInicio()));
            ps.setTimestamp(2, disponibilidade.getDataFim() == null
                    ? null : Timestamp.valueOf(disponibilidade.getDataFim()));
            ps.setString(3, disponibilidade.getModalidade());
            ps.setLong(4, disponibilidade.getIdSolicitacao());
            if (ps.executeUpdate() == 0) {
                return null;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    System.out.println("O Oracle não retornou o identificador de tb_disponibilidade.");
                    return null;
                }
                long idGerado = rs.getLong(1);
                if (rs.wasNull()) {
                    System.out.println("O Oracle retornou um identificador nulo para tb_disponibilidade.");
                    return null;
                }
                disponibilidade.setIdDisponibilidade(idGerado);
            }
            return disponibilidade;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir disponibilidade: " + e.getMessage());
            return null;
        }
    }

    public DisponibilidadeTO buscarPorId(Connection conexao, Long idDisponibilidade, Long idSolicitacao) {
        String sql = "select * from tb_disponibilidade where CD_DISPONIBILIDADE = ? and CD_SOLICITACAO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idDisponibilidade);
            ps.setLong(2, idSolicitacao);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DisponibilidadeTO disponibilidade = new DisponibilidadeTO();
                    disponibilidade.setIdDisponibilidade(rs.getLong("CD_DISPONIBILIDADE"));
                    Timestamp dataInicio = rs.getTimestamp("DT_INICIO");
                    disponibilidade.setDataInicio(dataInicio == null ? null : dataInicio.toLocalDateTime());
                    Timestamp dataFim = rs.getTimestamp("DT_FIM");
                    disponibilidade.setDataFim(dataFim == null ? null : dataFim.toLocalDateTime());
                    disponibilidade.setModalidade(rs.getString("DS_MODALIDADE"));
                    disponibilidade.setIdSolicitacao(rs.getLong("CD_SOLICITACAO"));
                    return disponibilidade;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar disponibilidade da solicitação: " + e.getMessage());
        }
        return null;
    }

    public DisponibilidadeTO atualizar(Connection conexao, DisponibilidadeTO disponibilidade) {
        String sql = "update tb_disponibilidade set DT_INICIO = ?, DT_FIM = ?, DS_MODALIDADE = ? "
                + "where CD_DISPONIBILIDADE = ? and CD_SOLICITACAO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(disponibilidade.getDataInicio()));
            ps.setTimestamp(2, Timestamp.valueOf(disponibilidade.getDataFim()));
            ps.setString(3, disponibilidade.getModalidade());
            ps.setLong(4, disponibilidade.getIdDisponibilidade());
            ps.setLong(5, disponibilidade.getIdSolicitacao());
            return ps.executeUpdate() > 0 ? disponibilidade : null;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar disponibilidade da solicitação: " + e.getMessage());
            return null;
        }
    }

    public boolean excluir(Connection conexao, Long idDisponibilidade, Long idSolicitacao) {
        String sql = "delete from tb_disponibilidade where CD_DISPONIBILIDADE = ? and CD_SOLICITACAO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idDisponibilidade);
            ps.setLong(2, idSolicitacao);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir disponibilidade da solicitação: " + e.getMessage());
            return false;
        }
    }

    public DisponibilidadeTO atualizar(DisponibilidadeTO disponibilidade) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "update tb_disponibilidade set DT_INICIO = ?, DT_FIM = ?, DS_MODALIDADE = ?, CD_SOLICITACAO "
                + "= ? where CD_DISPONIBILIDADE = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setTimestamp(1, disponibilidade.getDataInicio() == null
                    ? null : Timestamp.valueOf(disponibilidade.getDataInicio()));
            ps.setTimestamp(2, disponibilidade.getDataFim() == null
                    ? null : Timestamp.valueOf(disponibilidade.getDataFim()));
            ps.setString(3, disponibilidade.getModalidade());
            ps.setLong(4, disponibilidade.getIdSolicitacao());
            ps.setLong(5, disponibilidade.getIdDisponibilidade());
            return ps.executeUpdate() > 0 ? disponibilidade : null;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar disponibilidade: " + e.getMessage());
            return null;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public boolean excluir(Long idDisponibilidade) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return false;
        }
        String sql = "delete from tb_disponibilidade where CD_DISPONIBILIDADE = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idDisponibilidade);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir disponibilidade: " + e.getMessage());
            return false;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public ArrayList<DisponibilidadeTO> listarPorSolicitacao(Connection conexao, Long idSolicitacao) {
        ArrayList<DisponibilidadeTO> registros = new ArrayList<>();
        String sql = "select * from tb_disponibilidade where CD_SOLICITACAO = ? order by CD_DISPONIBILIDADE";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idSolicitacao);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DisponibilidadeTO disponibilidade = new DisponibilidadeTO();
                    disponibilidade.setIdDisponibilidade(rs.getLong("CD_DISPONIBILIDADE"));
                    Timestamp dataInicio = rs.getTimestamp("DT_INICIO");
                    disponibilidade.setDataInicio(dataInicio == null ? null : dataInicio.toLocalDateTime());
                    Timestamp dataFim = rs.getTimestamp("DT_FIM");
                    disponibilidade.setDataFim(dataFim == null ? null : dataFim.toLocalDateTime());
                    disponibilidade.setModalidade(rs.getString("DS_MODALIDADE"));
                    disponibilidade.setIdSolicitacao(rs.getLong("CD_SOLICITACAO"));
                    registros.add(disponibilidade);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar disponibilidades por solicitação: " + e.getMessage());
            return null;
        }
        return registros;
    }
}
