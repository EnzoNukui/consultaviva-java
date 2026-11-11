package br.com.fiap.dao;

import br.com.fiap.to.HistoricoEventoTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;

public class HistoricoEventoDAO {

    public ArrayList<HistoricoEventoTO> listarTodos() {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<HistoricoEventoTO> registros = new ArrayList<>();
        String sql = "select * from tb_historico_evento order by CD_EVENTO";
        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                HistoricoEventoTO evento = new HistoricoEventoTO();
                evento.setIdEvento(rs.getLong("CD_EVENTO"));
                Timestamp dataEvento = rs.getTimestamp("DT_EVENTO");
                evento.setDataEvento(dataEvento == null ? null : dataEvento.toLocalDateTime());
                evento.setTipoEvento(rs.getString("TP_EVENTO"));
                evento.setDescricaoEvento(rs.getString("DS_EVENTO"));
                evento.setTipoOrigem(rs.getString("TP_ORIGEM"));
                evento.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                long idSolicitacaoLido = rs.getLong("CD_SOLICITACAO");
                evento.setIdSolicitacao(rs.wasNull() ? null : idSolicitacaoLido);
                long idPropostaLido = rs.getLong("CD_PROPOSTA");
                evento.setIdProposta(rs.wasNull() ? null : idPropostaLido);
                long idMembroEquipeLido = rs.getLong("CD_MEMBRO_EQUIPE");
                evento.setIdMembroEquipe(rs.wasNull() ? null : idMembroEquipeLido);
                registros.add(evento);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar eventos do histórico: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    public HistoricoEventoTO buscarPorId(Long idEvento) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "select * from tb_historico_evento where CD_EVENTO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idEvento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    HistoricoEventoTO evento = new HistoricoEventoTO();
                    evento.setIdEvento(rs.getLong("CD_EVENTO"));
                    Timestamp dataEvento = rs.getTimestamp("DT_EVENTO");
                    evento.setDataEvento(dataEvento == null ? null : dataEvento.toLocalDateTime());
                    evento.setTipoEvento(rs.getString("TP_EVENTO"));
                    evento.setDescricaoEvento(rs.getString("DS_EVENTO"));
                    evento.setTipoOrigem(rs.getString("TP_ORIGEM"));
                    evento.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                    long idSolicitacaoLido = rs.getLong("CD_SOLICITACAO");
                    evento.setIdSolicitacao(rs.wasNull() ? null : idSolicitacaoLido);
                    long idPropostaLido = rs.getLong("CD_PROPOSTA");
                    evento.setIdProposta(rs.wasNull() ? null : idPropostaLido);
                    long idMembroEquipeLido = rs.getLong("CD_MEMBRO_EQUIPE");
                    evento.setIdMembroEquipe(rs.wasNull() ? null : idMembroEquipeLido);
                    return evento;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar evento do histórico: " + e.getMessage());
        } finally {
            ConnectionFactory.closeConnection();
        }
        return null;
    }

    public HistoricoEventoTO inserir(Connection conexao, HistoricoEventoTO evento) {
        String sql = "insert into tb_historico_evento (DT_EVENTO, TP_EVENTO, DS_EVENTO, TP_ORIGEM, "
                + "CD_AGENDAMENTO, CD_SOLICITACAO, CD_PROPOSTA, CD_MEMBRO_EQUIPE) values (?, ?, ?, ?, ?, ?, "
                + "?, ?)";
        // A coluna identity é omitida do INSERT e solicitada explicitamente ao driver Oracle.
        try (PreparedStatement ps = conexao.prepareStatement(sql, new String[]{"CD_EVENTO"})) {
            ps.setTimestamp(1, evento.getDataEvento() == null
                    ? null : Timestamp.valueOf(evento.getDataEvento()));
            ps.setString(2, evento.getTipoEvento());
            ps.setString(3, evento.getDescricaoEvento());
            ps.setString(4, evento.getTipoOrigem());
            ps.setLong(5, evento.getIdAgendamento());
            if (evento.getIdSolicitacao() == null) {
                ps.setNull(6, Types.NUMERIC);
            } else {
                ps.setLong(6, evento.getIdSolicitacao());
            }
            if (evento.getIdProposta() == null) {
                ps.setNull(7, Types.NUMERIC);
            } else {
                ps.setLong(7, evento.getIdProposta());
            }
            if (evento.getIdMembroEquipe() == null) {
                ps.setNull(8, Types.NUMERIC);
            } else {
                ps.setLong(8, evento.getIdMembroEquipe());
            }
            if (ps.executeUpdate() == 0) {
                return null;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    System.out.println("O Oracle não retornou o identificador de tb_historico_evento.");
                    return null;
                }
                long idGerado = rs.getLong(1);
                if (rs.wasNull()) {
                    System.out.println("O Oracle retornou um identificador nulo para tb_historico_evento.");
                    return null;
                }
                evento.setIdEvento(idGerado);
            }
            return evento;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir evento do histórico: " + e.getMessage());
            return null;
        }
    }

    public HistoricoEventoTO atualizar(HistoricoEventoTO evento) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "update tb_historico_evento set DT_EVENTO = ?, TP_EVENTO = ?, DS_EVENTO = ?, TP_ORIGEM = ?, "
                + "CD_AGENDAMENTO = ?, CD_SOLICITACAO = ?, CD_PROPOSTA = ?, CD_MEMBRO_EQUIPE = ? where "
                + "CD_EVENTO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setTimestamp(1, evento.getDataEvento() == null
                    ? null : Timestamp.valueOf(evento.getDataEvento()));
            ps.setString(2, evento.getTipoEvento());
            ps.setString(3, evento.getDescricaoEvento());
            ps.setString(4, evento.getTipoOrigem());
            ps.setLong(5, evento.getIdAgendamento());
            if (evento.getIdSolicitacao() == null) {
                ps.setNull(6, Types.NUMERIC);
            } else {
                ps.setLong(6, evento.getIdSolicitacao());
            }
            if (evento.getIdProposta() == null) {
                ps.setNull(7, Types.NUMERIC);
            } else {
                ps.setLong(7, evento.getIdProposta());
            }
            if (evento.getIdMembroEquipe() == null) {
                ps.setNull(8, Types.NUMERIC);
            } else {
                ps.setLong(8, evento.getIdMembroEquipe());
            }
            ps.setLong(9, evento.getIdEvento());
            return ps.executeUpdate() > 0 ? evento : null;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar evento do histórico: " + e.getMessage());
            return null;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public boolean excluir(Long idEvento) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return false;
        }
        String sql = "delete from tb_historico_evento where CD_EVENTO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idEvento);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir evento do histórico: " + e.getMessage());
            return false;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public ArrayList<HistoricoEventoTO> listarPorAgendamento(Long idAgendamento) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<HistoricoEventoTO> registros = new ArrayList<>();
        String sql = "select * from tb_historico_evento where CD_AGENDAMENTO = ? order by CD_EVENTO";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idAgendamento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistoricoEventoTO evento = new HistoricoEventoTO();
                    evento.setIdEvento(rs.getLong("CD_EVENTO"));
                    Timestamp dataEvento = rs.getTimestamp("DT_EVENTO");
                    evento.setDataEvento(dataEvento == null ? null : dataEvento.toLocalDateTime());
                    evento.setTipoEvento(rs.getString("TP_EVENTO"));
                    evento.setDescricaoEvento(rs.getString("DS_EVENTO"));
                    evento.setTipoOrigem(rs.getString("TP_ORIGEM"));
                    evento.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                    long idSolicitacaoLido = rs.getLong("CD_SOLICITACAO");
                    evento.setIdSolicitacao(rs.wasNull() ? null : idSolicitacaoLido);
                    long idPropostaLido = rs.getLong("CD_PROPOSTA");
                    evento.setIdProposta(rs.wasNull() ? null : idPropostaLido);
                    long idMembroEquipeLido = rs.getLong("CD_MEMBRO_EQUIPE");
                    evento.setIdMembroEquipe(rs.wasNull() ? null : idMembroEquipeLido);
                    registros.add(evento);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar eventos do histórico por agendamento: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    public ArrayList<HistoricoEventoTO> listarPorSolicitacao(Long idSolicitacao) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<HistoricoEventoTO> registros = new ArrayList<>();
        String sql = "select * from tb_historico_evento where CD_SOLICITACAO = ? order by CD_EVENTO";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idSolicitacao);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistoricoEventoTO evento = new HistoricoEventoTO();
                    evento.setIdEvento(rs.getLong("CD_EVENTO"));
                    Timestamp dataEvento = rs.getTimestamp("DT_EVENTO");
                    evento.setDataEvento(dataEvento == null ? null : dataEvento.toLocalDateTime());
                    evento.setTipoEvento(rs.getString("TP_EVENTO"));
                    evento.setDescricaoEvento(rs.getString("DS_EVENTO"));
                    evento.setTipoOrigem(rs.getString("TP_ORIGEM"));
                    evento.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                    long idSolicitacaoLido = rs.getLong("CD_SOLICITACAO");
                    evento.setIdSolicitacao(rs.wasNull() ? null : idSolicitacaoLido);
                    long idPropostaLido = rs.getLong("CD_PROPOSTA");
                    evento.setIdProposta(rs.wasNull() ? null : idPropostaLido);
                    long idMembroEquipeLido = rs.getLong("CD_MEMBRO_EQUIPE");
                    evento.setIdMembroEquipe(rs.wasNull() ? null : idMembroEquipeLido);
                    registros.add(evento);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar eventos do histórico por solicitação: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    public ArrayList<HistoricoEventoTO> listarPorProposta(Long idProposta) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<HistoricoEventoTO> registros = new ArrayList<>();
        String sql = "select * from tb_historico_evento where CD_PROPOSTA = ? order by CD_EVENTO";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idProposta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistoricoEventoTO evento = new HistoricoEventoTO();
                    evento.setIdEvento(rs.getLong("CD_EVENTO"));
                    Timestamp dataEvento = rs.getTimestamp("DT_EVENTO");
                    evento.setDataEvento(dataEvento == null ? null : dataEvento.toLocalDateTime());
                    evento.setTipoEvento(rs.getString("TP_EVENTO"));
                    evento.setDescricaoEvento(rs.getString("DS_EVENTO"));
                    evento.setTipoOrigem(rs.getString("TP_ORIGEM"));
                    evento.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                    long idSolicitacaoLido = rs.getLong("CD_SOLICITACAO");
                    evento.setIdSolicitacao(rs.wasNull() ? null : idSolicitacaoLido);
                    long idPropostaLido = rs.getLong("CD_PROPOSTA");
                    evento.setIdProposta(rs.wasNull() ? null : idPropostaLido);
                    long idMembroEquipeLido = rs.getLong("CD_MEMBRO_EQUIPE");
                    evento.setIdMembroEquipe(rs.wasNull() ? null : idMembroEquipeLido);
                    registros.add(evento);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar eventos do histórico por proposta: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }
}
