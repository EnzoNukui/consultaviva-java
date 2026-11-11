package br.com.fiap.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class HistoricoEventoTO {
    private Long idEvento;

    @NotNull
    @PastOrPresent
    private LocalDateTime dataEvento;

    @NotBlank
    @Size(max = 40)
    private String tipoEvento;

    @NotBlank
    @Size(max = 1000)
    private String descricaoEvento;

    @NotBlank
    @Size(max = 10)
    private String tipoOrigem;

    @NotNull
    @Positive
    private Long idAgendamento;

    @Positive
    private Long idSolicitacao;

    @Positive
    private Long idProposta;

    @Positive
    private Long idMembroEquipe;

    public HistoricoEventoTO() {
    }

    public HistoricoEventoTO(
            Long idEvento, LocalDateTime dataEvento, String tipoEvento, String descricaoEvento,
            String tipoOrigem, Long idAgendamento, Long idSolicitacao, Long idProposta, Long idMembroEquipe) {
        this.idEvento = idEvento;
        this.dataEvento = dataEvento;
        this.tipoEvento = tipoEvento;
        this.descricaoEvento = descricaoEvento;
        this.tipoOrigem = tipoOrigem;
        this.idAgendamento = idAgendamento;
        this.idSolicitacao = idSolicitacao;
        this.idProposta = idProposta;
        this.idMembroEquipe = idMembroEquipe;
    }

    public Long getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(Long idEvento) {
        this.idEvento = idEvento;
    }

    public LocalDateTime getDataEvento() {
        return dataEvento;
    }

    public void setDataEvento(LocalDateTime dataEvento) {
        this.dataEvento = dataEvento;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public String getDescricaoEvento() {
        return descricaoEvento;
    }

    public void setDescricaoEvento(String descricaoEvento) {
        this.descricaoEvento = descricaoEvento;
    }

    public String getTipoOrigem() {
        return tipoOrigem;
    }

    public void setTipoOrigem(String tipoOrigem) {
        this.tipoOrigem = tipoOrigem;
    }

    public Long getIdAgendamento() {
        return idAgendamento;
    }

    public void setIdAgendamento(Long idAgendamento) {
        this.idAgendamento = idAgendamento;
    }

    public Long getIdSolicitacao() {
        return idSolicitacao;
    }

    public void setIdSolicitacao(Long idSolicitacao) {
        this.idSolicitacao = idSolicitacao;
    }

    public Long getIdProposta() {
        return idProposta;
    }

    public void setIdProposta(Long idProposta) {
        this.idProposta = idProposta;
    }

    public Long getIdMembroEquipe() {
        return idMembroEquipe;
    }

    public void setIdMembroEquipe(Long idMembroEquipe) {
        this.idMembroEquipe = idMembroEquipe;
    }
}

