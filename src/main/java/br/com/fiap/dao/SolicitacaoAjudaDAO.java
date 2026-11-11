package br.com.fiap.dao;

import br.com.fiap.to.SolicitacaoAjudaTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;

public class SolicitacaoAjudaDAO {

    public ArrayList<SolicitacaoAjudaTO> listarTodos() {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<SolicitacaoAjudaTO> registros = new ArrayList<>();
        String sql = "select * from tb_solicitacao_ajuda order by CD_SOLICITACAO";
        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SolicitacaoAjudaTO solicitacao = new SolicitacaoAjudaTO();
                solicitacao.setIdSolicitacao(rs.getLong("CD_SOLICITACAO"));
                Timestamp dataAbertura = rs.getTimestamp("DT_ABERTURA");
                solicitacao.setDataAbertura(dataAbertura == null ? null : dataAbertura.toLocalDateTime());
                Timestamp dataEncerramento = rs.getTimestamp("DT_ENCERRAMENTO");
                solicitacao.setDataEncerramento(dataEncerramento == null ? null : dataEncerramento.toLocalDateTime());
                solicitacao.setRelato(rs.getString("DS_RELATO"));
                solicitacao.setStatusSolicitacao(rs.getString("ST_SOLICITACAO"));
                solicitacao.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                long idMembroEquipe = rs.getLong("CD_MEMBRO_EQUIPE");
                solicitacao.setIdMembroEquipe(rs.wasNull() ? null : idMembroEquipe);
                registros.add(solicitacao);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar solicitações: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    public SolicitacaoAjudaTO buscarPorId(Long idSolicitacao) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "select * from tb_solicitacao_ajuda where CD_SOLICITACAO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idSolicitacao);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SolicitacaoAjudaTO solicitacao = new SolicitacaoAjudaTO();
                    solicitacao.setIdSolicitacao(rs.getLong("CD_SOLICITACAO"));
                    Timestamp dataAbertura = rs.getTimestamp("DT_ABERTURA");
                    solicitacao.setDataAbertura(dataAbertura == null ? null : dataAbertura.toLocalDateTime());
                    Timestamp dataEncerramento = rs.getTimestamp("DT_ENCERRAMENTO");
                    solicitacao.setDataEncerramento(dataEncerramento == null ? null : dataEncerramento.toLocalDateTime());
                    solicitacao.setRelato(rs.getString("DS_RELATO"));
                    solicitacao.setStatusSolicitacao(rs.getString("ST_SOLICITACAO"));
                    solicitacao.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                    long idMembroEquipe = rs.getLong("CD_MEMBRO_EQUIPE");
                    solicitacao.setIdMembroEquipe(rs.wasNull() ? null : idMembroEquipe);
                    return solicitacao;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar solicitação: " + e.getMessage());
        } finally {
            ConnectionFactory.closeConnection();
        }
        return null;
    }

    // A BO mantém a transação aberta até concluir a operação que depende deste bloqueio.
    public SolicitacaoAjudaTO buscarPorIdParaAtualizar(Connection conexao, Long idSolicitacao) {
        try {
            if (conexao.getAutoCommit()) {
                System.out.println("O bloqueio do registro exige uma transação com autoCommit desativado.");
                return null;
            }
            String sql = "select * from tb_solicitacao_ajuda where CD_SOLICITACAO = ? for update";
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setLong(1, idSolicitacao);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        SolicitacaoAjudaTO solicitacao = new SolicitacaoAjudaTO();
                        solicitacao.setIdSolicitacao(rs.getLong("CD_SOLICITACAO"));
                        Timestamp dataAbertura = rs.getTimestamp("DT_ABERTURA");
                        solicitacao.setDataAbertura(dataAbertura == null ? null : dataAbertura.toLocalDateTime());
                        Timestamp dataEncerramento = rs.getTimestamp("DT_ENCERRAMENTO");
                        solicitacao.setDataEncerramento(dataEncerramento == null ? null : dataEncerramento.toLocalDateTime());
                        solicitacao.setRelato(rs.getString("DS_RELATO"));
                        solicitacao.setStatusSolicitacao(rs.getString("ST_SOLICITACAO"));
                        solicitacao.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                        long idMembroEquipe = rs.getLong("CD_MEMBRO_EQUIPE");
                        solicitacao.setIdMembroEquipe(rs.wasNull() ? null : idMembroEquipe);
                        return solicitacao;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao bloquear solicitação: " + e.getMessage());
        }
        return null;
    }

    public SolicitacaoAjudaTO inserir(Connection conexao, SolicitacaoAjudaTO solicitacao) {
        String sql = "insert into tb_solicitacao_ajuda (DT_ABERTURA, DT_ENCERRAMENTO, DS_RELATO, "
                + "ST_SOLICITACAO, CD_AGENDAMENTO, CD_MEMBRO_EQUIPE) values (?, ?, ?, ?, ?, ?)";
        // A coluna identity é omitida do INSERT e solicitada explicitamente ao driver Oracle.
        try (PreparedStatement ps = conexao.prepareStatement(sql, new String[]{"CD_SOLICITACAO"})) {
            ps.setTimestamp(1, solicitacao.getDataAbertura() == null ? null : Timestamp.valueOf(solicitacao.getDataAbertura()));
            ps.setTimestamp(2, solicitacao.getDataEncerramento() == null ? null : Timestamp.valueOf(solicitacao.getDataEncerramento()));
            ps.setString(3, solicitacao.getRelato());
            ps.setString(4, solicitacao.getStatusSolicitacao());
            ps.setLong(5, solicitacao.getIdAgendamento());
            if (solicitacao.getIdMembroEquipe() == null) {
                ps.setNull(6, Types.NUMERIC);
            } else {
                ps.setLong(6, solicitacao.getIdMembroEquipe());
            }
            if (ps.executeUpdate() == 0) {
                return null;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    System.out.println("O Oracle não retornou o identificador de tb_solicitacao_ajuda.");
                    return null;
                }
                long idGerado = rs.getLong(1);
                if (rs.wasNull()) {
                    System.out.println("O Oracle retornou um identificador nulo para tb_solicitacao_ajuda.");
                    return null;
                }
                solicitacao.setIdSolicitacao(idGerado);
            }
            return solicitacao;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir solicitação: " + e.getMessage());
            return null;
        }
    }

    public SolicitacaoAjudaTO atualizar(Connection conexao, SolicitacaoAjudaTO solicitacao) {
        String sql = "update tb_solicitacao_ajuda set DT_ABERTURA = ?, DT_ENCERRAMENTO = ?, DS_RELATO = "
                + "?, ST_SOLICITACAO = ?, CD_AGENDAMENTO = ?, CD_MEMBRO_EQUIPE = ? where "
                + "CD_SOLICITACAO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setTimestamp(1, solicitacao.getDataAbertura() == null ? null : Timestamp.valueOf(solicitacao.getDataAbertura()));
            ps.setTimestamp(2, solicitacao.getDataEncerramento() == null ? null : Timestamp.valueOf(solicitacao.getDataEncerramento()));
            ps.setString(3, solicitacao.getRelato());
            ps.setString(4, solicitacao.getStatusSolicitacao());
            ps.setLong(5, solicitacao.getIdAgendamento());
            if (solicitacao.getIdMembroEquipe() == null) {
                ps.setNull(6, Types.NUMERIC);
            } else {
                ps.setLong(6, solicitacao.getIdMembroEquipe());
            }
            ps.setLong(7, solicitacao.getIdSolicitacao());
            return ps.executeUpdate() > 0 ? solicitacao : null;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar solicitação: " + e.getMessage());
            return null;
        }
    }

    public boolean excluir(Long idSolicitacao) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return false;
        }
        String sql = "delete from tb_solicitacao_ajuda where CD_SOLICITACAO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idSolicitacao);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir solicitação: " + e.getMessage());
            return false;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public ArrayList<SolicitacaoAjudaTO> listarPorAgendamento(Long idAgendamento) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<SolicitacaoAjudaTO> registros = new ArrayList<>();
        String sql = "select * from tb_solicitacao_ajuda where CD_AGENDAMENTO = ? order by CD_SOLICITACAO";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idAgendamento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SolicitacaoAjudaTO solicitacao = new SolicitacaoAjudaTO();
                    solicitacao.setIdSolicitacao(rs.getLong("CD_SOLICITACAO"));
                    Timestamp dataAbertura = rs.getTimestamp("DT_ABERTURA");
                    solicitacao.setDataAbertura(dataAbertura == null ? null : dataAbertura.toLocalDateTime());
                    Timestamp dataEncerramento = rs.getTimestamp("DT_ENCERRAMENTO");
                    solicitacao.setDataEncerramento(dataEncerramento == null ? null : dataEncerramento.toLocalDateTime());
                    solicitacao.setRelato(rs.getString("DS_RELATO"));
                    solicitacao.setStatusSolicitacao(rs.getString("ST_SOLICITACAO"));
                    solicitacao.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                    long idMembroEquipe = rs.getLong("CD_MEMBRO_EQUIPE");
                    solicitacao.setIdMembroEquipe(rs.wasNull() ? null : idMembroEquipe);
                    registros.add(solicitacao);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar solicitações por agendamento: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    // A BO bloqueia o agendamento antes de verificar e abrir uma solicitação.
    public boolean existeSolicitacaoAtivaPorAgendamento(Connection conexao, Long idAgendamento,
                                                        Long idSolicitacaoIgnorada) {
        String sql = "select count(*) from tb_solicitacao_ajuda where CD_AGENDAMENTO = ? and "
                + "ST_SOLICITACAO in ('ABERTA', 'EM_ANALISE')";
        if (idSolicitacaoIgnorada != null) {
            sql += " and CD_SOLICITACAO <> ?";
        }
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idAgendamento);
            if (idSolicitacaoIgnorada != null) {
                ps.setLong(2, idSolicitacaoIgnorada);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar solicitações ativas: " + e.getMessage());
            // Impede um novo cadastro quando não foi possível verificar o conflito.
            return true;
        }
    }
}

