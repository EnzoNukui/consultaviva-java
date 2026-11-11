package br.com.fiap.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class PreparoVirtualTO {
    private Long idPreparo;

    private LocalDateTime dataVerificacao;

    @NotBlank
    @Size(max = 15)
    private String statusPermissaoMicrofone;

    @NotBlank
    @Size(max = 15)
    private String statusPermissaoCamera;

    @NotBlank
    @Size(max = 15)
    private String statusCamera;

    @NotBlank
    @Size(max = 15)
    private String statusMicrofone;

    @NotBlank
    @Size(max = 15)
    private String statusConexao;

    @Size(max = 500)
    private String observacao;

    @NotNull
    @Positive
    private Long idAgendamento;

    @Positive
    private Long idProposta;

    public PreparoVirtualTO() {
    }

    public PreparoVirtualTO(
            Long idPreparo, LocalDateTime dataVerificacao, String statusPermissaoMicrofone,
            String statusPermissaoCamera, String statusCamera, String statusMicrofone, String statusConexao,
            String observacao, Long idAgendamento, Long idProposta) {
        this.idPreparo = idPreparo;
        this.dataVerificacao = dataVerificacao;
        this.statusPermissaoMicrofone = statusPermissaoMicrofone;
        this.statusPermissaoCamera = statusPermissaoCamera;
        this.statusCamera = statusCamera;
        this.statusMicrofone = statusMicrofone;
        this.statusConexao = statusConexao;
        this.observacao = observacao;
        this.idAgendamento = idAgendamento;
        this.idProposta = idProposta;
    }

    public Long getIdPreparo() {
        return idPreparo;
    }

    public void setIdPreparo(Long idPreparo) {
        this.idPreparo = idPreparo;
    }

    public LocalDateTime getDataVerificacao() {
        return dataVerificacao;
    }

    public void setDataVerificacao(LocalDateTime dataVerificacao) {
        this.dataVerificacao = dataVerificacao;
    }

    public String getStatusPermissaoMicrofone() {
        return statusPermissaoMicrofone;
    }

    public void setStatusPermissaoMicrofone(String statusPermissaoMicrofone) {
        this.statusPermissaoMicrofone = statusPermissaoMicrofone;
    }

    public String getStatusPermissaoCamera() {
        return statusPermissaoCamera;
    }

    public void setStatusPermissaoCamera(String statusPermissaoCamera) {
        this.statusPermissaoCamera = statusPermissaoCamera;
    }

    public String getStatusCamera() {
        return statusCamera;
    }

    public void setStatusCamera(String statusCamera) {
        this.statusCamera = statusCamera;
    }

    public String getStatusMicrofone() {
        return statusMicrofone;
    }

    public void setStatusMicrofone(String statusMicrofone) {
        this.statusMicrofone = statusMicrofone;
    }

    public String getStatusConexao() {
        return statusConexao;
    }

    public void setStatusConexao(String statusConexao) {
        this.statusConexao = statusConexao;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public Long getIdAgendamento() {
        return idAgendamento;
    }

    public void setIdAgendamento(Long idAgendamento) {
        this.idAgendamento = idAgendamento;
    }

    public Long getIdProposta() {
        return idProposta;
    }

    public void setIdProposta(Long idProposta) {
        this.idProposta = idProposta;
    }
}

