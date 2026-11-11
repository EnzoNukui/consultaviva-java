package br.com.fiap.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class DisponibilidadeTO {
    private Long idDisponibilidade;

    @NotNull
    private LocalDateTime dataInicio;

    @NotNull
    private LocalDateTime dataFim;

    @NotBlank
    @Size(max = 10)
    @Pattern(regexp = "PRESENCIAL|VIRTUAL|AMBAS")
    private String modalidade;

    private Long idSolicitacao;

    public DisponibilidadeTO() {
    }

    public DisponibilidadeTO(
            Long idDisponibilidade, LocalDateTime dataInicio, LocalDateTime dataFim, String modalidade,
            Long idSolicitacao) {
        this.idDisponibilidade = idDisponibilidade;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.modalidade = modalidade;
        this.idSolicitacao = idSolicitacao;
    }

    public Long getIdDisponibilidade() {
        return idDisponibilidade;
    }

    public void setIdDisponibilidade(Long idDisponibilidade) {
        this.idDisponibilidade = idDisponibilidade;
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

    public String getModalidade() {
        return modalidade;
    }

    public void setModalidade(String modalidade) {
        this.modalidade = modalidade;
    }

    public Long getIdSolicitacao() {
        return idSolicitacao;
    }

    public void setIdSolicitacao(Long idSolicitacao) {
        this.idSolicitacao = idSolicitacao;
    }
}

