package br.com.fiap.dao;

import br.com.fiap.to.PreparoVirtualTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;

public class PreparoVirtualDAO {

    public ArrayList<PreparoVirtualTO> listarTodos() {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<PreparoVirtualTO> registros = new ArrayList<>();
        String sql = "select * from tb_preparo_virtual order by CD_PREPARO";
        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PreparoVirtualTO preparo = new PreparoVirtualTO();
                preparo.setIdPreparo(rs.getLong("CD_PREPARO"));
                Timestamp dataVerificacao = rs.getTimestamp("DT_VERIFICACAO");
                preparo.setDataVerificacao(dataVerificacao == null ? null : dataVerificacao.toLocalDateTime());
                preparo.setStatusPermissaoMicrofone(rs.getString("ST_PERMISSAO_MICROFONE"));
                preparo.setStatusPermissaoCamera(rs.getString("ST_PERMISSAO_CAMERA"));
                preparo.setStatusCamera(rs.getString("ST_CAMERA"));
                preparo.setStatusMicrofone(rs.getString("ST_MICROFONE"));
                preparo.setStatusConexao(rs.getString("ST_CONEXAO"));
                preparo.setObservacao(rs.getString("DS_OBSERVACAO"));
                preparo.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                long idPropostaLido = rs.getLong("CD_PROPOSTA");
                preparo.setIdProposta(rs.wasNull() ? null : idPropostaLido);
                registros.add(preparo);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar registros de preparo virtual: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    public PreparoVirtualTO buscarPorId(Long idPreparo) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "select * from tb_preparo_virtual where CD_PREPARO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idPreparo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PreparoVirtualTO preparo = new PreparoVirtualTO();
                    preparo.setIdPreparo(rs.getLong("CD_PREPARO"));
                    Timestamp dataVerificacao = rs.getTimestamp("DT_VERIFICACAO");
                    preparo.setDataVerificacao(dataVerificacao == null ? null : dataVerificacao.toLocalDateTime());
                    preparo.setStatusPermissaoMicrofone(rs.getString("ST_PERMISSAO_MICROFONE"));
                    preparo.setStatusPermissaoCamera(rs.getString("ST_PERMISSAO_CAMERA"));
                    preparo.setStatusCamera(rs.getString("ST_CAMERA"));
                    preparo.setStatusMicrofone(rs.getString("ST_MICROFONE"));
                    preparo.setStatusConexao(rs.getString("ST_CONEXAO"));
                    preparo.setObservacao(rs.getString("DS_OBSERVACAO"));
                    preparo.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                    long idPropostaLido = rs.getLong("CD_PROPOSTA");
                    preparo.setIdProposta(rs.wasNull() ? null : idPropostaLido);
                    return preparo;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar preparo virtual: " + e.getMessage());
        } finally {
            ConnectionFactory.closeConnection();
        }
        return null;
    }

    public PreparoVirtualTO inserir(Connection conexao, PreparoVirtualTO preparo) {
        String sql = "insert into tb_preparo_virtual (DT_VERIFICACAO, ST_PERMISSAO_MICROFONE, "
                + "ST_PERMISSAO_CAMERA, ST_CAMERA, ST_MICROFONE, ST_CONEXAO, DS_OBSERVACAO, CD_AGENDAMENTO, "
                + "CD_PROPOSTA) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // A coluna identity é omitida do INSERT e solicitada explicitamente ao driver Oracle.
        try (PreparedStatement ps = conexao.prepareStatement(sql, new String[]{"CD_PREPARO"})) {
            ps.setTimestamp(1, preparo.getDataVerificacao() == null
                    ? null : Timestamp.valueOf(preparo.getDataVerificacao()));
            ps.setString(2, preparo.getStatusPermissaoMicrofone());
            ps.setString(3, preparo.getStatusPermissaoCamera());
            ps.setString(4, preparo.getStatusCamera());
            ps.setString(5, preparo.getStatusMicrofone());
            ps.setString(6, preparo.getStatusConexao());
            ps.setString(7, preparo.getObservacao());
            ps.setLong(8, preparo.getIdAgendamento());
            if (preparo.getIdProposta() == null) {
                ps.setNull(9, Types.NUMERIC);
            } else {
                ps.setLong(9, preparo.getIdProposta());
            }
            if (ps.executeUpdate() == 0) {
                return null;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    System.out.println("O Oracle não retornou o identificador de tb_preparo_virtual.");
                    return null;
                }
                long idGerado = rs.getLong(1);
                if (rs.wasNull()) {
                    System.out.println("O Oracle retornou um identificador nulo para tb_preparo_virtual.");
                    return null;
                }
                preparo.setIdPreparo(idGerado);
            }
            return preparo;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir preparo virtual: " + e.getMessage());
            return null;
        }
    }

    public PreparoVirtualTO atualizar(Connection conexao, PreparoVirtualTO preparo) {
        String sql = "update tb_preparo_virtual set DT_VERIFICACAO = ?, ST_PERMISSAO_MICROFONE = ?, "
                + "ST_PERMISSAO_CAMERA = ?, ST_CAMERA = ?, ST_MICROFONE = ?, ST_CONEXAO = ?, DS_OBSERVACAO = "
                + "?, CD_AGENDAMENTO = ?, CD_PROPOSTA = ? where CD_PREPARO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setTimestamp(1, preparo.getDataVerificacao() == null
                    ? null : Timestamp.valueOf(preparo.getDataVerificacao()));
            ps.setString(2, preparo.getStatusPermissaoMicrofone());
            ps.setString(3, preparo.getStatusPermissaoCamera());
            ps.setString(4, preparo.getStatusCamera());
            ps.setString(5, preparo.getStatusMicrofone());
            ps.setString(6, preparo.getStatusConexao());
            ps.setString(7, preparo.getObservacao());
            ps.setLong(8, preparo.getIdAgendamento());
            if (preparo.getIdProposta() == null) {
                ps.setNull(9, Types.NUMERIC);
            } else {
                ps.setLong(9, preparo.getIdProposta());
            }
            ps.setLong(10, preparo.getIdPreparo());
            return ps.executeUpdate() > 0 ? preparo : null;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar preparo virtual: " + e.getMessage());
            return null;
        }
    }

    public boolean excluir(Long idPreparo) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return false;
        }
        String sql = "delete from tb_preparo_virtual where CD_PREPARO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idPreparo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir preparo virtual: " + e.getMessage());
            return false;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public ArrayList<PreparoVirtualTO> listarPorAgendamento(Long idAgendamento) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<PreparoVirtualTO> registros = new ArrayList<>();
        String sql = "select * from tb_preparo_virtual where CD_AGENDAMENTO = ? order by CD_PREPARO";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idAgendamento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PreparoVirtualTO preparo = new PreparoVirtualTO();
                    preparo.setIdPreparo(rs.getLong("CD_PREPARO"));
                    Timestamp dataVerificacao = rs.getTimestamp("DT_VERIFICACAO");
                    preparo.setDataVerificacao(dataVerificacao == null ? null : dataVerificacao.toLocalDateTime());
                    preparo.setStatusPermissaoMicrofone(rs.getString("ST_PERMISSAO_MICROFONE"));
                    preparo.setStatusPermissaoCamera(rs.getString("ST_PERMISSAO_CAMERA"));
                    preparo.setStatusCamera(rs.getString("ST_CAMERA"));
                    preparo.setStatusMicrofone(rs.getString("ST_MICROFONE"));
                    preparo.setStatusConexao(rs.getString("ST_CONEXAO"));
                    preparo.setObservacao(rs.getString("DS_OBSERVACAO"));
                    preparo.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                    long idPropostaLido = rs.getLong("CD_PROPOSTA");
                    preparo.setIdProposta(rs.wasNull() ? null : idPropostaLido);
                    registros.add(preparo);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar registros de preparo virtual por agendamento: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    public ArrayList<PreparoVirtualTO> listarPorProposta(Long idProposta) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<PreparoVirtualTO> registros = new ArrayList<>();
        String sql = "select * from tb_preparo_virtual where CD_PROPOSTA = ? order by CD_PREPARO";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idProposta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PreparoVirtualTO preparo = new PreparoVirtualTO();
                    preparo.setIdPreparo(rs.getLong("CD_PREPARO"));
                    Timestamp dataVerificacao = rs.getTimestamp("DT_VERIFICACAO");
                    preparo.setDataVerificacao(dataVerificacao == null ? null : dataVerificacao.toLocalDateTime());
                    preparo.setStatusPermissaoMicrofone(rs.getString("ST_PERMISSAO_MICROFONE"));
                    preparo.setStatusPermissaoCamera(rs.getString("ST_PERMISSAO_CAMERA"));
                    preparo.setStatusCamera(rs.getString("ST_CAMERA"));
                    preparo.setStatusMicrofone(rs.getString("ST_MICROFONE"));
                    preparo.setStatusConexao(rs.getString("ST_CONEXAO"));
                    preparo.setObservacao(rs.getString("DS_OBSERVACAO"));
                    preparo.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                    long idPropostaLido = rs.getLong("CD_PROPOSTA");
                    preparo.setIdProposta(rs.wasNull() ? null : idPropostaLido);
                    registros.add(preparo);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar registros de preparo virtual por proposta: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }
}
