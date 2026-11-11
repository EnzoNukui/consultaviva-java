package br.com.fiap.dao;

import br.com.fiap.to.PropostaAtendimentoTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class PropostaAtendimentoDAO {

    public ArrayList<PropostaAtendimentoTO> listarTodos() {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<PropostaAtendimentoTO> registros = new ArrayList<>();
        String sql = "select * from tb_proposta_atendimento order by CD_PROPOSTA";
        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PropostaAtendimentoTO proposta = new PropostaAtendimentoTO();
                proposta.setIdProposta(rs.getLong("CD_PROPOSTA"));
                proposta.setTipoAlternativa(rs.getString("TP_ALTERNATIVA"));
                proposta.setModalidade(rs.getString("DS_MODALIDADE"));
                proposta.setJustificativa(rs.getString("DS_JUSTIFICATIVA"));
                Timestamp dataCriacao = rs.getTimestamp("DT_CRIACAO");
                proposta.setDataCriacao(dataCriacao == null ? null : dataCriacao.toLocalDateTime());
                proposta.setStatusProposta(rs.getString("ST_PROPOSTA"));
                proposta.setStatusAutorizacao(rs.getString("ST_AUTORIZACAO"));
                Timestamp dataAutorizacao = rs.getTimestamp("DT_AUTORIZACAO");
                proposta.setDataAutorizacao(dataAutorizacao == null ? null : dataAutorizacao.toLocalDateTime());
                proposta.setStatusAceitePaciente(rs.getString("ST_ACEITE_PACIENTE"));
                Timestamp dataResposta = rs.getTimestamp("DT_RESPOSTA");
                proposta.setDataResposta(dataResposta == null ? null : dataResposta.toLocalDateTime());
                Timestamp dataInicioReserva = rs.getTimestamp("DT_INICIO_RESERVA");
                proposta.setDataInicioReserva(dataInicioReserva == null ? null : dataInicioReserva.toLocalDateTime());
                Timestamp dataFimReserva = rs.getTimestamp("DT_FIM_RESERVA");
                proposta.setDataFimReserva(dataFimReserva == null ? null : dataFimReserva.toLocalDateTime());
                Timestamp dataLimiteAceite = rs.getTimestamp("DT_LIMITE_ACEITE");
                proposta.setDataLimiteAceite(dataLimiteAceite == null ? null : dataLimiteAceite.toLocalDateTime());
                Timestamp dataConfirmacao = rs.getTimestamp("DT_CONFIRMACAO");
                proposta.setDataConfirmacao(dataConfirmacao == null ? null : dataConfirmacao.toLocalDateTime());
                proposta.setIdSolicitacao(rs.getLong("CD_SOLICITACAO"));
                proposta.setIdHorario(rs.getLong("CD_HORARIO"));
                long idAnalistaLido = rs.getLong("CD_ANALISTA");
                proposta.setIdAnalista(rs.wasNull() ? null : idAnalistaLido);
                long idAutorizadorLido = rs.getLong("CD_AUTORIZADOR");
                proposta.setIdAutorizador(rs.wasNull() ? null : idAutorizadorLido);
                registros.add(proposta);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar propostas: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    public PropostaAtendimentoTO buscarPorId(Long idProposta) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "select * from tb_proposta_atendimento where CD_PROPOSTA = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idProposta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PropostaAtendimentoTO proposta = new PropostaAtendimentoTO();
                    proposta.setIdProposta(rs.getLong("CD_PROPOSTA"));
                    proposta.setTipoAlternativa(rs.getString("TP_ALTERNATIVA"));
                    proposta.setModalidade(rs.getString("DS_MODALIDADE"));
                    proposta.setJustificativa(rs.getString("DS_JUSTIFICATIVA"));
                    Timestamp dataCriacao = rs.getTimestamp("DT_CRIACAO");
                    proposta.setDataCriacao(dataCriacao == null ? null : dataCriacao.toLocalDateTime());
                    proposta.setStatusProposta(rs.getString("ST_PROPOSTA"));
                    proposta.setStatusAutorizacao(rs.getString("ST_AUTORIZACAO"));
                    Timestamp dataAutorizacao = rs.getTimestamp("DT_AUTORIZACAO");
                    proposta.setDataAutorizacao(dataAutorizacao == null ? null : dataAutorizacao.toLocalDateTime());
                    proposta.setStatusAceitePaciente(rs.getString("ST_ACEITE_PACIENTE"));
                    Timestamp dataResposta = rs.getTimestamp("DT_RESPOSTA");
                    proposta.setDataResposta(dataResposta == null ? null : dataResposta.toLocalDateTime());
                    Timestamp dataInicioReserva = rs.getTimestamp("DT_INICIO_RESERVA");
                    proposta.setDataInicioReserva(dataInicioReserva == null ? null : dataInicioReserva.toLocalDateTime());
                    Timestamp dataFimReserva = rs.getTimestamp("DT_FIM_RESERVA");
                    proposta.setDataFimReserva(dataFimReserva == null ? null : dataFimReserva.toLocalDateTime());
                    Timestamp dataLimiteAceite = rs.getTimestamp("DT_LIMITE_ACEITE");
                    proposta.setDataLimiteAceite(dataLimiteAceite == null ? null : dataLimiteAceite.toLocalDateTime());
                    Timestamp dataConfirmacao = rs.getTimestamp("DT_CONFIRMACAO");
                    proposta.setDataConfirmacao(dataConfirmacao == null ? null : dataConfirmacao.toLocalDateTime());
                    proposta.setIdSolicitacao(rs.getLong("CD_SOLICITACAO"));
                    proposta.setIdHorario(rs.getLong("CD_HORARIO"));
                    long idAnalistaLido = rs.getLong("CD_ANALISTA");
                    proposta.setIdAnalista(rs.wasNull() ? null : idAnalistaLido);
                    long idAutorizadorLido = rs.getLong("CD_AUTORIZADOR");
                    proposta.setIdAutorizador(rs.wasNull() ? null : idAutorizadorLido);
                    return proposta;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar proposta: " + e.getMessage());
        } finally {
            ConnectionFactory.closeConnection();
        }
        return null;
    }

    // A BO mantém a transação aberta enquanto usa o registro bloqueado.
    public PropostaAtendimentoTO buscarPorIdParaAtualizar(Connection conexao, Long idProposta) {
        try {
            if (conexao.getAutoCommit()) {
                System.out.println("O bloqueio do registro exige uma transação com autoCommit desativado.");
                return null;
            }
            String sql = "select * from tb_proposta_atendimento where CD_PROPOSTA = ? for update";
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setLong(1, idProposta);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        PropostaAtendimentoTO proposta = new PropostaAtendimentoTO();
                        proposta.setIdProposta(rs.getLong("CD_PROPOSTA"));
                        proposta.setTipoAlternativa(rs.getString("TP_ALTERNATIVA"));
                        proposta.setModalidade(rs.getString("DS_MODALIDADE"));
                        proposta.setJustificativa(rs.getString("DS_JUSTIFICATIVA"));
                        Timestamp dataCriacao = rs.getTimestamp("DT_CRIACAO");
                        proposta.setDataCriacao(dataCriacao == null ? null : dataCriacao.toLocalDateTime());
                        proposta.setStatusProposta(rs.getString("ST_PROPOSTA"));
                        proposta.setStatusAutorizacao(rs.getString("ST_AUTORIZACAO"));
                        Timestamp dataAutorizacao = rs.getTimestamp("DT_AUTORIZACAO");
                        proposta.setDataAutorizacao(dataAutorizacao == null ? null : dataAutorizacao.toLocalDateTime());
                        proposta.setStatusAceitePaciente(rs.getString("ST_ACEITE_PACIENTE"));
                        Timestamp dataResposta = rs.getTimestamp("DT_RESPOSTA");
                        proposta.setDataResposta(dataResposta == null ? null : dataResposta.toLocalDateTime());
                        Timestamp dataInicioReserva = rs.getTimestamp("DT_INICIO_RESERVA");
                        proposta.setDataInicioReserva(dataInicioReserva == null ? null : dataInicioReserva.toLocalDateTime());
                        Timestamp dataFimReserva = rs.getTimestamp("DT_FIM_RESERVA");
                        proposta.setDataFimReserva(dataFimReserva == null ? null : dataFimReserva.toLocalDateTime());
                        Timestamp dataLimiteAceite = rs.getTimestamp("DT_LIMITE_ACEITE");
                        proposta.setDataLimiteAceite(dataLimiteAceite == null ? null : dataLimiteAceite.toLocalDateTime());
                        Timestamp dataConfirmacao = rs.getTimestamp("DT_CONFIRMACAO");
                        proposta.setDataConfirmacao(dataConfirmacao == null ? null : dataConfirmacao.toLocalDateTime());
                        proposta.setIdSolicitacao(rs.getLong("CD_SOLICITACAO"));
                        proposta.setIdHorario(rs.getLong("CD_HORARIO"));
                        long idAnalistaLido = rs.getLong("CD_ANALISTA");
                        proposta.setIdAnalista(rs.wasNull() ? null : idAnalistaLido);
                        long idAutorizadorLido = rs.getLong("CD_AUTORIZADOR");
                        proposta.setIdAutorizador(rs.wasNull() ? null : idAutorizadorLido);
                        return proposta;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao bloquear proposta: " + e.getMessage());
        }
        return null;
    }

    public PropostaAtendimentoTO inserir(Connection conexao, PropostaAtendimentoTO proposta) {
        String sql = "insert into tb_proposta_atendimento (TP_ALTERNATIVA, DS_MODALIDADE, DS_JUSTIFICATIVA, "
                + "DT_CRIACAO, ST_PROPOSTA, ST_AUTORIZACAO, DT_AUTORIZACAO, ST_ACEITE_PACIENTE, DT_RESPOSTA, "
                + "DT_INICIO_RESERVA, DT_FIM_RESERVA, DT_LIMITE_ACEITE, DT_CONFIRMACAO, CD_SOLICITACAO, "
                + "CD_HORARIO, CD_ANALISTA, CD_AUTORIZADOR) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                + "?, ?, ?)";
        // A coluna identity é omitida do INSERT e solicitada explicitamente ao driver Oracle.
        try (PreparedStatement ps = conexao.prepareStatement(sql, new String[]{"CD_PROPOSTA"})) {
            ps.setString(1, proposta.getTipoAlternativa());
            ps.setString(2, proposta.getModalidade());
            ps.setString(3, proposta.getJustificativa());
            ps.setTimestamp(4, proposta.getDataCriacao() == null
                    ? null : Timestamp.valueOf(proposta.getDataCriacao()));
            ps.setString(5, proposta.getStatusProposta());
            ps.setString(6, proposta.getStatusAutorizacao());
            ps.setTimestamp(7, proposta.getDataAutorizacao() == null
                    ? null : Timestamp.valueOf(proposta.getDataAutorizacao()));
            ps.setString(8, proposta.getStatusAceitePaciente());
            ps.setTimestamp(9, proposta.getDataResposta() == null
                    ? null : Timestamp.valueOf(proposta.getDataResposta()));
            ps.setTimestamp(10, proposta.getDataInicioReserva() == null
                    ? null : Timestamp.valueOf(proposta.getDataInicioReserva()));
            ps.setTimestamp(11, proposta.getDataFimReserva() == null
                    ? null : Timestamp.valueOf(proposta.getDataFimReserva()));
            ps.setTimestamp(12, proposta.getDataLimiteAceite() == null
                    ? null : Timestamp.valueOf(proposta.getDataLimiteAceite()));
            ps.setTimestamp(13, proposta.getDataConfirmacao() == null
                    ? null : Timestamp.valueOf(proposta.getDataConfirmacao()));
            ps.setLong(14, proposta.getIdSolicitacao());
            ps.setLong(15, proposta.getIdHorario());
            if (proposta.getIdAnalista() == null) {
                ps.setNull(16, Types.NUMERIC);
            } else {
                ps.setLong(16, proposta.getIdAnalista());
            }
            if (proposta.getIdAutorizador() == null) {
                ps.setNull(17, Types.NUMERIC);
            } else {
                ps.setLong(17, proposta.getIdAutorizador());
            }
            if (ps.executeUpdate() == 0) {
                return null;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    System.out.println("O Oracle não retornou o identificador de tb_proposta_atendimento.");
                    return null;
                }
                long idGerado = rs.getLong(1);
                if (rs.wasNull()) {
                    System.out.println("O Oracle retornou um identificador nulo para tb_proposta_atendimento.");
                    return null;
                }
                proposta.setIdProposta(idGerado);
            }
            return proposta;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir proposta: " + e.getMessage());
            return null;
        }
    }

    public PropostaAtendimentoTO atualizar(Connection conexao, PropostaAtendimentoTO proposta) {
        String sql = "update tb_proposta_atendimento set TP_ALTERNATIVA = ?, DS_MODALIDADE = ?, DS_JUSTIFICATIVA "
                + "= ?, DT_CRIACAO = ?, ST_PROPOSTA = ?, ST_AUTORIZACAO = ?, DT_AUTORIZACAO = ?, "
                + "ST_ACEITE_PACIENTE = ?, DT_RESPOSTA = ?, DT_INICIO_RESERVA = ?, DT_FIM_RESERVA = ?, "
                + "DT_LIMITE_ACEITE = ?, DT_CONFIRMACAO = ?, CD_SOLICITACAO = ?, CD_HORARIO = ?, CD_ANALISTA "
                + "= ?, CD_AUTORIZADOR = ? where CD_PROPOSTA = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, proposta.getTipoAlternativa());
            ps.setString(2, proposta.getModalidade());
            ps.setString(3, proposta.getJustificativa());
            ps.setTimestamp(4, proposta.getDataCriacao() == null
                    ? null : Timestamp.valueOf(proposta.getDataCriacao()));
            ps.setString(5, proposta.getStatusProposta());
            ps.setString(6, proposta.getStatusAutorizacao());
            ps.setTimestamp(7, proposta.getDataAutorizacao() == null
                    ? null : Timestamp.valueOf(proposta.getDataAutorizacao()));
            ps.setString(8, proposta.getStatusAceitePaciente());
            ps.setTimestamp(9, proposta.getDataResposta() == null
                    ? null : Timestamp.valueOf(proposta.getDataResposta()));
            ps.setTimestamp(10, proposta.getDataInicioReserva() == null
                    ? null : Timestamp.valueOf(proposta.getDataInicioReserva()));
            ps.setTimestamp(11, proposta.getDataFimReserva() == null
                    ? null : Timestamp.valueOf(proposta.getDataFimReserva()));
            ps.setTimestamp(12, proposta.getDataLimiteAceite() == null
                    ? null : Timestamp.valueOf(proposta.getDataLimiteAceite()));
            ps.setTimestamp(13, proposta.getDataConfirmacao() == null
                    ? null : Timestamp.valueOf(proposta.getDataConfirmacao()));
            ps.setLong(14, proposta.getIdSolicitacao());
            ps.setLong(15, proposta.getIdHorario());
            if (proposta.getIdAnalista() == null) {
                ps.setNull(16, Types.NUMERIC);
            } else {
                ps.setLong(16, proposta.getIdAnalista());
            }
            if (proposta.getIdAutorizador() == null) {
                ps.setNull(17, Types.NUMERIC);
            } else {
                ps.setLong(17, proposta.getIdAutorizador());
            }
            ps.setLong(18, proposta.getIdProposta());
            return ps.executeUpdate() > 0 ? proposta : null;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar proposta: " + e.getMessage());
            return null;
        }
    }

    public boolean excluir(Long idProposta) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return false;
        }
        String sql = "delete from tb_proposta_atendimento where CD_PROPOSTA = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idProposta);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir proposta: " + e.getMessage());
            return false;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public ArrayList<PropostaAtendimentoTO> listarPorSolicitacao(Connection conexao, Long idSolicitacao) {
        ArrayList<PropostaAtendimentoTO> registros = new ArrayList<>();
        String sql = "select * from tb_proposta_atendimento where CD_SOLICITACAO = ? order by CD_PROPOSTA";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idSolicitacao);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PropostaAtendimentoTO proposta = new PropostaAtendimentoTO();
                    proposta.setIdProposta(rs.getLong("CD_PROPOSTA"));
                    proposta.setTipoAlternativa(rs.getString("TP_ALTERNATIVA"));
                    proposta.setModalidade(rs.getString("DS_MODALIDADE"));
                    proposta.setJustificativa(rs.getString("DS_JUSTIFICATIVA"));
                    Timestamp dataCriacao = rs.getTimestamp("DT_CRIACAO");
                    proposta.setDataCriacao(dataCriacao == null ? null : dataCriacao.toLocalDateTime());
                    proposta.setStatusProposta(rs.getString("ST_PROPOSTA"));
                    proposta.setStatusAutorizacao(rs.getString("ST_AUTORIZACAO"));
                    Timestamp dataAutorizacao = rs.getTimestamp("DT_AUTORIZACAO");
                    proposta.setDataAutorizacao(dataAutorizacao == null ? null : dataAutorizacao.toLocalDateTime());
                    proposta.setStatusAceitePaciente(rs.getString("ST_ACEITE_PACIENTE"));
                    Timestamp dataResposta = rs.getTimestamp("DT_RESPOSTA");
                    proposta.setDataResposta(dataResposta == null ? null : dataResposta.toLocalDateTime());
                    Timestamp dataInicioReserva = rs.getTimestamp("DT_INICIO_RESERVA");
                    proposta.setDataInicioReserva(dataInicioReserva == null ? null : dataInicioReserva.toLocalDateTime());
                    Timestamp dataFimReserva = rs.getTimestamp("DT_FIM_RESERVA");
                    proposta.setDataFimReserva(dataFimReserva == null ? null : dataFimReserva.toLocalDateTime());
                    Timestamp dataLimiteAceite = rs.getTimestamp("DT_LIMITE_ACEITE");
                    proposta.setDataLimiteAceite(dataLimiteAceite == null ? null : dataLimiteAceite.toLocalDateTime());
                    Timestamp dataConfirmacao = rs.getTimestamp("DT_CONFIRMACAO");
                    proposta.setDataConfirmacao(dataConfirmacao == null ? null : dataConfirmacao.toLocalDateTime());
                    proposta.setIdSolicitacao(rs.getLong("CD_SOLICITACAO"));
                    proposta.setIdHorario(rs.getLong("CD_HORARIO"));
                    long idAnalistaLido = rs.getLong("CD_ANALISTA");
                    proposta.setIdAnalista(rs.wasNull() ? null : idAnalistaLido);
                    long idAutorizadorLido = rs.getLong("CD_AUTORIZADOR");
                    proposta.setIdAutorizador(rs.wasNull() ? null : idAutorizadorLido);
                    registros.add(proposta);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar propostas por solicitação: " + e.getMessage());
            return null;
        }
        return registros;
    }

    // Verifica reservas vigentes. A BO bloqueia o horário antes de ofertar ou ocupar a vaga.
    public Boolean existeReservaAtivaPorHorario(Connection conexao, Long idHorario, LocalDateTime referencia,
                                                Long idPropostaIgnorada) {
        String sql = "select 1 from tb_proposta_atendimento p "
                + "join tb_solicitacao_ajuda s on s.CD_SOLICITACAO = p.CD_SOLICITACAO "
                + "join tb_agendamento a on a.CD_AGENDAMENTO = s.CD_AGENDAMENTO "
                + "where p.CD_HORARIO = ? and p.ST_PROPOSTA = 'OFERTADA' "
                + "and s.ST_SOLICITACAO = 'EM_ANALISE' and a.ST_AGENDAMENTO = 'AGENDADO' "
                + "and p.DT_INICIO_RESERVA <= ? and p.DT_FIM_RESERVA > ? and p.DT_LIMITE_ACEITE > ?";
        if (idPropostaIgnorada != null) {
            sql += " and p.CD_PROPOSTA <> ?";
        }
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            Timestamp instante = Timestamp.valueOf(referencia);
            ps.setLong(1, idHorario);
            ps.setTimestamp(2, instante);
            ps.setTimestamp(3, instante);
            ps.setTimestamp(4, instante);
            if (idPropostaIgnorada != null) {
                ps.setLong(5, idPropostaIgnorada);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar reservas do horário: " + e.getMessage());
            return null;
        }
    }

    public ArrayList<PropostaAtendimentoTO> listarPorHorario(Long idHorario) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<PropostaAtendimentoTO> registros = new ArrayList<>();
        String sql = "select * from tb_proposta_atendimento where CD_HORARIO = ? order by CD_PROPOSTA";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idHorario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PropostaAtendimentoTO proposta = new PropostaAtendimentoTO();
                    proposta.setIdProposta(rs.getLong("CD_PROPOSTA"));
                    proposta.setTipoAlternativa(rs.getString("TP_ALTERNATIVA"));
                    proposta.setModalidade(rs.getString("DS_MODALIDADE"));
                    proposta.setJustificativa(rs.getString("DS_JUSTIFICATIVA"));
                    Timestamp dataCriacao = rs.getTimestamp("DT_CRIACAO");
                    proposta.setDataCriacao(dataCriacao == null ? null : dataCriacao.toLocalDateTime());
                    proposta.setStatusProposta(rs.getString("ST_PROPOSTA"));
                    proposta.setStatusAutorizacao(rs.getString("ST_AUTORIZACAO"));
                    Timestamp dataAutorizacao = rs.getTimestamp("DT_AUTORIZACAO");
                    proposta.setDataAutorizacao(dataAutorizacao == null ? null : dataAutorizacao.toLocalDateTime());
                    proposta.setStatusAceitePaciente(rs.getString("ST_ACEITE_PACIENTE"));
                    Timestamp dataResposta = rs.getTimestamp("DT_RESPOSTA");
                    proposta.setDataResposta(dataResposta == null ? null : dataResposta.toLocalDateTime());
                    Timestamp dataInicioReserva = rs.getTimestamp("DT_INICIO_RESERVA");
                    proposta.setDataInicioReserva(dataInicioReserva == null ? null : dataInicioReserva.toLocalDateTime());
                    Timestamp dataFimReserva = rs.getTimestamp("DT_FIM_RESERVA");
                    proposta.setDataFimReserva(dataFimReserva == null ? null : dataFimReserva.toLocalDateTime());
                    Timestamp dataLimiteAceite = rs.getTimestamp("DT_LIMITE_ACEITE");
                    proposta.setDataLimiteAceite(dataLimiteAceite == null ? null : dataLimiteAceite.toLocalDateTime());
                    Timestamp dataConfirmacao = rs.getTimestamp("DT_CONFIRMACAO");
                    proposta.setDataConfirmacao(dataConfirmacao == null ? null : dataConfirmacao.toLocalDateTime());
                    proposta.setIdSolicitacao(rs.getLong("CD_SOLICITACAO"));
                    proposta.setIdHorario(rs.getLong("CD_HORARIO"));
                    long idAnalistaLido = rs.getLong("CD_ANALISTA");
                    proposta.setIdAnalista(rs.wasNull() ? null : idAnalistaLido);
                    long idAutorizadorLido = rs.getLong("CD_AUTORIZADOR");
                    proposta.setIdAutorizador(rs.wasNull() ? null : idAutorizadorLido);
                    registros.add(proposta);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar propostas por horário: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }
}
