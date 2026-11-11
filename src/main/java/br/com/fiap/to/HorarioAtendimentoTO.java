package br.com.fiap.to;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public class HorarioAtendimentoTO {
    private Long idHorario;

    @NotNull
    private LocalDateTime dataInicio;

    @NotNull
    private LocalDateTime dataFim;

    @NotNull
    @Positive
    private Long idUnidade;

    @NotNull
    @Positive
    private Long idMembroEquipe;

    public HorarioAtendimentoTO() {
    }

    public HorarioAtendimentoTO(
            Long idHorario, LocalDateTime dataInicio, LocalDateTime dataFim, Long idUnidade,
            Long idMembroEquipe) {
        this.idHorario = idHorario;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.idUnidade = idUnidade;
        this.idMembroEquipe = idMembroEquipe;
    }

    public Long getIdHorario() {
        return idHorario;
    }

    public void setIdHorario(Long idHorario) {
        this.idHorario = idHorario;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public Long getIdUnidade() {
        return idUnidade;
    }

    public void setIdUnidade(Long idUnidade) {
        this.idUnidade = idUnidade;
    }

    public Long getIdMembroEquipe() {
        return idMembroEquipe;
    }

    public void setIdMembroEquipe(Long idMembroEquipe) {
        this.idMembroEquipe = idMembroEquipe;
    }
}

