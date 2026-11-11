package br.com.fiap.dao;

import br.com.fiap.to.TipoDificuldadeTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class TipoDificuldadeDAO {

    public ArrayList<TipoDificuldadeTO> listarTodos() {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<TipoDificuldadeTO> registros = new ArrayList<>();
        String sql = "select * from tb_tipo_dificuldade order by CD_DIFICULDADE";
        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TipoDificuldadeTO tipoDificuldade = new TipoDificuldadeTO();
                tipoDificuldade.setIdDificuldade(rs.getLong("CD_DIFICULDADE"));
                tipoDificuldade.setNomeDificuldade(rs.getString("NM_DIFICULDADE"));
                registros.add(tipoDificuldade);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar tipos de dificuldade: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    public TipoDificuldadeTO buscarPorId(Long idDificuldade) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "select * from tb_tipo_dificuldade where CD_DIFICULDADE = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idDificuldade);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TipoDificuldadeTO tipoDificuldade = new TipoDificuldadeTO();
                    tipoDificuldade.setIdDificuldade(rs.getLong("CD_DIFICULDADE"));
                    tipoDificuldade.setNomeDificuldade(rs.getString("NM_DIFICULDADE"));
                    return tipoDificuldade;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar tipo de dificuldade: " + e.getMessage());
        } finally {
            ConnectionFactory.closeConnection();
        }
        return null;
    }

    public TipoDificuldadeTO inserir(TipoDificuldadeTO tipoDificuldade) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "insert into tb_tipo_dificuldade (NM_DIFICULDADE) values (?)";
        // A coluna identity é omitida do INSERT e solicitada explicitamente ao driver Oracle.
        try (PreparedStatement ps = conexao.prepareStatement(sql, new String[]{"CD_DIFICULDADE"})) {
            ps.setString(1, tipoDificuldade.getNomeDificuldade());
            if (ps.executeUpdate() == 0) {
                return null;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    System.out.println("O Oracle não retornou o identificador de tb_tipo_dificuldade.");
                    return null;
                }
                long idGerado = rs.getLong(1);
                if (rs.wasNull()) {
                    System.out.println("O Oracle retornou um identificador nulo para tb_tipo_dificuldade.");
                    return null;
                }
                tipoDificuldade.setIdDificuldade(idGerado);
            }
            return tipoDificuldade;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir tipo de dificuldade: " + e.getMessage());
            return null;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public TipoDificuldadeTO atualizar(TipoDificuldadeTO tipoDificuldade) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "update tb_tipo_dificuldade set NM_DIFICULDADE = ? where CD_DIFICULDADE = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, tipoDificuldade.getNomeDificuldade());
            ps.setLong(2, tipoDificuldade.getIdDificuldade());
            return ps.executeUpdate() > 0 ? tipoDificuldade : null;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar tipo de dificuldade: " + e.getMessage());
            return null;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public boolean excluir(Long idDificuldade) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return false;
        }
        String sql = "delete from tb_tipo_dificuldade where CD_DIFICULDADE = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idDificuldade);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir tipo de dificuldade: " + e.getMessage());
            return false;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }
}

