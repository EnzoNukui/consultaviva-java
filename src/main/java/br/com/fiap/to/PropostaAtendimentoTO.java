package br.com.fiap.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class PropostaAtendimentoTO {
    private Long idProposta;

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "MANUTENCAO|REMARCACAO|CONVERSAO|REMARCACAO_CONVERSAO")
    private String tipoAlternativa;

    @NotBlank
    @Size(max = 10)
    @Pattern(regexp = "PRESENCIAL|VIRTUAL")
    private String modalidade;

    @NotBlank
    @Size(max = 500)
    private String justificativa;

    @NotNull
    @PastOrPresent
    private LocalDateTime dataCriacao;

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "EM_ANALISE|OFERTADA|CONFIRMADA|EXPIRADA|CANCELADA")
    private String statusProposta;

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "PENDENTE|AUTORIZADA|NEGADA|NAO_APLICAVEL")
    private String statusAutorizacao;

    @PastOrPresent
    private LocalDateTime dataAutorizacao;

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "PENDENTE|ACEITA|RECUSADA")
    private String statusAceitePaciente;

    @PastOrPresent
    private LocalDateTime dataResposta;

    // A validade da reserva e do prazo de aceite depende do estado da proposta, validado na BO.
    private LocalDateTime dataInicioReserva;

    private LocalDateTime dataFimReserva;

    private LocalDateTime dataLimiteAceite;

    @PastOrPresent
    private LocalDateTime dataConfirmacao;

    @NotNull
    @Positive
    private Long idSolicitacao;

    @NotNull
    @Positive
    private Long idHorario;

    @Positive
    private Long idAnalista;

    @Positive
    private Long idAutorizador;

    public PropostaAtendimentoTO() {
    }

    public PropostaAtendimentoTO(
            Long idProposta, String tipoAlternativa, String modalidade, String justificativa,
            LocalDateTime dataCriacao, String statusProposta, String statusAutorizacao,
            LocalDateTime dataAutorizacao, String statusAceitePaciente, LocalDateTime dataResposta,
            LocalDateTime dataInicioReserva, LocalDateTime dataFimReserva, LocalDateTime dataLimiteAceite,
            LocalDateTime dataConfirmacao, Long idSolicitacao, Long idHorario, Long idAnalista,
            Long idAutorizador) {
        this.idProposta = idProposta;
        this.tipoAlternativa = tipoAlternativa;
        this.modalidade = modalidade;
        this.justificativa = justificativa;
        this.dataCriacao = dataCriacao;
        this.statusProposta = statusProposta;
        this.statusAutorizacao = statusAutorizacao;
        this.dataAutorizacao = dataAutorizacao;
        this.statusAceitePaciente = statusAceitePaciente;
        this.dataResposta = dataResposta;
        this.dataInicioReserva = dataInicioReserva;
        this.dataFimReserva = dataFimReserva;
        this.dataLimiteAceite = dataLimiteAceite;
        this.dataConfirmacao = dataConfirmacao;
        this.idSolicitacao = idSolicitacao;
        this.idHorario = idHorario;
        this.idAnalista = idAnalista;
        this.idAutorizador = idAutorizador;
    }

    public Long getIdProposta() {
        return idProposta;
    }

    public void setIdProposta(Long idProposta) {
        this.idProposta = idProposta;
    }

    public String getTipoAlternativa() {
        return tipoAlternativa;
    }

    public void setTipoAlternativa(String tipoAlternativa) {
        this.tipoAlternativa = tipoAlternativa;
    }

    public String getModalidade() {
        return modalidade;
    }

    public void setModalidade(String modalidade) {
        this.modalidade = modalidade;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getStatusProposta() {
        return statusProposta;
    }

    public void setStatusProposta(String statusProposta) {
        this.statusProposta = statusProposta;
    }

    public String getStatusAutorizacao() {
        return statusAutorizacao;
    }

    public void setStatusAutorizacao(String statusAutorizacao) {
        this.statusAutorizacao = statusAutorizacao;
    }

    public LocalDateTime getDataAutorizacao() {
        return dataAutorizacao;
    }

    public void setDataAutorizacao(LocalDateTime dataAutorizacao) {
        this.dataAutorizacao = dataAutorizacao;
    }

    public String getStatusAceitePaciente() {
        return statusAceitePaciente;
    }

    public void setStatusAceitePaciente(String statusAceitePaciente) {
        this.statusAceitePaciente = statusAceitePaciente;
    }

    public LocalDateTime getDataResposta() {
        return dataResposta;
    }

    public void setDataResposta(LocalDateTime dataResposta) {
        this.dataResposta = dataResposta;
    }

    public LocalDateTime getDataInicioReserva() {
        return dataInicioReserva;
    }

    public void setDataInicioReserva(LocalDateTime dataInicioReserva) {
        this.dataInicioReserva = dataInicioReserva;
    }

    public LocalDateTime getDataFimReserva() {
        return dataFimReserva;
    }

    public void setDataFimReserva(LocalDateTime dataFimReserva) {
        this.dataFimReserva = dataFimReserva;
    }

    public LocalDateTime getDataLimiteAceite() {
        return dataLimiteAceite;
    }

    public void setDataLimiteAceite(LocalDateTime dataLimiteAceite) {
        this.dataLimiteAceite = dataLimiteAceite;
    }

    public LocalDateTime getDataConfirmacao() {
        return dataConfirmacao;
    }

    public void setDataConfirmacao(LocalDateTime dataConfirmacao) {
        this.dataConfirmacao = dataConfirmacao;
    }

    public Long getIdSolicitacao() {
        return idSolicitacao;
    }

    public void setIdSolicitacao(Long idSolicitacao) {
        this.idSolicitacao = idSolicitacao;
    }

    public Long getIdHorario() {
        return idHorario;
    }

    public void setIdHorario(Long idHorario) {
        this.idHorario = idHorario;
    }

    public Long getIdAnalista() {
        return idAnalista;
    }

    public void setIdAnalista(Long idAnalista) {
        this.idAnalista = idAnalista;
    }

    public Long getIdAutorizador() {
        return idAutorizador;
    }

    public void setIdAutorizador(Long idAutorizador) {
        this.idAutorizador = idAutorizador;
    }
}

