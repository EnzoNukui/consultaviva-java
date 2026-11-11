package br.com.fiap.bo;

import br.com.fiap.dao.ConnectionFactory;
import br.com.fiap.dao.UnidadeDAO;
import br.com.fiap.exception.RegraNegocioException;
import br.com.fiap.to.HorarioAtendimentoTO;
import br.com.fiap.to.UnidadeTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HorarioAtendimentoBOTests {
    private final Connection conexao = mock(Connection.class);
    private final LocalDateTime inicio = LocalDateTime.of(2027, 2, 1, 10, 0);
    private final HorarioAtendimentoTO horario = new HorarioAtendimentoTO(null, inicio,
            inicio.plusMinutes(30), 1L, 2L);

    @Test
    void rejeitaHorarioQueTerminaAntesDoInicio() {
        horario.setDataFim(inicio.minusMinutes(1));

        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            assertThrows(RegraNegocioException.class, () -> new HorarioAtendimentoBO().inserir(horario));
            factory.verifyNoInteractions();
        }
    }

    @Test
    void bloqueiaProfissionalAntesDeVerificarSobreposicaoEGravaNaMesmaConexao() throws SQLException {
        simularProfissional("S");
        simularSobreposicao(false);
        PreparedStatement insert = mock(PreparedStatement.class);
        ResultSet chaves = mock(ResultSet.class);
        when(conexao.prepareStatement(startsWith("insert into tb_horario_atendimento"), any(String[].class)))
                .thenReturn(insert);
        when(insert.executeUpdate()).thenReturn(1);
        when(insert.getGeneratedKeys()).thenReturn(chaves);
        when(chaves.next()).thenReturn(true);
        when(chaves.getLong(1)).thenReturn(5_000_000_000L);

        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class);
             MockedConstruction<UnidadeDAO> unidades = simularUnidade()) {
            factory.when(ConnectionFactory::getNewConnection).thenReturn(conexao);

            HorarioAtendimentoTO resultado = new HorarioAtendimentoBO().inserir(horario);

            assertSame(horario, resultado);
            assertEquals(5_000_000_000L, resultado.getIdHorario());
        }

        ArgumentCaptor<String> sqlInsert = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String[]> colunaGerada = ArgumentCaptor.forClass(String[].class);
        InOrder ordem = inOrder(conexao, insert);
        ordem.verify(conexao).setAutoCommit(false);
        ordem.verify(conexao).prepareStatement(contains("tb_membro_equipe where CD_MEMBRO_EQUIPE = ? for update"));
        ordem.verify(conexao).prepareStatement(contains("DT_INICIO < ? and DT_FIM > ?"));
        ordem.verify(conexao).prepareStatement(sqlInsert.capture(), colunaGerada.capture());
        ordem.verify(insert).executeUpdate();
        ordem.verify(conexao).commit();
        ordem.verify(conexao).close();
        assertFalse(sqlInsert.getValue().contains("CD_HORARIO"));
        assertArrayEquals(new String[]{"CD_HORARIO"}, colunaGerada.getValue());
        verify(conexao, never()).rollback();
    }

    @Test
    void sobreposicaoDesfazTransacaoAntesDaGravacao() throws SQLException {
        simularProfissional("S");
        PreparedStatement conflito = simularSobreposicao(true);

        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class);
             MockedConstruction<UnidadeDAO> unidades = simularUnidade()) {
            factory.when(ConnectionFactory::getNewConnection).thenReturn(conexao);

            assertThrows(RegraNegocioException.class, () -> new HorarioAtendimentoBO().inserir(horario));
        }

        verify(conflito).setLong(1, 2L);
        verify(conflito).setTimestamp(2, Timestamp.valueOf(horario.getDataFim()));
        verify(conflito).setTimestamp(3, Timestamp.valueOf(horario.getDataInicio()));
        verify(conexao, never()).prepareStatement(startsWith("insert into"), any(String[].class));
        verify(conexao).rollback();
        verify(conexao, never()).commit();
    }

    @Test
    void horarioVinculadoAAgendamentoNaoPodeSerEditado() throws SQLException {
        horario.setIdHorario(10L);
        PreparedStatement buscarHorario = mock(PreparedStatement.class);
        ResultSet horarioEncontrado = mock(ResultSet.class);
        when(conexao.prepareStatement(contains("CD_HORARIO = ? for update"))).thenReturn(buscarHorario);
        when(buscarHorario.executeQuery()).thenReturn(horarioEncontrado);
        when(horarioEncontrado.next()).thenReturn(true);
        PreparedStatement buscarAgendamento = mock(PreparedStatement.class);
        ResultSet agendamentoEncontrado = mock(ResultSet.class);
        when(conexao.prepareStatement(startsWith("select 1 from tb_agendamento"))).thenReturn(buscarAgendamento);
        when(buscarAgendamento.executeQuery()).thenReturn(agendamentoEncontrado);
        when(agendamentoEncontrado.next()).thenReturn(true);

        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class);
             MockedConstruction<UnidadeDAO> unidades = simularUnidade()) {
            factory.when(ConnectionFactory::getNewConnection).thenReturn(conexao);

            assertThrows(RegraNegocioException.class, () -> new HorarioAtendimentoBO().atualizar(horario));
        }

        verify(conexao, never()).prepareStatement(startsWith("update tb_horario_atendimento"));
        verify(conexao).rollback();
        verify(conexao, never()).commit();
    }

    @Test
    void falhaAoRecuperarIdDesfazCadastro() throws SQLException {
        simularProfissional("S");
        simularSobreposicao(false);
        PreparedStatement insert = mock(PreparedStatement.class);
        ResultSet chaves = mock(ResultSet.class);
        when(conexao.prepareStatement(startsWith("insert into tb_horario_atendimento"), any(String[].class)))
                .thenReturn(insert);
        when(insert.executeUpdate()).thenReturn(1);
        when(insert.getGeneratedKeys()).thenReturn(chaves);

        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class);
             MockedConstruction<UnidadeDAO> unidades = simularUnidade()) {
            factory.when(ConnectionFactory::getNewConnection).thenReturn(conexao);

            assertNull(new HorarioAtendimentoBO().inserir(horario));
        }

        assertNull(horario.getIdHorario());
        verify(conexao).rollback();
        verify(conexao, never()).commit();
        verify(conexao).close();
    }

    private MockedConstruction<UnidadeDAO> simularUnidade() {
        return mockConstruction(UnidadeDAO.class, (dao, contexto) ->
                when(dao.buscarPorId(1L)).thenReturn(new UnidadeTO(1L, "Unidade teste", "Endereço teste")));
    }

    private void simularProfissional(String atendimento) throws SQLException {
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conexao.prepareStatement(contains("tb_membro_equipe where CD_MEMBRO_EQUIPE = ? for update")))
                .thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getString("IN_ATENDIMENTO")).thenReturn(atendimento);
    }

    private PreparedStatement simularSobreposicao(boolean existe) throws SQLException {
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conexao.prepareStatement(contains("DT_INICIO < ? and DT_FIM > ?"))).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(existe);
        return ps;
    }
}
