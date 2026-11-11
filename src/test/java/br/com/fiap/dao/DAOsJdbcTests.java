package br.com.fiap.dao;

import br.com.fiap.to.AgendamentoTO;
import br.com.fiap.to.DisponibilidadeTO;
import br.com.fiap.to.HistoricoEventoTO;
import br.com.fiap.to.MembroEquipeTO;
import br.com.fiap.to.PacienteTO;
import br.com.fiap.to.PlanoComparecimentoTO;
import br.com.fiap.to.PreparoVirtualTO;
import br.com.fiap.to.PropostaAtendimentoTO;
import br.com.fiap.to.SincronizacaoAgendaTO;
import br.com.fiap.to.SolicitacaoAjudaTO;
import br.com.fiap.to.SolicitacaoDificuldadeTO;
import br.com.fiap.to.TipoDificuldadeTO;
import br.com.fiap.to.UnidadeTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DAOsJdbcTests {

    @FunctionalInterface
    private interface Bloqueio {
        Object buscar(Connection conexao);
    }

    private static Stream<Arguments> cadastros() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 20, 10, 30);
        return Stream.of(
                Arguments.of("tb_paciente", "CD_PACIENTE", false, (Function<Connection, Long>) conexao -> {
                    PacienteTO paciente = new PacienteTO(99L, "Paciente teste",
                            LocalDate.of(1990, 1, 1), null);
                    paciente = new PacienteDAO().inserir(paciente);
                    return paciente == null ? null : paciente.getIdPaciente();
                }),
                Arguments.of("tb_unidade", "CD_UNIDADE", false, (Function<Connection, Long>) conexao -> {
                    UnidadeTO unidade = new UnidadeTO(99L, "Unidade teste", "Endereço teste");
                    unidade = new UnidadeDAO().inserir(unidade);
                    return unidade == null ? null : unidade.getIdUnidade();
                }),
                Arguments.of("tb_membro_equipe", "CD_MEMBRO_EQUIPE", false, (Function<Connection, Long>) conexao -> {
                    MembroEquipeTO membro = new MembroEquipeTO(99L, "Membro teste", "Profissional",
                            "S", "N", "S", null);
                    membro = new MembroEquipeDAO().inserir(membro);
                    return membro == null ? null : membro.getIdMembroEquipe();
                }),
                Arguments.of("tb_tipo_dificuldade", "CD_DIFICULDADE", false, (Function<Connection, Long>) conexao -> {
                    TipoDificuldadeTO dificuldade = new TipoDificuldadeTO(99L, "Transporte");
                    dificuldade = new TipoDificuldadeDAO().inserir(dificuldade);
                    return dificuldade == null ? null : dificuldade.getIdDificuldade();
                }),
                Arguments.of("tb_agendamento", "CD_AGENDAMENTO", true, (Function<Connection, Long>) conexao -> {
                    AgendamentoTO agendamento = new AgendamentoTO(99L, "PRESENCIAL", "AGENDADO",
                            "SEM_RESPOSTA", 1L, 2L);
                    agendamento = new AgendamentoDAO().inserir(conexao, agendamento);
                    return agendamento == null ? null : agendamento.getIdAgendamento();
                }),
                Arguments.of("tb_solicitacao_ajuda", "CD_SOLICITACAO", true, (Function<Connection, Long>) conexao -> {
                    SolicitacaoAjudaTO solicitacao = new SolicitacaoAjudaTO(99L, inicio,
                            null, null, "ABERTA", 1L, null);
                    solicitacao = new SolicitacaoAjudaDAO().inserir(conexao, solicitacao);
                    return solicitacao == null ? null : solicitacao.getIdSolicitacao();
                }),
                Arguments.of("tb_disponibilidade", "CD_DISPONIBILIDADE", true, (Function<Connection, Long>) conexao -> {
                    DisponibilidadeTO disponibilidade = new DisponibilidadeTO(99L, inicio,
                            inicio.plusHours(1), "AMBAS", 1L);
                    disponibilidade = new DisponibilidadeDAO().inserir(conexao, disponibilidade);
                    return disponibilidade == null ? null : disponibilidade.getIdDisponibilidade();
                }),
                Arguments.of("tb_proposta_atendimento", "CD_PROPOSTA", true, (Function<Connection, Long>) conexao -> {
                    PropostaAtendimentoTO proposta = new PropostaAtendimentoTO(99L, "REMARCACAO", "PRESENCIAL",
                            "Horário disponível", inicio, "EM_ANALISE", "PENDENTE", null, "PENDENTE", null,
                            null, null, null, null, 1L, 2L, null, null);
                    proposta = new PropostaAtendimentoDAO().inserir(conexao, proposta);
                    return proposta == null ? null : proposta.getIdProposta();
                }),
                Arguments.of("tb_historico_evento", "CD_EVENTO", true, (Function<Connection, Long>) conexao -> {
                    HistoricoEventoTO evento = new HistoricoEventoTO(99L, inicio, "ABERTURA",
                            "Solicitação aberta", "SISTEMA", 1L, 2L, null, null);
                    evento = new HistoricoEventoDAO().inserir(conexao, evento);
                    return evento == null ? null : evento.getIdEvento();
                }),
                Arguments.of("tb_plano_comparecimento", "CD_PLANO", true, (Function<Connection, Long>) conexao -> {
                    PlanoComparecimentoTO plano = new PlanoComparecimentoTO(99L, inicio,
                            "Origem", "Unidade", "Ônibus", "Simulação", 30, 10,
                            inicio.plusHours(2), inicio.plusMinutes(80), "S", 1L, null);
                    plano = new PlanoComparecimentoDAO().inserir(conexao, plano);
                    return plano == null ? null : plano.getIdPlano();
                }),
                Arguments.of("tb_preparo_virtual", "CD_PREPARO", true, (Function<Connection, Long>) conexao -> {
                    PreparoVirtualTO preparo = new PreparoVirtualTO(99L, inicio, "PERMITIDA", "PERMITIDA",
                            "OK", "OK", "OK", null, 1L, null);
                    preparo = new PreparoVirtualDAO().inserir(conexao, preparo);
                    return preparo == null ? null : preparo.getIdPreparo();
                }),
                Arguments.of("tb_sincronizacao_agenda", "CD_SINCRONIZACAO", false, (Function<Connection, Long>) conexao -> {
                    SincronizacaoAgendaTO sincronizacao = new SincronizacaoAgendaTO(99L, "Agenda teste", null,
                            null, "CRIAR", "PENDENTE", 0, null, null, null, 1L);
                    sincronizacao = new SincronizacaoAgendaDAO().inserir(sincronizacao);
                    return sincronizacao == null ? null : sincronizacao.getIdSincronizacao();
                })
        );
    }

    @ParameterizedTest(name = "{0}: recupera o ID gerado e respeita a conexão da operação")
    @MethodSource("cadastros")
    void inserirRecuperaIdDoOracleSemEnviarIdInformado(String tabela, String coluna, boolean transacional,
                                                      Function<Connection, Long> cadastro)
            throws SQLException {
        Connection conexao = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet chaves = mock(ResultSet.class);
        when(conexao.prepareStatement(anyString(), any(String[].class))).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);
        when(ps.getGeneratedKeys()).thenReturn(chaves);
        when(chaves.next()).thenReturn(true);
        when(chaves.getLong(1)).thenReturn(5_000_000_000L);

        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conexao);

            assertEquals(5_000_000_000L, cadastro.apply(conexao));

            factory.verify(ConnectionFactory::closeConnection, times(transacional ? 0 : 1));
        }

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String[]> colunas = ArgumentCaptor.forClass(String[].class);
        verify(conexao).prepareStatement(sql.capture(), colunas.capture());
        assertTrue(sql.getValue().startsWith("insert into " + tabela + " ("));
        String colunasInseridas = sql.getValue().substring(sql.getValue().indexOf('(') + 1,
                sql.getValue().indexOf(')'));
        assertFalse(Stream.of(colunasInseridas.split(",")).map(String::trim).anyMatch(coluna::equals));
        assertArrayEquals(new String[]{coluna}, colunas.getValue());
        verify(chaves).close();
        verify(ps).close();
        verify(conexao, never()).close();
        verify(conexao, never()).commit();
        verify(conexao, never()).rollback();
        verify(conexao, never()).setAutoCommit(anyBoolean());
    }

    @ParameterizedTest(name = "{0}: trata a falha no cadastro e respeita a conexão da operação")
    @MethodSource("cadastros")
    void inserirTrataErroSql(String tabela, String coluna, boolean transacional,
                             Function<Connection, Long> cadastro) throws SQLException {
        Connection conexao = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        SQLException falha = new SQLException("Falha de integridade", "23000", 2291);
        when(conexao.prepareStatement(anyString(), any(String[].class))).thenReturn(ps);
        when(ps.executeUpdate()).thenThrow(falha);

        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conexao);

            assertNull(cadastro.apply(conexao));

            factory.verify(ConnectionFactory::closeConnection, times(transacional ? 0 : 1));
        }
        verify(ps).close();
        verify(conexao, never()).close();
        verify(conexao, never()).commit();
        verify(conexao, never()).rollback();
    }

    @Test
    void vinculoUsaAsDuasChavesNoCadastroENaExclusao() throws SQLException {
        Connection conexao = mock(Connection.class);
        PreparedStatement insert = mock(PreparedStatement.class);
        PreparedStatement delete = mock(PreparedStatement.class);
        when(conexao.prepareStatement(startsWith("insert into tb_solicitacao_dificuldade"))).thenReturn(insert);
        when(conexao.prepareStatement(contains("where CD_SOLICITACAO = ? and CD_DIFICULDADE = ?")))
                .thenReturn(delete);
        when(insert.executeUpdate()).thenReturn(1);
        when(delete.executeUpdate()).thenReturn(1);
        SolicitacaoDificuldadeTO vinculo = new SolicitacaoDificuldadeTO(10L, 20L);

        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conexao);
            SolicitacaoDificuldadeDAO dao = new SolicitacaoDificuldadeDAO();

            assertSame(vinculo, dao.inserir(conexao, vinculo));
            assertTrue(dao.excluir(10L, 20L));

            factory.verify(ConnectionFactory::closeConnection);
        }

        verify(insert).setLong(1, 10L);
        verify(insert).setLong(2, 20L);
        verify(insert, never()).getGeneratedKeys();
        verify(delete).setLong(1, 10L);
        verify(delete).setLong(2, 20L);
    }

    @Test
    void inserirNaoRetornaSucessoSemIdGerado() throws SQLException {
        Connection conexao = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet chaves = mock(ResultSet.class);
        PacienteTO paciente = new PacienteTO(null, "Paciente teste", LocalDate.of(1990, 1, 1), null);
        when(conexao.prepareStatement(anyString(), any(String[].class))).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);
        when(ps.getGeneratedKeys()).thenReturn(chaves);
        when(chaves.next()).thenReturn(false);

        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conexao);
            assertNull(new PacienteDAO().inserir(paciente));
            factory.verify(ConnectionFactory::closeConnection);
        }
        assertNull(paciente.getIdPaciente());
        verify(conexao, never()).commit();
    }

    @Test
    void consultaPreservaDataDeNascimentoETelefoneAusente() throws SQLException {
        Connection conexao = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conexao.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong("CD_PACIENTE")).thenReturn(5_000_000_000L);
        when(rs.getString("NM_PACIENTE")).thenReturn("Paciente teste");
        when(rs.getDate("DT_NASCIMENTO")).thenReturn(Date.valueOf("1990-01-01"));

        PacienteTO paciente;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conexao);
            paciente = new PacienteDAO().buscarPorId(5_000_000_000L);
        }

        assertEquals(5_000_000_000L, paciente.getIdPaciente());
        assertEquals(LocalDate.of(1990, 1, 1), paciente.getDataNascimento());
        assertNull(paciente.getTelefone());
        verify(rs).close();
        verify(ps).close();
        verify(conexao, never()).close();
    }

    @Test
    void solicitacaoSemAnalistaMantemIdNuloEPreservaHoraDaAbertura() throws SQLException {
        Connection conexao = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        LocalDateTime abertura = LocalDateTime.of(2026, 11, 10, 9, 45, 30);
        when(conexao.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong("CD_SOLICITACAO")).thenReturn(1L);
        when(rs.getTimestamp("DT_ABERTURA")).thenReturn(Timestamp.valueOf(abertura));
        when(rs.getString("ST_SOLICITACAO")).thenReturn("ABERTA");
        when(rs.getLong("CD_AGENDAMENTO")).thenReturn(2L);
        when(rs.wasNull()).thenReturn(true);

        SolicitacaoAjudaTO solicitacao;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conexao);
            solicitacao = new SolicitacaoAjudaDAO().buscarPorId(1L);
        }

        assertEquals(abertura, solicitacao.getDataAbertura());
        assertNull(solicitacao.getDataEncerramento());
        assertNull(solicitacao.getIdMembroEquipe());
    }

    @Test
    void inserirSolicitacaoGravaReferenciasEDatasOpcionaisComoNulas() throws SQLException {
        Connection conexao = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        LocalDateTime abertura = LocalDateTime.of(2026, 11, 10, 9, 45, 30);
        SolicitacaoAjudaTO solicitacao = new SolicitacaoAjudaTO(null, abertura, null,
                null, "ABERTA", 1L, null);
        when(conexao.prepareStatement(anyString(), any(String[].class))).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(0);

        assertNull(new SolicitacaoAjudaDAO().inserir(conexao, solicitacao));

        verify(ps).setTimestamp(1, Timestamp.valueOf(abertura));
        verify(ps).setTimestamp(2, null);
        verify(ps).setNull(6, Types.NUMERIC);
        verify(ps, never()).getGeneratedKeys();
    }

    @Test
    void consultaInexistenteRetornaNull() throws SQLException {
        Connection conexao = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conexao.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conexao);
            assertNull(new UnidadeDAO().buscarPorId(1L));
        }
        verify(rs).close();
        verify(ps).close();
    }

    @Test
    void operacoesAvulsasTratamFalhaEFechamConexaoPelaFactory() throws SQLException {
        Connection conexao = mock(Connection.class);
        SQLException falha = new SQLException("Banco indisponível");
        when(conexao.prepareStatement(anyString())).thenThrow(falha);
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conexao);
            PacienteDAO dao = new PacienteDAO();

            assertTrue(dao.listarTodos().isEmpty());
            assertNull(dao.buscarPorId(1L));
            assertFalse(dao.excluir(1L));

            factory.verify(ConnectionFactory::getConnection, times(3));
            factory.verify(ConnectionFactory::closeConnection, times(3));
        }
    }

    @Test
    void falhaDaFactoryDevolveListaVaziaSemTentarFecharConexaoInexistente() {
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(null);

            assertTrue(new PacienteDAO().listarTodos().isEmpty());

            factory.verify(ConnectionFactory::closeConnection, never());
        }
    }

    private static Stream<Arguments> bloqueios() {
        return Stream.of(
                Arguments.of((Bloqueio) conexao -> new HorarioAtendimentoDAO()
                        .buscarPorIdParaAtualizar(conexao, 1L)),
                Arguments.of((Bloqueio) conexao -> new AgendamentoDAO()
                        .buscarPorIdParaAtualizar(conexao, 1L)),
                Arguments.of((Bloqueio) conexao -> new SolicitacaoAjudaDAO()
                        .buscarPorIdParaAtualizar(conexao, 1L)),
                Arguments.of((Bloqueio) conexao -> new PropostaAtendimentoDAO()
                        .buscarPorIdParaAtualizar(conexao, 1L))
        );
    }

    @ParameterizedTest
    @MethodSource("bloqueios")
    void bloqueioExigeTransacaoAberta(Bloqueio bloqueio) throws SQLException {
        Connection conexao = mock(Connection.class);
        when(conexao.getAutoCommit()).thenReturn(true);

        assertNull(bloqueio.buscar(conexao));

        verify(conexao, never()).prepareStatement(anyString());
        verify(conexao, never()).close();
    }

    @Test
    void bloqueioUsaForUpdateEMantemTransacaoDaBo() throws SQLException {
        Connection conexao = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conexao.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        assertNull(new HorarioAtendimentoDAO().buscarPorIdParaAtualizar(conexao, 1L));

        verify(conexao).prepareStatement(endsWith("for update"));
        verify(conexao, never()).commit();
        verify(conexao, never()).rollback();
        verify(conexao, never()).close();
    }

    @Test
    void ocupacaoDesconsideraOProprioAgendamentoAoRevalidarHorario() throws SQLException {
        Connection conexao = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conexao.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.getInt(1)).thenReturn(1);

        assertTrue(new AgendamentoDAO().existeAgendamentoAtivoPorHorario(conexao, 10L, 20L));

        verify(conexao).prepareStatement(contains("ST_AGENDAMENTO = 'AGENDADO'"));
        verify(ps).setLong(1, 10L);
        verify(ps).setLong(2, 20L);
    }

    @Test
    void solicitacoesAtivasIncluemAbertaEEmAnalise() throws SQLException {
        Connection conexao = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conexao.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.getInt(1)).thenReturn(1);

        assertTrue(new SolicitacaoAjudaDAO().existeSolicitacaoAtivaPorAgendamento(conexao, 10L, null));

        verify(conexao).prepareStatement(contains("ST_SOLICITACAO in ('ABERTA', 'EM_ANALISE')"));
        verify(ps).setLong(1, 10L);
        verify(ps, never()).setLong(eq(2), anyLong());
    }

    @Test
    void falhaAoConsultarConflitoImpedeCadastro() throws SQLException {
        Connection conexao = mock(Connection.class);
        when(conexao.prepareStatement(anyString())).thenThrow(new SQLException("Banco indisponível"));

        assertTrue(new AgendamentoDAO().existeAgendamentoAtivoPorHorario(conexao, 10L, null));
        assertTrue(new SolicitacaoAjudaDAO().existeSolicitacaoAtivaPorAgendamento(conexao, 10L, null));
        verify(conexao, never()).close();
    }
}
