package br.com.fiap.dao;

import br.com.fiap.to.UnidadeTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class UnidadeDAO {

    public ArrayList<UnidadeTO> listarTodos() {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<UnidadeTO> registros = new ArrayList<>();
        String sql = "select * from tb_unidade order by CD_UNIDADE";
        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                registros.add(lerUnidade(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar unidades: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    public UnidadeTO buscarPorId(Long idUnidade) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        try {
            return buscarPorId(conexao, idUnidade);
        } catch (SQLException e) {
            System.out.println("Erro ao buscar unidade: " + e.getMessage());
        } finally {
            ConnectionFactory.closeConnection();
        }
        return null;
    }

    private UnidadeTO buscarPorId(Connection conexao, Long idUnidade) throws SQLException {
        String sql = "select * from tb_unidade where CD_UNIDADE = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idUnidade);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? lerUnidade(rs) : null;
            }
        }
    }

    private UnidadeTO lerUnidade(ResultSet rs) throws SQLException {
        UnidadeTO unidade = new UnidadeTO(rs.getLong("CD_UNIDADE"),
                rs.getString("NM_UNIDADE"), rs.getString("DS_ENDERECO"));
        unidade.setCodigoCnes(rs.getString("CD_CNES"));
        return unidade;
    }

    public UnidadeTO inserir(UnidadeTO unidade) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "insert into tb_unidade (NM_UNIDADE, DS_ENDERECO, CD_CNES) values (?, ?, ?)";
        // A coluna identity é omitida do INSERT e solicitada explicitamente ao driver Oracle.
        try (PreparedStatement ps = conexao.prepareStatement(sql, new String[]{"CD_UNIDADE"})) {
            ps.setString(1, unidade.getNomeUnidade());
            ps.setString(2, unidade.getEndereco());
            ps.setString(3, unidade.getCodigoCnes());
            if (ps.executeUpdate() == 0) {
                return null;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    System.out.println("O Oracle não retornou o identificador de tb_unidade.");
                    return null;
                }
                long idGerado = rs.getLong(1);
                if (rs.wasNull()) {
                    System.out.println("O Oracle retornou um identificador nulo para tb_unidade.");
                    return null;
                }
                unidade.setIdUnidade(idGerado);
            }
            return unidade;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir unidade: " + e.getMessage());
            return null;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public UnidadeTO atualizar(UnidadeTO unidade) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "update tb_unidade set NM_UNIDADE = ?, DS_ENDERECO = ?, CD_CNES = COALESCE(?, CD_CNES) "
                + "where CD_UNIDADE = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, unidade.getNomeUnidade());
            ps.setString(2, unidade.getEndereco());
            ps.setString(3, unidade.getCodigoCnes());
            ps.setLong(4, unidade.getIdUnidade());
            if (ps.executeUpdate() == 0) return null;
            return unidade.getCodigoCnes() == null ? buscarPorId(conexao, unidade.getIdUnidade()) : unidade;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar unidade: " + e.getMessage());
            return null;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public boolean excluir(Long idUnidade) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return false;
        }
        String sql = "delete from tb_unidade where CD_UNIDADE = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idUnidade);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir unidade: " + e.getMessage());
            return false;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }
}

