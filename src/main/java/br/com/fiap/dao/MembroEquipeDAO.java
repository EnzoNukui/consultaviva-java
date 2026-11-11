package br.com.fiap.dao;

import br.com.fiap.to.MembroEquipeTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MembroEquipeDAO {

    public ArrayList<MembroEquipeTO> listarTodos() {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<MembroEquipeTO> registros = new ArrayList<>();
        String sql = "select * from tb_membro_equipe order by CD_MEMBRO_EQUIPE";
        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                MembroEquipeTO membroEquipe = new MembroEquipeTO();
                membroEquipe.setIdMembroEquipe(rs.getLong("CD_MEMBRO_EQUIPE"));
                membroEquipe.setNomeMembroEquipe(rs.getString("NM_MEMBRO_EQUIPE"));
                membroEquipe.setCargo(rs.getString("DS_CARGO"));
                membroEquipe.setAtendimento(rs.getString("IN_ATENDIMENTO"));
                membroEquipe.setApoioAdministrativo(rs.getString("IN_APOIO_ADMINISTRATIVO"));
                membroEquipe.setAutorizaModalidade(rs.getString("IN_AUTORIZA_MODALIDADE"));
                membroEquipe.setRegistroProfissional(rs.getString("NR_REGISTRO_PROFISSIONAL"));
                registros.add(membroEquipe);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar membros da equipe: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    public MembroEquipeTO buscarPorId(Long idMembroEquipe) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "select * from tb_membro_equipe where CD_MEMBRO_EQUIPE = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idMembroEquipe);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MembroEquipeTO membroEquipe = new MembroEquipeTO();
                    membroEquipe.setIdMembroEquipe(rs.getLong("CD_MEMBRO_EQUIPE"));
                    membroEquipe.setNomeMembroEquipe(rs.getString("NM_MEMBRO_EQUIPE"));
                    membroEquipe.setCargo(rs.getString("DS_CARGO"));
                    membroEquipe.setAtendimento(rs.getString("IN_ATENDIMENTO"));
                    membroEquipe.setApoioAdministrativo(rs.getString("IN_APOIO_ADMINISTRATIVO"));
                    membroEquipe.setAutorizaModalidade(rs.getString("IN_AUTORIZA_MODALIDADE"));
                    membroEquipe.setRegistroProfissional(rs.getString("NR_REGISTRO_PROFISSIONAL"));
                    return membroEquipe;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar membro da equipe: " + e.getMessage());
        } finally {
            ConnectionFactory.closeConnection();
        }
        return null;
    }

    // A BO bloqueia o membro durante a verificação e a gravação de horários.
    public MembroEquipeTO buscarPorIdParaAtualizar(Connection conexao, Long idMembroEquipe) {
        try {
            if (conexao.getAutoCommit()) {
                System.out.println("O bloqueio do registro exige uma transação com autoCommit desativado.");
                return null;
            }
            String sql = "select * from tb_membro_equipe where CD_MEMBRO_EQUIPE = ? for update";
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setLong(1, idMembroEquipe);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        MembroEquipeTO membroEquipe = new MembroEquipeTO();
                        membroEquipe.setIdMembroEquipe(rs.getLong("CD_MEMBRO_EQUIPE"));
                        membroEquipe.setNomeMembroEquipe(rs.getString("NM_MEMBRO_EQUIPE"));
                        membroEquipe.setCargo(rs.getString("DS_CARGO"));
                        membroEquipe.setAtendimento(rs.getString("IN_ATENDIMENTO"));
                        membroEquipe.setApoioAdministrativo(rs.getString("IN_APOIO_ADMINISTRATIVO"));
                        membroEquipe.setAutorizaModalidade(rs.getString("IN_AUTORIZA_MODALIDADE"));
                        membroEquipe.setRegistroProfissional(rs.getString("NR_REGISTRO_PROFISSIONAL"));
                        return membroEquipe;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao bloquear membro da equipe: " + e.getMessage());
        }
        return null;
    }

    public MembroEquipeTO inserir(MembroEquipeTO membroEquipe) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "insert into tb_membro_equipe (NM_MEMBRO_EQUIPE, DS_CARGO, IN_ATENDIMENTO, "
                + "IN_APOIO_ADMINISTRATIVO, IN_AUTORIZA_MODALIDADE, NR_REGISTRO_PROFISSIONAL) values "
                + "(?, ?, ?, ?, ?, ?)";
        // A coluna identity é omitida do INSERT e solicitada explicitamente ao driver Oracle.
        try (PreparedStatement ps = conexao.prepareStatement(sql, new String[]{"CD_MEMBRO_EQUIPE"})) {
            ps.setString(1, membroEquipe.getNomeMembroEquipe());
            ps.setString(2, membroEquipe.getCargo());
            ps.setString(3, membroEquipe.getAtendimento());
            ps.setString(4, membroEquipe.getApoioAdministrativo());
            ps.setString(5, membroEquipe.getAutorizaModalidade());
            ps.setString(6, membroEquipe.getRegistroProfissional());
            if (ps.executeUpdate() == 0) {
                return null;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    System.out.println("O Oracle não retornou o identificador de tb_membro_equipe.");
                    return null;
                }
                long idGerado = rs.getLong(1);
                if (rs.wasNull()) {
                    System.out.println("O Oracle retornou um identificador nulo para tb_membro_equipe.");
                    return null;
                }
                membroEquipe.setIdMembroEquipe(idGerado);
            }
            return membroEquipe;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir membro da equipe: " + e.getMessage());
            return null;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public MembroEquipeTO atualizar(MembroEquipeTO membroEquipe) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "update tb_membro_equipe set NM_MEMBRO_EQUIPE = ?, DS_CARGO = ?, IN_ATENDIMENTO = ?, "
                + "IN_APOIO_ADMINISTRATIVO = ?, IN_AUTORIZA_MODALIDADE = ?, NR_REGISTRO_PROFISSIONAL = "
                + "? where CD_MEMBRO_EQUIPE = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, membroEquipe.getNomeMembroEquipe());
            ps.setString(2, membroEquipe.getCargo());
            ps.setString(3, membroEquipe.getAtendimento());
            ps.setString(4, membroEquipe.getApoioAdministrativo());
            ps.setString(5, membroEquipe.getAutorizaModalidade());
            ps.setString(6, membroEquipe.getRegistroProfissional());
            ps.setLong(7, membroEquipe.getIdMembroEquipe());
            return ps.executeUpdate() > 0 ? membroEquipe : null;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar membro da equipe: " + e.getMessage());
            return null;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public boolean excluir(Long idMembroEquipe) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return false;
        }
        String sql = "delete from tb_membro_equipe where CD_MEMBRO_EQUIPE = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idMembroEquipe);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir membro da equipe: " + e.getMessage());
            return false;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }
}

