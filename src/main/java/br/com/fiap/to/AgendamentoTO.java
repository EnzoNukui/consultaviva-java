package br.com.fiap.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class AgendamentoTO {
    private Long idAgendamento;

    @NotBlank
    @Size(max = 10)
    @Pattern(regexp = "PRESENCIAL|VIRTUAL")
    private String modalidade;

    private String statusAgendamento;

    private String statusRespostaPaciente;

    @NotNull
    @Positive
    private Long idPaciente;

    @NotNull
    @Positive
    private Long idHorario;

    public AgendamentoTO() {
    }

    public AgendamentoTO(
            Long idAgendamento, String modalidade, String statusAgendamento, String statusRespostaPaciente,
            Long idPaciente, Long idHorario) {
        this.idAgendamento = idAgendamento;
        this.modalidade = modalidade;
        this.statusAgendamento = statusAgendamento;
        this.statusRespostaPaciente = statusRespostaPaciente;
        this.idPaciente = idPaciente;
        this.idHorario = idHorario;
    }

    public Long getIdAgendamento() {
        return idAgendamento;
    }

    public void setIdAgendamento(Long idAgendamento) {
        this.idAgendamento = idAgendamento;
    }

    public String getModalidade() {
        return modalidade;
    }

    public void setModalidade(String modalidade) {
        this.modalidade = modalidade;
    }

    public String getStatusAgendamento() {
        return statusAgendamento;
    }

    public void setStatusAgendamento(String statusAgendamento) {
        this.statusAgendamento = statusAgendamento;
    }

    public String getStatusRespostaPaciente() {
        return statusRespostaPaciente;
    }

    public void setStatusRespostaPaciente(String statusRespostaPaciente) {
        this.statusRespostaPaciente = statusRespostaPaciente;
    }

    public Long getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(Long idPaciente) {
        this.idPaciente = idPaciente;
    }

    public Long getIdHorario() {
        return idHorario;
    }

    public void setIdHorario(Long idHorario) {
        this.idHorario = idHorario;
    }
}

