package br.com.fiap.dao;

import br.com.fiap.to.PlanoComparecimentoTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;

public class PlanoComparecimentoDAO {

    public ArrayList<PlanoComparecimentoTO> listarTodos() {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<PlanoComparecimentoTO> registros = new ArrayList<>();
        String sql = "select * from tb_plano_comparecimento order by CD_PLANO";
        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PlanoComparecimentoTO plano = new PlanoComparecimentoTO();
                plano.setIdPlano(rs.getLong("CD_PLANO"));
                Timestamp dataCalculo = rs.getTimestamp("DT_CALCULO");
                plano.setDataCalculo(dataCalculo == null ? null : dataCalculo.toLocalDateTime());
                plano.setOrigemTrajeto(rs.getString("DS_ORIGEM_TRAJETO"));
                plano.setDestinoTrajeto(rs.getString("DS_DESTINO_TRAJETO"));
                plano.setMeioTransporte(rs.getString("DS_MEIO_TRANSPORTE"));
                plano.setNomeFonte(rs.getString("NM_FONTE"));
                plano.setDuracaoMinutos(rs.getInt("NR_DURACAO_MIN"));
                plano.setMargemMinutos(rs.getInt("NR_MARGEM_MIN"));
                Timestamp dataAtendimentoReferencia = rs.getTimestamp("DT_ATENDIMENTO_REFERENCIA");
                plano.setDataAtendimentoReferencia(dataAtendimentoReferencia == null ? null : dataAtendimentoReferencia.toLocalDateTime());
                Timestamp dataSaidaSugerida = rs.getTimestamp("DT_SAIDA_SUGERIDA");
                plano.setDataSaidaSugerida(dataSaidaSugerida == null ? null : dataSaidaSugerida.toLocalDateTime());
                plano.setSimulado(rs.getString("IN_SIMULADO"));
                plano.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                long idPropostaLido = rs.getLong("CD_PROPOSTA");
                plano.setIdProposta(rs.wasNull() ? null : idPropostaLido);
                registros.add(plano);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar planos de comparecimento: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    public PlanoComparecimentoTO buscarPorId(Long idPlano) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "select * from tb_plano_comparecimento where CD_PLANO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idPlano);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PlanoComparecimentoTO plano = new PlanoComparecimentoTO();
                    plano.setIdPlano(rs.getLong("CD_PLANO"));
                    Timestamp dataCalculo = rs.getTimestamp("DT_CALCULO");
                    plano.setDataCalculo(dataCalculo == null ? null : dataCalculo.toLocalDateTime());
                    plano.setOrigemTrajeto(rs.getString("DS_ORIGEM_TRAJETO"));
                    plano.setDestinoTrajeto(rs.getString("DS_DESTINO_TRAJETO"));
                    plano.setMeioTransporte(rs.getString("DS_MEIO_TRANSPORTE"));
                    plano.setNomeFonte(rs.getString("NM_FONTE"));
                    plano.setDuracaoMinutos(rs.getInt("NR_DURACAO_MIN"));
                    plano.setMargemMinutos(rs.getInt("NR_MARGEM_MIN"));
                    Timestamp dataAtendimentoReferencia = rs.getTimestamp("DT_ATENDIMENTO_REFERENCIA");
                    plano.setDataAtendimentoReferencia(dataAtendimentoReferencia == null ? null : dataAtendimentoReferencia.toLocalDateTime());
                    Timestamp dataSaidaSugerida = rs.getTimestamp("DT_SAIDA_SUGERIDA");
                    plano.setDataSaidaSugerida(dataSaidaSugerida == null ? null : dataSaidaSugerida.toLocalDateTime());
                    plano.setSimulado(rs.getString("IN_SIMULADO"));
                    plano.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                    long idPropostaLido = rs.getLong("CD_PROPOSTA");
                    plano.setIdProposta(rs.wasNull() ? null : idPropostaLido);
                    return plano;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar plano de comparecimento: " + e.getMessage());
        } finally {
            ConnectionFactory.closeConnection();
        }
        return null;
    }

    public PlanoComparecimentoTO inserir(Connection conexao, PlanoComparecimentoTO plano) {
        String sql = "insert into tb_plano_comparecimento (DT_CALCULO, DS_ORIGEM_TRAJETO, DS_DESTINO_TRAJETO, "
                + "DS_MEIO_TRANSPORTE, NM_FONTE, NR_DURACAO_MIN, NR_MARGEM_MIN, DT_ATENDIMENTO_REFERENCIA, "
                + "DT_SAIDA_SUGERIDA, IN_SIMULADO, CD_AGENDAMENTO, CD_PROPOSTA) values (?, ?, ?, ?, ?, ?, ?, "
                + "?, ?, ?, ?, ?)";
        // A coluna identity é omitida do INSERT e solicitada explicitamente ao driver Oracle.
        try (PreparedStatement ps = conexao.prepareStatement(sql, new String[]{"CD_PLANO"})) {
            ps.setTimestamp(1, plano.getDataCalculo() == null
                    ? null : Timestamp.valueOf(plano.getDataCalculo()));
            ps.setString(2, plano.getOrigemTrajeto());
            ps.setString(3, plano.getDestinoTrajeto());
            ps.setString(4, plano.getMeioTransporte());
            ps.setString(5, plano.getNomeFonte());
            ps.setInt(6, plano.getDuracaoMinutos());
            ps.setInt(7, plano.getMargemMinutos());
            ps.setTimestamp(8, plano.getDataAtendimentoReferencia() == null
                    ? null : Timestamp.valueOf(plano.getDataAtendimentoReferencia()));
            ps.setTimestamp(9, plano.getDataSaidaSugerida() == null
                    ? null : Timestamp.valueOf(plano.getDataSaidaSugerida()));
            ps.setString(10, plano.getSimulado());
            ps.setLong(11, plano.getIdAgendamento());
            if (plano.getIdProposta() == null) {
                ps.setNull(12, Types.NUMERIC);
            } else {
                ps.setLong(12, plano.getIdProposta());
            }
            if (ps.executeUpdate() == 0) {
                return null;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    System.out.println("O Oracle não retornou o identificador de tb_plano_comparecimento.");
                    return null;
                }
                long idGerado = rs.getLong(1);
                if (rs.wasNull()) {
                    System.out.println("O Oracle retornou um identificador nulo para tb_plano_comparecimento.");
                    return null;
                }
                plano.setIdPlano(idGerado);
            }
            return plano;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir plano de comparecimento: " + e.getMessage());
            return null;
        }
    }

    public PlanoComparecimentoTO atualizar(Connection conexao, PlanoComparecimentoTO plano) {
        String sql = "update tb_plano_comparecimento set DT_CALCULO = ?, DS_ORIGEM_TRAJETO = ?, "
                + "DS_DESTINO_TRAJETO = ?, DS_MEIO_TRANSPORTE = ?, NM_FONTE = ?, NR_DURACAO_MIN = ?, "
                + "NR_MARGEM_MIN = ?, DT_ATENDIMENTO_REFERENCIA = ?, DT_SAIDA_SUGERIDA = ?, IN_SIMULADO = ?, "
                + "CD_AGENDAMENTO = ?, CD_PROPOSTA = ? where CD_PLANO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setTimestamp(1, plano.getDataCalculo() == null
                    ? null : Timestamp.valueOf(plano.getDataCalculo()));
            ps.setString(2, plano.getOrigemTrajeto());
            ps.setString(3, plano.getDestinoTrajeto());
            ps.setString(4, plano.getMeioTransporte());
            ps.setString(5, plano.getNomeFonte());
            ps.setInt(6, plano.getDuracaoMinutos());
            ps.setInt(7, plano.getMargemMinutos());
            ps.setTimestamp(8, plano.getDataAtendimentoReferencia() == null
                    ? null : Timestamp.valueOf(plano.getDataAtendimentoReferencia()));
            ps.setTimestamp(9, plano.getDataSaidaSugerida() == null
                    ? null : Timestamp.valueOf(plano.getDataSaidaSugerida()));
            ps.setString(10, plano.getSimulado());
            ps.setLong(11, plano.getIdAgendamento());
            if (plano.getIdProposta() == null) {
                ps.setNull(12, Types.NUMERIC);
            } else {
                ps.setLong(12, plano.getIdProposta());
            }
            ps.setLong(13, plano.getIdPlano());
            return ps.executeUpdate() > 0 ? plano : null;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar plano de comparecimento: " + e.getMessage());
            return null;
        }
    }

    public boolean excluir(Long idPlano) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return false;
        }
        String sql = "delete from tb_plano_comparecimento where CD_PLANO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idPlano);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir plano de comparecimento: " + e.getMessage());
            return false;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public ArrayList<PlanoComparecimentoTO> listarPorAgendamento(Long idAgendamento) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<PlanoComparecimentoTO> registros = new ArrayList<>();
        String sql = "select * from tb_plano_comparecimento where CD_AGENDAMENTO = ? order by CD_PLANO";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idAgendamento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PlanoComparecimentoTO plano = new PlanoComparecimentoTO();
                    plano.setIdPlano(rs.getLong("CD_PLANO"));
                    Timestamp dataCalculo = rs.getTimestamp("DT_CALCULO");
                    plano.setDataCalculo(dataCalculo == null ? null : dataCalculo.toLocalDateTime());
                    plano.setOrigemTrajeto(rs.getString("DS_ORIGEM_TRAJETO"));
                    plano.setDestinoTrajeto(rs.getString("DS_DESTINO_TRAJETO"));
                    plano.setMeioTransporte(rs.getString("DS_MEIO_TRANSPORTE"));
                    plano.setNomeFonte(rs.getString("NM_FONTE"));
                    plano.setDuracaoMinutos(rs.getInt("NR_DURACAO_MIN"));
                    plano.setMargemMinutos(rs.getInt("NR_MARGEM_MIN"));
                    Timestamp dataAtendimentoReferencia = rs.getTimestamp("DT_ATENDIMENTO_REFERENCIA");
                    plano.setDataAtendimentoReferencia(dataAtendimentoReferencia == null ? null : dataAtendimentoReferencia.toLocalDateTime());
                    Timestamp dataSaidaSugerida = rs.getTimestamp("DT_SAIDA_SUGERIDA");
                    plano.setDataSaidaSugerida(dataSaidaSugerida == null ? null : dataSaidaSugerida.toLocalDateTime());
                    plano.setSimulado(rs.getString("IN_SIMULADO"));
                    plano.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                    long idPropostaLido = rs.getLong("CD_PROPOSTA");
                    plano.setIdProposta(rs.wasNull() ? null : idPropostaLido);
                    registros.add(plano);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar planos de comparecimento por agendamento: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    public ArrayList<PlanoComparecimentoTO> listarPorProposta(Long idProposta) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<PlanoComparecimentoTO> registros = new ArrayList<>();
        String sql = "select * from tb_plano_comparecimento where CD_PROPOSTA = ? order by CD_PLANO";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idProposta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PlanoComparecimentoTO plano = new PlanoComparecimentoTO();
                    plano.setIdPlano(rs.getLong("CD_PLANO"));
                    Timestamp dataCalculo = rs.getTimestamp("DT_CALCULO");
                    plano.setDataCalculo(dataCalculo == null ? null : dataCalculo.toLocalDateTime());
                    plano.setOrigemTrajeto(rs.getString("DS_ORIGEM_TRAJETO"));
                    plano.setDestinoTrajeto(rs.getString("DS_DESTINO_TRAJETO"));
                    plano.setMeioTransporte(rs.getString("DS_MEIO_TRANSPORTE"));
                    plano.setNomeFonte(rs.getString("NM_FONTE"));
                    plano.setDuracaoMinutos(rs.getInt("NR_DURACAO_MIN"));
                    plano.setMargemMinutos(rs.getInt("NR_MARGEM_MIN"));
                    Timestamp dataAtendimentoReferencia = rs.getTimestamp("DT_ATENDIMENTO_REFERENCIA");
                    plano.setDataAtendimentoReferencia(dataAtendimentoReferencia == null ? null : dataAtendimentoReferencia.toLocalDateTime());
                    Timestamp dataSaidaSugerida = rs.getTimestamp("DT_SAIDA_SUGERIDA");
                    plano.setDataSaidaSugerida(dataSaidaSugerida == null ? null : dataSaidaSugerida.toLocalDateTime());
                    plano.setSimulado(rs.getString("IN_SIMULADO"));
                    plano.setIdAgendamento(rs.getLong("CD_AGENDAMENTO"));
                    long idPropostaLido = rs.getLong("CD_PROPOSTA");
                    plano.setIdProposta(rs.wasNull() ? null : idPropostaLido);
                    registros.add(plano);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar planos de comparecimento por proposta: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }
}
