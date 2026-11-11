package br.com.fiap.dao;

import br.com.fiap.to.PacienteTO;
import br.com.fiap.to.CadastroPacienteTO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PacienteDAO {

    public boolean emailEmUso(String email) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return true;
        }
        String sql = "select (select count(*) from tb_paciente where upper(DS_EMAIL) = upper(?)) + "
                + "(select count(*) from tb_membro_equipe where upper(DS_EMAIL) = upper(?)) from dual";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ps.setString(2, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getLong(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar e-mail de cadastro: " + e.getMessage());
            return true;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public PacienteTO cadastrar(CadastroPacienteTO cadastro, String hashSenha) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "insert into tb_paciente (NM_PACIENTE, DT_NASCIMENTO, NR_TELEFONE, DS_EMAIL, DS_HASH_SENHA) "
                + "values (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql, new String[]{"CD_PACIENTE"})) {
            ps.setString(1, cadastro.getNomePaciente());
            ps.setDate(2, cadastro.getDataNascimento() == null ? null : Date.valueOf(cadastro.getDataNascimento()));
            ps.setString(3, cadastro.getTelefone());
            ps.setString(4, cadastro.getEmail().trim());
            ps.setString(5, hashSenha);
            if (ps.executeUpdate() == 0) {
                return null;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    return null;
                }
                long idGerado = rs.getLong(1);
                if (rs.wasNull()) {
                    return null;
                }
                return new PacienteTO(idGerado, cadastro.getNomePaciente(), cadastro.getDataNascimento(),
                        cadastro.getTelefone(), cadastro.getEmail().trim());
            }
        } catch (SQLException e) {
            System.out.println("Erro ao cadastrar conta do paciente: " + e.getMessage());
            return null;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public ArrayList<PacienteTO> listarTodos() {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<PacienteTO> registros = new ArrayList<>();
        String sql = "select * from tb_paciente order by CD_PACIENTE";
        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PacienteTO paciente = new PacienteTO();
                paciente.setIdPaciente(rs.getLong("CD_PACIENTE"));
                paciente.setNomePaciente(rs.getString("NM_PACIENTE"));
                Date dataNascimento = rs.getDate("DT_NASCIMENTO");
                paciente.setDataNascimento(dataNascimento == null ? null : dataNascimento.toLocalDate());
                paciente.setTelefone(rs.getString("NR_TELEFONE"));
                registros.add(paciente);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar pacientes: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    public PacienteTO buscarPorId(Long idPaciente) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "select * from tb_paciente where CD_PACIENTE = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idPaciente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PacienteTO paciente = new PacienteTO();
                    paciente.setIdPaciente(rs.getLong("CD_PACIENTE"));
                    paciente.setNomePaciente(rs.getString("NM_PACIENTE"));
                    Date dataNascimento = rs.getDate("DT_NASCIMENTO");
                    paciente.setDataNascimento(dataNascimento == null ? null : dataNascimento.toLocalDate());
                    paciente.setTelefone(rs.getString("NR_TELEFONE"));
                    return paciente;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar paciente: " + e.getMessage());
        } finally {
            ConnectionFactory.closeConnection();
        }
        return null;
    }

    public PacienteTO inserir(PacienteTO paciente) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "insert into tb_paciente (NM_PACIENTE, DT_NASCIMENTO, NR_TELEFONE) values (?, ?, ?)";
        // A coluna identity é omitida do INSERT e solicitada explicitamente ao driver Oracle.
        try (PreparedStatement ps = conexao.prepareStatement(sql, new String[]{"CD_PACIENTE"})) {
            ps.setString(1, paciente.getNomePaciente());
            ps.setDate(2, paciente.getDataNascimento() == null ? null : Date.valueOf(paciente.getDataNascimento()));
            ps.setString(3, paciente.getTelefone());
            if (ps.executeUpdate() == 0) {
                return null;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    System.out.println("O Oracle não retornou o identificador de tb_paciente.");
                    return null;
                }
                long idGerado = rs.getLong(1);
                if (rs.wasNull()) {
                    System.out.println("O Oracle retornou um identificador nulo para tb_paciente.");
                    return null;
                }
                paciente.setIdPaciente(idGerado);
            }
            return paciente;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir paciente: " + e.getMessage());
            return null;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public PacienteTO atualizar(PacienteTO paciente) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "update tb_paciente set NM_PACIENTE = ?, DT_NASCIMENTO = ?, NR_TELEFONE = ? where "
                + "CD_PACIENTE = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, paciente.getNomePaciente());
            ps.setDate(2, paciente.getDataNascimento() == null ? null : Date.valueOf(paciente.getDataNascimento()));
            ps.setString(3, paciente.getTelefone());
            ps.setLong(4, paciente.getIdPaciente());
            return ps.executeUpdate() > 0 ? paciente : null;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar paciente: " + e.getMessage());
            return null;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public boolean excluir(Long idPaciente) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return false;
        }
        String sql = "delete from tb_paciente where CD_PACIENTE = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idPaciente);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir paciente: " + e.getMessage());
            return false;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }
}

