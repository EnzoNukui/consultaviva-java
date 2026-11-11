package br.com.fiap.dao;

import br.com.fiap.to.SolicitacaoDificuldadeTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SolicitacaoDificuldadeDAO {

    public ArrayList<SolicitacaoDificuldadeTO> listarTodos() {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<SolicitacaoDificuldadeTO> registros = new ArrayList<>();
        String sql = "select * from tb_solicitacao_dificuldade order by CD_SOLICITACAO, CD_DIFICULDADE";
        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SolicitacaoDificuldadeTO vinculo = new SolicitacaoDificuldadeTO();
                vinculo.setIdSolicitacao(rs.getLong("CD_SOLICITACAO"));
                vinculo.setIdDificuldade(rs.getLong("CD_DIFICULDADE"));
                registros.add(vinculo);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar vínculos: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    public SolicitacaoDificuldadeTO buscarPorIds(Long idSolicitacao, Long idDificuldade) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "select * from tb_solicitacao_dificuldade where CD_SOLICITACAO = ? and CD_DIFICULDADE = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idSolicitacao);
            ps.setLong(2, idDificuldade);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SolicitacaoDificuldadeTO vinculo = new SolicitacaoDificuldadeTO();
                    vinculo.setIdSolicitacao(rs.getLong("CD_SOLICITACAO"));
                    vinculo.setIdDificuldade(rs.getLong("CD_DIFICULDADE"));
                    return vinculo;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar vínculo: " + e.getMessage());
        } finally {
            ConnectionFactory.closeConnection();
        }
        return null;
    }

    public SolicitacaoDificuldadeTO inserir(Connection conexao, SolicitacaoDificuldadeTO vinculo) {
        String sql = "insert into tb_solicitacao_dificuldade (CD_SOLICITACAO, CD_DIFICULDADE) values (?, ?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, vinculo.getIdSolicitacao());
            ps.setLong(2, vinculo.getIdDificuldade());
            return ps.executeUpdate() > 0 ? vinculo : null;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir vínculo: " + e.getMessage());
            return null;
        }
    }

    public boolean excluir(Long idSolicitacao, Long idDificuldade) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return false;
        }
        String sql = "delete from tb_solicitacao_dificuldade where CD_SOLICITACAO = ? and CD_DIFICULDADE = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idSolicitacao);
            ps.setLong(2, idDificuldade);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir vínculo: " + e.getMessage());
            return false;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public SolicitacaoDificuldadeTO buscarPorIds(Connection conexao, Long idSolicitacao, Long idDificuldade) {
        String sql = "select * from tb_solicitacao_dificuldade where CD_SOLICITACAO = ? and CD_DIFICULDADE = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idSolicitacao);
            ps.setLong(2, idDificuldade);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SolicitacaoDificuldadeTO vinculo = new SolicitacaoDificuldadeTO();
                    vinculo.setIdSolicitacao(rs.getLong("CD_SOLICITACAO"));
                    vinculo.setIdDificuldade(rs.getLong("CD_DIFICULDADE"));
                    return vinculo;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar vínculo na solicitação: " + e.getMessage());
        }
        return null;
    }

    public boolean excluir(Connection conexao, Long idSolicitacao, Long idDificuldade) {
        String sql = "delete from tb_solicitacao_dificuldade where CD_SOLICITACAO = ? and CD_DIFICULDADE = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idSolicitacao);
            ps.setLong(2, idDificuldade);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir vínculo da solicitação: " + e.getMessage());
            return false;
        }
    }

    public ArrayList<SolicitacaoDificuldadeTO> listarPorSolicitacao(Long idSolicitacao) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<SolicitacaoDificuldadeTO> registros = new ArrayList<>();
        String sql = "select * from tb_solicitacao_dificuldade where CD_SOLICITACAO = ? order by CD_SOLICITACAO, "
                + "CD_DIFICULDADE";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idSolicitacao);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SolicitacaoDificuldadeTO vinculo = new SolicitacaoDificuldadeTO();
                    vinculo.setIdSolicitacao(rs.getLong("CD_SOLICITACAO"));
                    vinculo.setIdDificuldade(rs.getLong("CD_DIFICULDADE"));
                    registros.add(vinculo);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar vínculos por solicitação: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    public ArrayList<SolicitacaoDificuldadeTO> listarPorDificuldade(Long idDificuldade) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<SolicitacaoDificuldadeTO> registros = new ArrayList<>();
        String sql = "select * from tb_solicitacao_dificuldade where CD_DIFICULDADE = ? order by CD_SOLICITACAO, "
                + "CD_DIFICULDADE";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idDificuldade);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SolicitacaoDificuldadeTO vinculo = new SolicitacaoDificuldadeTO();
                    vinculo.setIdSolicitacao(rs.getLong("CD_SOLICITACAO"));
                    vinculo.setIdDificuldade(rs.getLong("CD_DIFICULDADE"));
                    registros.add(vinculo);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar vínculos por tipo de dificuldade: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }
}
