package br.com.fiap.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class SincronizacaoAgendaTO {
    private Long idSincronizacao;

    @NotBlank
    @Size(max = 40)
    private String nomeProvedor;

    @Size(max = 255)
    private String idAgendaExterna;

    @Size(max = 1024)
    private String idEventoExterno;

    @NotBlank
    @Size(max = 10)
    private String tipoOperacao;

    @NotBlank
    @Size(max = 20)
    private String statusSincronizacao;

    @NotNull
    @PositiveOrZero
    private Integer numeroTentativas;

    @PastOrPresent
    private LocalDateTime dataUltimaTentativa;

    @PastOrPresent
    private LocalDateTime dataUltimoSucesso;

    @Size(max = 500)
    private String ultimoErro;

    @NotNull
    @Positive
    private Long idAgendamento;

    public SincronizacaoAgendaTO() {
    }

    public SincronizacaoAgendaTO(
            Long idSincronizacao, String nomeProvedor, String idAgendaExterna, String idEventoExterno,
            String tipoOperacao, String statusSincronizacao, Integer numeroTentativas,
            LocalDateTime dataUltimaTentativa, LocalDateTime dataUltimoSucesso, String ultimoErro,
            Long idAgendamento) {
        this.idSincronizacao = idSincronizacao;
        this.nomeProvedor = nomeProvedor;
        this.idAgendaExterna = idAgendaExterna;
        this.idEventoExterno = idEventoExterno;
        this.tipoOperacao = tipoOperacao;
        this.statusSincronizacao = statusSincronizacao;
        this.numeroTentativas = numeroTentativas;
        this.dataUltimaTentativa = dataUltimaTentativa;
        this.dataUltimoSucesso = dataUltimoSucesso;
        this.ultimoErro = ultimoErro;
        this.idAgendamento = idAgendamento;
    }

    public Long getIdSincronizacao() {
        return idSincronizacao;
    }

    public void setIdSincronizacao(Long idSincronizacao) {
        this.idSincronizacao = idSincronizacao;
    }

    public String getNomeProvedor() {
        return nomeProvedor;
    }

    public void setNomeProvedor(String nomeProvedor) {
        this.nomeProvedor = nomeProvedor;
    }

    public String getIdAgendaExterna() {
        return idAgendaExterna;
    }

    public void setIdAgendaExterna(String idAgendaExterna) {
        this.idAgendaExterna = idAgendaExterna;
    }

    public String getIdEventoExterno() {
        return idEventoExterno;
    }

    public void setIdEventoExterno(String idEventoExterno) {
        this.idEventoExterno = idEventoExterno;
    }

    public String getTipoOperacao() {
        return tipoOperacao;
    }

    public void setTipoOperacao(String tipoOperacao) {
        this.tipoOperacao = tipoOperacao;
    }

    public String getStatusSincronizacao() {
        return statusSincronizacao;
    }

    public void setStatusSincronizacao(String statusSincronizacao) {
        this.statusSincronizacao = statusSincronizacao;
    }

    public Integer getNumeroTentativas() {
        return numeroTentativas;
    }

    public void setNumeroTentativas(Integer numeroTentativas) {
        this.numeroTentativas = numeroTentativas;
    }

    public LocalDateTime getDataUltimaTentativa() {
        return dataUltimaTentativa;
    }

    public void setDataUltimaTentativa(LocalDateTime dataUltimaTentativa) {
        this.dataUltimaTentativa = dataUltimaTentativa;
    }

    public LocalDateTime getDataUltimoSucesso() {
        return dataUltimoSucesso;
    }

    public void setDataUltimoSucesso(LocalDateTime dataUltimoSucesso) {
        this.dataUltimoSucesso = dataUltimoSucesso;
    }

    public String getUltimoErro() {
        return ultimoErro;
    }

    public void setUltimoErro(String ultimoErro) {
        this.ultimoErro = ultimoErro;
    }

    public Long getIdAgendamento() {
        return idAgendamento;
    }

    public void setIdAgendamento(Long idAgendamento) {
        this.idAgendamento = idAgendamento;
    }
}

