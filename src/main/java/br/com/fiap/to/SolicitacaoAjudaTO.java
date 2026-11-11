package br.com.fiap.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class SolicitacaoAjudaTO {
    private Long idSolicitacao;

    @NotNull
    @PastOrPresent
    private LocalDateTime dataAbertura;

    @PastOrPresent
    private LocalDateTime dataEncerramento;

    @Size(max = 500)
    private String relato;

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "ABERTA|EM_ANALISE|RESOLVIDA|SEM_SOLUCAO|CANCELADA")
    private String statusSolicitacao;

    @NotNull
    @Positive
    private Long idAgendamento;

    @Positive
    private Long idMembroEquipe;

    public SolicitacaoAjudaTO() {
    }

    public SolicitacaoAjudaTO(
            Long idSolicitacao, LocalDateTime dataAbertura, LocalDateTime dataEncerramento, String relato,
            String statusSolicitacao, Long idAgendamento, Long idMembroEquipe) {
        this.idSolicitacao = idSolicitacao;
        this.dataAbertura = dataAbertura;
        this.dataEncerramento = dataEncerramento;
        this.relato = relato;
        this.statusSolicitacao = statusSolicitacao;
        this.idAgendamento = idAgendamento;
        this.idMembroEquipe = idMembroEquipe;
    }

    public Long getIdSolicitacao() {
        return idSolicitacao;
    }

    public void setIdSolicitacao(Long idSolicitacao) {
        this.idSolicitacao = idSolicitacao;
    }

    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(LocalDateTime dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public LocalDateTime getDataEncerramento() {
        return dataEncerramento;
    }

    public void setDataEncerramento(LocalDateTime dataEncerramento) {
        this.dataEncerramento = dataEncerramento;
    }

    public String getRelato() {
        return relato;
    }

    public void setRelato(String relato) {
        this.relato = relato;
    }

    public String getStatusSolicitacao() {
        return statusSolicitacao;
    }

    public void setStatusSolicitacao(String statusSolicitacao) {
        this.statusSolicitacao = statusSolicitacao;
    }

    public Long getIdAgendamento() {
        return idAgendamento;
    }

    public void setIdAgendamento(Long idAgendamento) {
        this.idAgendamento = idAgendamento;
    }

    public Long getIdMembroEquipe() {
        return idMembroEquipe;
    }

    public void setIdMembroEquipe(Long idMembroEquipe) {
        this.idMembroEquipe = idMembroEquipe;
    }
}

