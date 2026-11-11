package br.com.fiap.dao;

import br.com.fiap.to.SincronizacaoAgendaTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class SincronizacaoAgendaDAO {

    public ArrayList<SincronizacaoAgendaTO> listarTodos() {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<SincronizacaoAgendaTO> registros = new ArrayList<>();
        String sql = "select * from tb_sincronizacao_agenda order by CD_SINCRONIZACAO";
        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SincronizacaoAgendaTO sincronizacao = new SincronizacaoAgendaTO();
                sincronizacao.setIdSincronizacao(rs.getLong("CD_SINCRONIZACAO"));
                sincronizacao.setNomeProvedor(rs.getString("NM_PROVEDOR"));
                sincronizacao.setIdAgendaExterna(rs.getString("CD_AGENDA_EXTERNA"));
                sincronizacao.setIdEventoExterno(rs.getString("CD_EVENTO_EXTERNO"));
                sincronizacao.setTipoOperacao(rs.getString("TP_OPERACAO"));
                sincronizacao.setStatusSincronizacao(rs.getString("ST_SINCRONIZACAO"));
                sincronizacao.setNumeroTentativas(rs.getInt("NR_TENTATIVAS"));
                Timestamp dataUltimaTentativa = rs.getTimestamp("DT_ULTIMA_TENTATIVA");
                sincronizacao.setDataUltimaTentativa(dataUltimaTentativa == null ? null : dataUltimaTentativa.toLocalDateTime());
                Timestamp dataUltimoSucesso = rs.getTimestamp("DT_ULTIMO_SUCESSO");
                sincronizacao.setDataUltimoSucesso(dataUltimoSucesso == null ? null : dataUltimoSucesso.toLocalDateTime());
                sincronizacao.setUltimoErro(rs.getString("DS_ULTIMO_ERRO"));
                sincronizacao.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                registros.add(sincronizacao);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar registros de sincronização: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    public SincronizacaoAgendaTO buscarPorId(Long idSincronizacao) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "select * from tb_sincronizacao_agenda where CD_SINCRONIZACAO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idSincronizacao);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SincronizacaoAgendaTO sincronizacao = new SincronizacaoAgendaTO();
                    sincronizacao.setIdSincronizacao(rs.getLong("CD_SINCRONIZACAO"));
                    sincronizacao.setNomeProvedor(rs.getString("NM_PROVEDOR"));
                    sincronizacao.setIdAgendaExterna(rs.getString("CD_AGENDA_EXTERNA"));
                    sincronizacao.setIdEventoExterno(rs.getString("CD_EVENTO_EXTERNO"));
                    sincronizacao.setTipoOperacao(rs.getString("TP_OPERACAO"));
                    sincronizacao.setStatusSincronizacao(rs.getString("ST_SINCRONIZACAO"));
                    sincronizacao.setNumeroTentativas(rs.getInt("NR_TENTATIVAS"));
                    Timestamp dataUltimaTentativa = rs.getTimestamp("DT_ULTIMA_TENTATIVA");
                    sincronizacao.setDataUltimaTentativa(dataUltimaTentativa == null ? null : dataUltimaTentativa.toLocalDateTime());
                    Timestamp dataUltimoSucesso = rs.getTimestamp("DT_ULTIMO_SUCESSO");
                    sincronizacao.setDataUltimoSucesso(dataUltimoSucesso == null ? null : dataUltimoSucesso.toLocalDateTime());
                    sincronizacao.setUltimoErro(rs.getString("DS_ULTIMO_ERRO"));
                    sincronizacao.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                    return sincronizacao;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar registro de sincronização: " + e.getMessage());
        } finally {
            ConnectionFactory.closeConnection();
        }
        return null;
    }

    public SincronizacaoAgendaTO inserir(SincronizacaoAgendaTO sincronizacao) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "insert into tb_sincronizacao_agenda (NM_PROVEDOR, CD_AGENDA_EXTERNA, CD_EVENTO_EXTERNO, "
                + "TP_OPERACAO, ST_SINCRONIZACAO, NR_TENTATIVAS, DT_ULTIMA_TENTATIVA, DT_ULTIMO_SUCESSO, "
                + "DS_ULTIMO_ERRO, CD_AGENDAMENTO) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // A coluna identity é omitida do INSERT e solicitada explicitamente ao driver Oracle.
        try (PreparedStatement ps = conexao.prepareStatement(sql, new String[]{"CD_SINCRONIZACAO"})) {
            ps.setString(1, sincronizacao.getNomeProvedor());
            ps.setString(2, sincronizacao.getIdAgendaExterna());
            ps.setString(3, sincronizacao.getIdEventoExterno());
            ps.setString(4, sincronizacao.getTipoOperacao());
            ps.setString(5, sincronizacao.getStatusSincronizacao());
            ps.setInt(6, sincronizacao.getNumeroTentativas());
            ps.setTimestamp(7, sincronizacao.getDataUltimaTentativa() == null
                    ? null : Timestamp.valueOf(sincronizacao.getDataUltimaTentativa()));
            ps.setTimestamp(8, sincronizacao.getDataUltimoSucesso() == null
                    ? null : Timestamp.valueOf(sincronizacao.getDataUltimoSucesso()));
            ps.setString(9, sincronizacao.getUltimoErro());
            ps.setLong(10, sincronizacao.getIdAgendamento());
            if (ps.executeUpdate() == 0) {
                return null;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    System.out.println("O Oracle não retornou o identificador de tb_sincronizacao_agenda.");
                    return null;
                }
                long idGerado = rs.getLong(1);
                if (rs.wasNull()) {
                    System.out.println("O Oracle retornou um identificador nulo para tb_sincronizacao_agenda.");
                    return null;
                }
                sincronizacao.setIdSincronizacao(idGerado);
            }
            return sincronizacao;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir registro de sincronização: " + e.getMessage());
            return null;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public SincronizacaoAgendaTO atualizar(SincronizacaoAgendaTO sincronizacao) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "update tb_sincronizacao_agenda set NM_PROVEDOR = ?, CD_AGENDA_EXTERNA = ?, "
                + "CD_EVENTO_EXTERNO = ?, TP_OPERACAO = ?, ST_SINCRONIZACAO = ?, NR_TENTATIVAS = ?, "
                + "DT_ULTIMA_TENTATIVA = ?, DT_ULTIMO_SUCESSO = ?, DS_ULTIMO_ERRO = ?, CD_AGENDAMENTO = ? "
                + "where CD_SINCRONIZACAO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, sincronizacao.getNomeProvedor());
            ps.setString(2, sincronizacao.getIdAgendaExterna());
            ps.setString(3, sincronizacao.getIdEventoExterno());
            ps.setString(4, sincronizacao.getTipoOperacao());
            ps.setString(5, sincronizacao.getStatusSincronizacao());
            ps.setInt(6, sincronizacao.getNumeroTentativas());
            ps.setTimestamp(7, sincronizacao.getDataUltimaTentativa() == null
                    ? null : Timestamp.valueOf(sincronizacao.getDataUltimaTentativa()));
            ps.setTimestamp(8, sincronizacao.getDataUltimoSucesso() == null
                    ? null : Timestamp.valueOf(sincronizacao.getDataUltimoSucesso()));
            ps.setString(9, sincronizacao.getUltimoErro());
            ps.setLong(10, sincronizacao.getIdAgendamento());
            ps.setLong(11, sincronizacao.getIdSincronizacao());
            return ps.executeUpdate() > 0 ? sincronizacao : null;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar registro de sincronização: " + e.getMessage());
            return null;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public boolean excluir(Long idSincronizacao) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return false;
        }
        String sql = "delete from tb_sincronizacao_agenda where CD_SINCRONIZACAO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idSincronizacao);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir registro de sincronização: " + e.getMessage());
            return false;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public ArrayList<SincronizacaoAgendaTO> listarPorAgendamento(Long idAgendamento) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<SincronizacaoAgendaTO> registros = new ArrayList<>();
        String sql = "select * from tb_sincronizacao_agenda where CD_AGENDAMENTO = ? order by CD_SINCRONIZACAO";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idAgendamento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SincronizacaoAgendaTO sincronizacao = new SincronizacaoAgendaTO();
                    sincronizacao.setIdSincronizacao(rs.getLong("CD_SINCRONIZACAO"));
                    sincronizacao.setNomeProvedor(rs.getString("NM_PROVEDOR"));
                    sincronizacao.setIdAgendaExterna(rs.getString("CD_AGENDA_EXTERNA"));
                    sincronizacao.setIdEventoExterno(rs.getString("CD_EVENTO_EXTERNO"));
                    sincronizacao.setTipoOperacao(rs.getString("TP_OPERACAO"));
                    sincronizacao.setStatusSincronizacao(rs.getString("ST_SINCRONIZACAO"));
                    sincronizacao.setNumeroTentativas(rs.getInt("NR_TENTATIVAS"));
                    Timestamp dataUltimaTentativa = rs.getTimestamp("DT_ULTIMA_TENTATIVA");
                    sincronizacao.setDataUltimaTentativa(dataUltimaTentativa == null ? null : dataUltimaTentativa.toLocalDateTime());
                    Timestamp dataUltimoSucesso = rs.getTimestamp("DT_ULTIMO_SUCESSO");
                    sincronizacao.setDataUltimoSucesso(dataUltimoSucesso == null ? null : dataUltimoSucesso.toLocalDateTime());
                    sincronizacao.setUltimoErro(rs.getString("DS_ULTIMO_ERRO"));
                    sincronizacao.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                    registros.add(sincronizacao);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar registros de sincronização por agendamento: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }
}
