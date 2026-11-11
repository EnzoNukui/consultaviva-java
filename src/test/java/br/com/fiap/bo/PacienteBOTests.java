package br.com.fiap.bo;

import br.com.fiap.dao.AgendamentoDAO;
import br.com.fiap.to.AgendamentoTO;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class PacienteBOTests {
    @Test
    void consultaSomenteAgendamentosDoPacienteAutenticado() {
        AgendamentoTO agendamento = new AgendamentoTO(1L, "PRESENCIAL", "AGENDADO", "SEM_RESPOSTA", 10L, 2L);
        try (MockedConstruction<AgendamentoDAO> daos = mockConstruction(AgendamentoDAO.class, (dao, contexto) ->
                when(dao.listarPorPaciente(10L)).thenReturn(new ArrayList<>(List.of(agendamento))))) {
            PacienteBO pacienteBO = new PacienteBO();
            AgendamentoDAO dao = daos.constructed().getFirst();

            assertTrue(pacienteBO.listarAgendamentos(20L, 10L).isEmpty());
            assertTrue(pacienteBO.listarAgendamentos(10L, null).isEmpty());
            verifyNoInteractions(dao);

            assertEquals(List.of(agendamento), pacienteBO.listarAgendamentos(10L, 10L));
            verify(dao).listarPorPaciente(10L);
        }
    }
}
