package br.com.fiap.bo;

import br.com.fiap.dao.AgendamentoDAO;
import br.com.fiap.dao.ConnectionFactory;
import br.com.fiap.dao.DisponibilidadeDAO;
import br.com.fiap.dao.HistoricoEventoDAO;
import br.com.fiap.dao.HorarioAtendimentoDAO;
import br.com.fiap.dao.MembroEquipeDAO;
import br.com.fiap.dao.PropostaAtendimentoDAO;
import br.com.fiap.dao.SolicitacaoAjudaDAO;
import br.com.fiap.exception.AcessoNegadoException;
import br.com.fiap.exception.RegraNegocioException;
import br.com.fiap.to.AgendamentoTO;
import br.com.fiap.to.DisponibilidadeTO;
import br.com.fiap.to.HistoricoEventoTO;
import br.com.fiap.to.HorarioAtendimentoTO;
import br.com.fiap.to.MembroEquipeTO;
import br.com.fiap.to.PropostaAtendimentoTO;
import br.com.fiap.to.SolicitacaoAjudaTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FluxoResgateBOTests {
    @Mock private Connection conexao;
    @Mock private AgendamentoDAO agendamentoDAO;
    @Mock private SolicitacaoAjudaDAO solicitacaoDAO;
    @Mock private PropostaAtendimentoDAO propostaDAO;
    @Mock private HorarioAtendimentoDAO horarioDAO;
    @Mock private DisponibilidadeDAO disponibilidadeDAO;
    @Mock private MembroEquipeDAO membroEquipeDAO;
    @Mock private HistoricoEventoDAO historicoDAO;
    @Mock private MembroEquipeBO membroEquipeBO;

    @InjectMocks private PropostaAtendimentoBO propostaBO;
    @InjectMocks private AgendamentoBO agendamentoBO;
    @InjectMocks private SolicitacaoAjudaBO solicitacaoBO;

    private AutoCloseable mocks;
    private MockedStatic<ConnectionFactory> fabrica;
    private LocalDateTime instante;
    private AgendamentoTO agendamento;
    private SolicitacaoAjudaTO solicitacao;
    private PropostaAtendimentoTO proposta;
    private HorarioAtendimentoTO original;
    private HorarioAtendimentoTO candidato;

    @BeforeEach
    void prepararFluxo() {
        mocks = MockitoAnnotations.openMocks(this);
        fabrica = mockStatic(ConnectionFactory.class);
        fabrica.when(ConnectionFactory::getNewConnection).thenReturn(conexao);
        instante = LocalDateTime.now(ZoneId.of("America/Sao_Paulo")).withNano(0);
        agendamento = new AgendamentoTO(10L, "PRESENCIAL", "AGENDADO", "PRECISO_DE_AJUDA", 20L, 30L);
        original = new HorarioAtendimentoTO(30L, instante.plusHours(2), instante.plusHours(3), 40L, 50L);
        candidato = new HorarioAtendimentoTO(31L, instante.plusHours(4), instante.plusHours(5), 40L, 50L);
        solicitacao = new SolicitacaoAjudaTO(60L, instante.minusHours(2), null, null, "EM_ANALISE", 10L, 70L);
        proposta = new PropostaAtendimentoTO(80L, "REMARCACAO", "PRESENCIAL", "Alternativa compatível",
                instante.minusHours(1), "OFERTADA", "NAO_APLICAVEL", null, "ACEITA", instante.minusMinutes(1),
                instante.minusMinutes(10), instante.plusHours(1), instante.plusHours(1), null, 60L, 31L, 70L, null);

        when(membroEquipeBO.podeApoiarAdministrativamente(70L)).thenReturn(true);
        when(membroEquipeDAO.buscarPorIdParaAtualizar(conexao, 50L)).thenReturn(new MembroEquipeTO(50L,
                "Profissional teste", "Profissional", "S", "N", "S", null));
        when(agendamentoDAO.buscarPorIdParaAtualizar(conexao, 10L)).thenReturn(agendamento);
        when(solicitacaoDAO.buscarPorId(60L)).thenReturn(solicitacao);
        when(solicitacaoDAO.buscarPorIdParaAtualizar(conexao, 60L)).thenReturn(solicitacao);
        when(propostaDAO.buscarPorId(80L)).thenReturn(proposta);
        when(propostaDAO.buscarPorIdParaAtualizar(conexao, 80L)).thenReturn(proposta);
        when(horarioDAO.buscarPorId(30L)).thenReturn(original);
        when(horarioDAO.buscarPorIdParaAtualizar(conexao, 31L)).thenReturn(candidato);
        when(disponibilidadeDAO.listarPorSolicitacao(conexao, 60L)).thenReturn(new ArrayList<>(List.of(
                new DisponibilidadeTO(1L, original.getDataInicio(), candidato.getDataFim(), "AMBAS", 60L))));
        when(propostaDAO.existeReservaAtivaPorHorario(eq(conexao), anyLong(), any(), nullable(Long.class)))
                .thenReturn(false);
        when(propostaDAO.listarPorSolicitacao(conexao, 60L)).thenReturn(new ArrayList<>(List.of(proposta)));
        when(agendamentoDAO.atualizar(eq(conexao), any(AgendamentoTO.class))).thenAnswer(chamada -> chamada.getArgument(1));
        when(propostaDAO.atualizar(eq(conexao), any(PropostaAtendimentoTO.class))).thenAnswer(chamada -> chamada.getArgument(1));
        when(solicitacaoDAO.atualizar(eq(conexao), any(SolicitacaoAjudaTO.class))).thenAnswer(chamada -> chamada.getArgument(1));
        when(historicoDAO.inserir(eq(conexao), any(HistoricoEventoTO.class))).thenAnswer(chamada -> chamada.getArgument(1));
    }

    @AfterEach
    void fecharMocks() throws Exception {
        fabrica.close();
        mocks.close();
    }

    @Test
    void confirmaRemarcacaoNaMesmaTransacaoEGravaHistoricoAntesDoCommit() throws SQLException {
        assertSame(proposta, propostaBO.confirmar(80L, 70L));

        assertEquals(10L, agendamento.getIdAgendamento());
        assertEquals(31L, agendamento.getIdHorario());
        assertEquals("CONFIRMADA", proposta.getStatusProposta());
        assertEquals("RESOLVIDA", solicitacao.getStatusSolicitacao());
        assertEquals(proposta.getDataConfirmacao(), solicitacao.getDataEncerramento());
        InOrder ordem = inOrder(conexao, agendamentoDAO, solicitacaoDAO, propostaDAO, horarioDAO, historicoDAO);
        ordem.verify(conexao).setAutoCommit(false);
        ordem.verify(agendamentoDAO).buscarPorIdParaAtualizar(conexao, 10L);
        ordem.verify(solicitacaoDAO).buscarPorIdParaAtualizar(conexao, 60L);
        ordem.verify(propostaDAO).buscarPorIdParaAtualizar(conexao, 80L);
        ordem.verify(horarioDAO).buscarPorIdParaAtualizar(conexao, 31L);
        ordem.verify(agendamentoDAO).atualizar(conexao, agendamento);
        ordem.verify(propostaDAO).atualizar(conexao, proposta);
        ordem.verify(solicitacaoDAO).atualizar(conexao, solicitacao);
        ordem.verify(historicoDAO).inserir(eq(conexao), argThat(evento ->
                "RESGATE_CONFIRMADO".equals(evento.getTipoEvento()) && evento.getIdAgendamento().equals(10L)));
        ordem.verify(conexao).commit();
        verify(conexao, never()).rollback();
    }

    @Test
    void falhaDeHistoricoDesfazTodasAsGravacoesDaConfirmacao() throws SQLException {
        when(historicoDAO.inserir(eq(conexao), any(HistoricoEventoTO.class))).thenReturn(null);

        assertNull(propostaBO.confirmar(80L, 70L));

        verify(agendamentoDAO).atualizar(conexao, agendamento);
        verify(propostaDAO).atualizar(conexao, proposta);
        verify(solicitacaoDAO).atualizar(conexao, solicitacao);
        verify(conexao).rollback();
        verify(conexao, never()).commit();
        verify(conexao).close();
    }

    @Test
    void vagaOcupadaImpedeConfirmacaoMesmoComAceite() throws SQLException {
        when(agendamentoDAO.existeAgendamentoAtivoPorHorario(conexao, 31L, 10L)).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> propostaBO.confirmar(80L, 70L));

        assertEquals(30L, agendamento.getIdHorario());
        verificarConfirmacaoRecusada();
    }

    @Test
    void confirmarExigeAceiteDoPaciente() throws SQLException {
        proposta.setStatusAceitePaciente("PENDENTE");
        proposta.setDataResposta(null);

        assertThrows(RegraNegocioException.class, () -> propostaBO.confirmar(80L, 70L));

        verificarConfirmacaoRecusada();
    }

    @Test
    void confirmarRecusaOfertaComPrazoVencido() throws SQLException {
        proposta.setDataLimiteAceite(instante.minusSeconds(1));

        assertThrows(RegraNegocioException.class, () -> propostaBO.confirmar(80L, 70L));

        verificarConfirmacaoRecusada();
    }

    @Test
    void ofertarConversaoExigeAutorizacaoProfissional() throws SQLException {
        proposta.setStatusProposta("EM_ANALISE");
        proposta.setTipoAlternativa("REMARCACAO_CONVERSAO");
        proposta.setModalidade("VIRTUAL");
        proposta.setStatusAutorizacao("PENDENTE");

        assertThrows(RegraNegocioException.class, () -> propostaBO.ofertar(80L, 70L, instante.plusHours(1)));

        verify(propostaDAO, never()).atualizar(eq(conexao), any());
        verify(conexao).rollback();
    }

    @Test
    void pacienteDeOutroAgendamentoNaoPodeAceitar() throws SQLException {
        proposta.setStatusAceitePaciente("PENDENTE");

        assertThrows(AcessoNegadoException.class, () -> propostaBO.registrarResposta(80L, 999L, true));

        verify(propostaDAO, never()).atualizar(eq(conexao), any());
        verify(conexao).rollback();
    }

    @Test
    void recusaCancelaPropostaEPreservaAgendamento() throws SQLException {
        proposta.setStatusAceitePaciente("PENDENTE");
        proposta.setDataResposta(null);

        assertSame(proposta, propostaBO.registrarResposta(80L, 20L, false));

        assertEquals("RECUSADA", proposta.getStatusAceitePaciente());
        assertEquals("CANCELADA", proposta.getStatusProposta());
        assertEquals("AGENDADO", agendamento.getStatusAgendamento());
        assertEquals(30L, agendamento.getIdHorario());
        verify(agendamentoDAO, never()).atualizar(eq(conexao), any());
        verify(conexao).commit();
    }

    @Test
    void geraAlternativasOrdenadasESeparaAsQueExigemAutorizacao() throws SQLException {
        when(horarioDAO.listarCandidatos(eq(conexao), eq(40L), any(), eq(10L)))
                .thenReturn(new ArrayList<>(List.of(original, candidato)));
        when(propostaDAO.listarPorSolicitacao(conexao, 60L)).thenReturn(new ArrayList<>());
        when(propostaDAO.inserir(eq(conexao), any(PropostaAtendimentoTO.class))).thenAnswer(chamada -> {
            PropostaAtendimentoTO criada = chamada.getArgument(1);
            criada.setIdProposta(90L);
            return criada;
        });

        List<PropostaAtendimentoTO> criadas = propostaBO.gerarPropostas(60L, 70L);

        assertEquals(List.of("MANUTENCAO", "REMARCACAO", "CONVERSAO", "REMARCACAO_CONVERSAO"),
                criadas.stream().map(PropostaAtendimentoTO::getTipoAlternativa).toList());
        assertEquals(List.of("NAO_APLICAVEL", "NAO_APLICAVEL", "PENDENTE", "PENDENTE"),
                criadas.stream().map(PropostaAtendimentoTO::getStatusAutorizacao).toList());
        verify(conexao).commit();
    }

    @Test
    void solicitacaoSemDisponibilidadeNaoGeraAlternativa() {
        when(disponibilidadeDAO.listarPorSolicitacao(conexao, 60L)).thenReturn(new ArrayList<>());

        assertTrue(propostaBO.gerarPropostas(60L, 70L).isEmpty());

        verify(propostaDAO, never()).inserir(eq(conexao), any());
    }

    @Test
    void bloqueiaSegundaSolicitacaoAtivaParaMesmoAgendamento() throws SQLException {
        when(solicitacaoDAO.existeSolicitacaoAtivaPorAgendamento(conexao, 10L, null)).thenReturn(true);
        SolicitacaoAjudaTO dados = new SolicitacaoAjudaTO(null, null, null, "Transporte", null, 10L, null);

        assertThrows(RegraNegocioException.class, () -> solicitacaoBO.inserir(dados, 20L, List.of(), List.of()));

        verify(solicitacaoDAO, never()).inserir(eq(conexao), any());
        verify(conexao).rollback();
    }

    @Test
    void respostaDeAjudaNaoCancelaAgendamento() throws SQLException {
        agendamento.setStatusRespostaPaciente("SEM_RESPOSTA");

        assertSame(agendamento, agendamentoBO.registrarResposta(10L, 20L, "PRECISO_DE_AJUDA"));

        assertEquals("PRECISO_DE_AJUDA", agendamento.getStatusRespostaPaciente());
        assertEquals("AGENDADO", agendamento.getStatusAgendamento());
        verify(conexao).commit();
    }

    private void verificarConfirmacaoRecusada() throws SQLException {
        verify(agendamentoDAO, never()).atualizar(eq(conexao), any());
        verify(propostaDAO, never()).atualizar(eq(conexao), any());
        verify(solicitacaoDAO, never()).atualizar(eq(conexao), any());
        verify(conexao).rollback();
        verify(conexao, never()).commit();
    }
}
