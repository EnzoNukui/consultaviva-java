package br.com.fiap.dao;

import br.com.fiap.to.HorarioAtendimentoTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class HorarioAtendimentoDAO {

    public ArrayList<HorarioAtendimentoTO> listarTodos() {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<HorarioAtendimentoTO> registros = new ArrayList<>();
        String sql = "select * from tb_horario_atendimento order by CD_HORARIO";
        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                HorarioAtendimentoTO horario = new HorarioAtendimentoTO();
                horario.setIdHorario(rs.getLong("CD_HORARIO"));
                Timestamp dataInicio = rs.getTimestamp("DT_INICIO");
                horario.setDataInicio(dataInicio == null ? null : dataInicio.toLocalDateTime());
                Timestamp dataFim = rs.getTimestamp("DT_FIM");
                horario.setDataFim(dataFim == null ? null : dataFim.toLocalDateTime());
                horario.setIdUnidade(rs.getLong("CD_UNIDADE"));
                horario.setIdMembroEquipe(rs.getLong("CD_MEMBRO_EQUIPE"));
                registros.add(horario);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar horários: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    public HorarioAtendimentoTO buscarPorId(Long idHorario) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return null;
        }
        String sql = "select * from tb_horario_atendimento where CD_HORARIO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idHorario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    HorarioAtendimentoTO horario = new HorarioAtendimentoTO();
                    horario.setIdHorario(rs.getLong("CD_HORARIO"));
                    Timestamp dataInicio = rs.getTimestamp("DT_INICIO");
                    horario.setDataInicio(dataInicio == null ? null : dataInicio.toLocalDateTime());
                    Timestamp dataFim = rs.getTimestamp("DT_FIM");
                    horario.setDataFim(dataFim == null ? null : dataFim.toLocalDateTime());
                    horario.setIdUnidade(rs.getLong("CD_UNIDADE"));
                    horario.setIdMembroEquipe(rs.getLong("CD_MEMBRO_EQUIPE"));
                    return horario;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar horário: " + e.getMessage());
        } finally {
            ConnectionFactory.closeConnection();
        }
        return null;
    }

    // A BO mantém a transação aberta até concluir a operação que depende deste bloqueio.
    public HorarioAtendimentoTO buscarPorIdParaAtualizar(Connection conexao, Long idHorario) {
        try {
            if (conexao.getAutoCommit()) {
                System.out.println("O bloqueio do registro exige uma transação com autoCommit desativado.");
                return null;
            }
            String sql = "select * from tb_horario_atendimento where CD_HORARIO = ? for update";
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setLong(1, idHorario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        HorarioAtendimentoTO horario = new HorarioAtendimentoTO();
                        horario.setIdHorario(rs.getLong("CD_HORARIO"));
                        Timestamp dataInicio = rs.getTimestamp("DT_INICIO");
                        horario.setDataInicio(dataInicio == null ? null : dataInicio.toLocalDateTime());
                        Timestamp dataFim = rs.getTimestamp("DT_FIM");
                        horario.setDataFim(dataFim == null ? null : dataFim.toLocalDateTime());
                        horario.setIdUnidade(rs.getLong("CD_UNIDADE"));
                        horario.setIdMembroEquipe(rs.getLong("CD_MEMBRO_EQUIPE"));
                        return horario;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao bloquear horário: " + e.getMessage());
        }
        return null;
    }

    public HorarioAtendimentoTO inserir(Connection conexao, HorarioAtendimentoTO horario) {
        String sql = "insert into tb_horario_atendimento (DT_INICIO, DT_FIM, CD_UNIDADE, "
                + "CD_MEMBRO_EQUIPE) values (?, ?, ?, ?)";
        // A coluna identity é omitida do INSERT e solicitada explicitamente ao driver Oracle.
        try (PreparedStatement ps = conexao.prepareStatement(sql, new String[]{"CD_HORARIO"})) {
            ps.setTimestamp(1, horario.getDataInicio() == null ? null : Timestamp.valueOf(horario.getDataInicio()));
            ps.setTimestamp(2, horario.getDataFim() == null ? null : Timestamp.valueOf(horario.getDataFim()));
            ps.setLong(3, horario.getIdUnidade());
            ps.setLong(4, horario.getIdMembroEquipe());
            if (ps.executeUpdate() == 0) {
                return null;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    System.out.println("O Oracle não retornou o identificador de tb_horario_atendimento.");
                    return null;
                }
                long idGerado = rs.getLong(1);
                if (rs.wasNull()) {
                    System.out.println("O Oracle retornou um identificador nulo para tb_horario_atendimento.");
                    return null;
                }
                horario.setIdHorario(idGerado);
            }
            return horario;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir horário: " + e.getMessage());
            return null;
        }
    }

    public HorarioAtendimentoTO atualizar(Connection conexao, HorarioAtendimentoTO horario) {
        String sql = "update tb_horario_atendimento set DT_INICIO = ?, DT_FIM = ?, CD_UNIDADE = ?, "
                + "CD_MEMBRO_EQUIPE = ? where CD_HORARIO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setTimestamp(1, horario.getDataInicio() == null ? null : Timestamp.valueOf(horario.getDataInicio()));
            ps.setTimestamp(2, horario.getDataFim() == null ? null : Timestamp.valueOf(horario.getDataFim()));
            ps.setLong(3, horario.getIdUnidade());
            ps.setLong(4, horario.getIdMembroEquipe());
            ps.setLong(5, horario.getIdHorario());
            return ps.executeUpdate() > 0 ? horario : null;
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar horário: " + e.getMessage());
            return null;
        }
    }

    public boolean excluir(Long idHorario) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return false;
        }
        String sql = "delete from tb_horario_atendimento where CD_HORARIO = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idHorario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir horário: " + e.getMessage());
            return false;
        } finally {
            ConnectionFactory.closeConnection();
        }
    }

    public ArrayList<HorarioAtendimentoTO> listarPorUnidade(Long idUnidade) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<HorarioAtendimentoTO> registros = new ArrayList<>();
        String sql = "select * from tb_horario_atendimento where CD_UNIDADE = ? order by CD_HORARIO";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idUnidade);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HorarioAtendimentoTO horario = new HorarioAtendimentoTO();
                    horario.setIdHorario(rs.getLong("CD_HORARIO"));
                    Timestamp dataInicio = rs.getTimestamp("DT_INICIO");
                    horario.setDataInicio(dataInicio == null ? null : dataInicio.toLocalDateTime());
                    Timestamp dataFim = rs.getTimestamp("DT_FIM");
                    horario.setDataFim(dataFim == null ? null : dataFim.toLocalDateTime());
                    horario.setIdUnidade(rs.getLong("CD_UNIDADE"));
                    horario.setIdMembroEquipe(rs.getLong("CD_MEMBRO_EQUIPE"));
                    registros.add(horario);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar horários por unidade: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    public ArrayList<HorarioAtendimentoTO> listarPorMembroEquipe(Long idMembroEquipe) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<HorarioAtendimentoTO> registros = new ArrayList<>();
        String sql = "select * from tb_horario_atendimento where CD_MEMBRO_EQUIPE = ? order by CD_HORARIO";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idMembroEquipe);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HorarioAtendimentoTO horario = new HorarioAtendimentoTO();
                    horario.setIdHorario(rs.getLong("CD_HORARIO"));
                    Timestamp dataInicio = rs.getTimestamp("DT_INICIO");
                    horario.setDataInicio(dataInicio == null ? null : dataInicio.toLocalDateTime());
                    Timestamp dataFim = rs.getTimestamp("DT_FIM");
                    horario.setDataFim(dataFim == null ? null : dataFim.toLocalDateTime());
                    horario.setIdUnidade(rs.getLong("CD_UNIDADE"));
                    horario.setIdMembroEquipe(rs.getLong("CD_MEMBRO_EQUIPE"));
                    registros.add(horario);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar horários por membro da equipe: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    public ArrayList<HorarioAtendimentoTO> listarDisponiveis(LocalDateTime referencia) {
        Connection conexao = ConnectionFactory.getConnection();
        if (conexao == null) {
            return new ArrayList<>();
        }
        ArrayList<HorarioAtendimentoTO> registros = new ArrayList<>();
        String sql = "select h.* from tb_horario_atendimento h "
                + "join tb_membro_equipe m on m.CD_MEMBRO_EQUIPE = h.CD_MEMBRO_EQUIPE "
                + "where h.DT_INICIO > ? and m.IN_ATENDIMENTO = 'S' "
                + "and not exists (select 1 from tb_agendamento a "
                + "where a.CD_HORARIO = h.CD_HORARIO and a.ST_AGENDAMENTO = 'AGENDADO') "
                + "and not exists (select 1 from tb_proposta_atendimento p "
                + "join tb_solicitacao_ajuda s on s.CD_SOLICITACAO = p.CD_SOLICITACAO "
                + "join tb_agendamento original on original.CD_AGENDAMENTO = s.CD_AGENDAMENTO "
                + "where p.CD_HORARIO = h.CD_HORARIO and p.ST_PROPOSTA = 'OFERTADA' "
                + "and s.ST_SOLICITACAO = 'EM_ANALISE' and original.ST_AGENDAMENTO = 'AGENDADO' "
                + "and p.DT_INICIO_RESERVA <= ? and p.DT_FIM_RESERVA > ? and p.DT_LIMITE_ACEITE > ?) "
                + "order by h.DT_INICIO, h.CD_HORARIO";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            Timestamp instante = Timestamp.valueOf(referencia);
            ps.setTimestamp(1, instante);
            ps.setTimestamp(2, instante);
            ps.setTimestamp(3, instante);
            ps.setTimestamp(4, instante);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HorarioAtendimentoTO horario = new HorarioAtendimentoTO();
                    horario.setIdHorario(rs.getLong("CD_HORARIO"));
                    Timestamp dataInicio = rs.getTimestamp("DT_INICIO");
                    horario.setDataInicio(dataInicio == null ? null : dataInicio.toLocalDateTime());
                    Timestamp dataFim = rs.getTimestamp("DT_FIM");
                    horario.setDataFim(dataFim == null ? null : dataFim.toLocalDateTime());
                    horario.setIdUnidade(rs.getLong("CD_UNIDADE"));
                    horario.setIdMembroEquipe(rs.getLong("CD_MEMBRO_EQUIPE"));
                    registros.add(horario);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar horários disponíveis: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            ConnectionFactory.closeConnection();
        }
        return registros;
    }

    public ArrayList<HorarioAtendimentoTO> listarCandidatos(Connection conexao, Long idUnidade,
                                                          LocalDateTime referencia, Long idAgendamento) {
        ArrayList<HorarioAtendimentoTO> registros = new ArrayList<>();
        String sql = "select h.* from tb_horario_atendimento h "
                + "join tb_membro_equipe m on m.CD_MEMBRO_EQUIPE = h.CD_MEMBRO_EQUIPE "
                + "where h.CD_UNIDADE = ? and h.DT_INICIO > ? and m.IN_ATENDIMENTO = 'S' "
                + "and not exists (select 1 from tb_agendamento a "
                + "where a.CD_HORARIO = h.CD_HORARIO and a.ST_AGENDAMENTO = 'AGENDADO' "
                + "and a.CD_AGENDAMENTO <> ?) "
                + "order by h.DT_INICIO, h.CD_HORARIO";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, idUnidade);
            ps.setTimestamp(2, Timestamp.valueOf(referencia));
            ps.setLong(3, idAgendamento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HorarioAtendimentoTO horario = new HorarioAtendimentoTO();
                    horario.setIdHorario(rs.getLong("CD_HORARIO"));
                    Timestamp dataInicio = rs.getTimestamp("DT_INICIO");
                    horario.setDataInicio(dataInicio == null ? null : dataInicio.toLocalDateTime());
                    Timestamp dataFim = rs.getTimestamp("DT_FIM");
                    horario.setDataFim(dataFim == null ? null : dataFim.toLocalDateTime());
                    horario.setIdUnidade(rs.getLong("CD_UNIDADE"));
                    horario.setIdMembroEquipe(rs.getLong("CD_MEMBRO_EQUIPE"));
                    registros.add(horario);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar horários candidatos: " + e.getMessage());
            return null;
        }
        return registros;
    }

    // A BO consulta depois de bloquear o membro da equipe. Null indica falha na consulta.
    public Boolean existeSobreposicao(Connection conexao, HorarioAtendimentoTO horario, Long idHorarioIgnorado) {
        String sql = "select 1 from tb_horario_atendimento where CD_MEMBRO_EQUIPE = ? "
                + "and DT_INICIO < ? and DT_FIM > ?";
        if (idHorarioIgnorado != null) {
            sql += " and CD_HORARIO <> ?";
        }
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, horario.getIdMembroEquipe());
            ps.setTimestamp(2, Timestamp.valueOf(horario.getDataFim()));
            ps.setTimestamp(3, Timestamp.valueOf(horario.getDataInicio()));
            if (idHorarioIgnorado != null) {
                ps.setLong(4, idHorarioIgnorado);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar sobreposição de horários: " + e.getMessage());
            return null;
        }
    }
}

