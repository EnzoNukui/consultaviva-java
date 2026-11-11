package br.com.fiap.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class PlanoComparecimentoTO {
    private Long idPlano;

    private LocalDateTime dataCalculo;

    @NotBlank
    @Size(max = 250)
    private String origemTrajeto;

    @NotBlank
    @Size(max = 250)
    private String destinoTrajeto;

    @NotBlank
    @Size(max = 60)
    private String meioTransporte;

    private String nomeFonte;

    @NotNull
    @Positive
    private Integer duracaoMinutos;

    @NotNull
    @PositiveOrZero
    private Integer margemMinutos;

    private LocalDateTime dataAtendimentoReferencia;

    private LocalDateTime dataSaidaSugerida;

    private String simulado;

    @NotNull
    @Positive
    private Long idAgendamento;

    @Positive
    private Long idProposta;

    public PlanoComparecimentoTO() {
    }

    public PlanoComparecimentoTO(
            Long idPlano, LocalDateTime dataCalculo, String origemTrajeto, String destinoTrajeto,
            String meioTransporte, String nomeFonte, Integer duracaoMinutos, Integer margemMinutos,
            LocalDateTime dataAtendimentoReferencia, LocalDateTime dataSaidaSugerida, String simulado,
            Long idAgendamento, Long idProposta) {
        this.idPlano = idPlano;
        this.dataCalculo = dataCalculo;
        this.origemTrajeto = origemTrajeto;
        this.destinoTrajeto = destinoTrajeto;
        this.meioTransporte = meioTransporte;
        this.nomeFonte = nomeFonte;
        this.duracaoMinutos = duracaoMinutos;
        this.margemMinutos = margemMinutos;
        this.dataAtendimentoReferencia = dataAtendimentoReferencia;
        this.dataSaidaSugerida = dataSaidaSugerida;
        this.simulado = simulado;
        this.idAgendamento = idAgendamento;
        this.idProposta = idProposta;
    }

    public Long getIdPlano() {
        return idPlano;
    }

    public void setIdPlano(Long idPlano) {
        this.idPlano = idPlano;
    }

    public LocalDateTime getDataCalculo() {
        return dataCalculo;
    }

    public void setDataCalculo(LocalDateTime dataCalculo) {
        this.dataCalculo = dataCalculo;
    }

    public String getOrigemTrajeto() {
        return origemTrajeto;
    }

    public void setOrigemTrajeto(String origemTrajeto) {
        this.origemTrajeto = origemTrajeto;
    }

    public String getDestinoTrajeto() {
        return destinoTrajeto;
    }

    public void setDestinoTrajeto(String destinoTrajeto) {
        this.destinoTrajeto = destinoTrajeto;
    }

    public String getMeioTransporte() {
        return meioTransporte;
    }

    public void setMeioTransporte(String meioTransporte) {
        this.meioTransporte = meioTransporte;
    }

    public String getNomeFonte() {
        return nomeFonte;
    }

    public void setNomeFonte(String nomeFonte) {
        this.nomeFonte = nomeFonte;
    }

    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Integer duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }

    public Integer getMargemMinutos() {
        return margemMinutos;
    }

    public void setMargemMinutos(Integer margemMinutos) {
        this.margemMinutos = margemMinutos;
    }

    public LocalDateTime getDataAtendimentoReferencia() {
        return dataAtendimentoReferencia;
    }

    public void setDataAtendimentoReferencia(LocalDateTime dataAtendimentoReferencia) {
        this.dataAtendimentoReferencia = dataAtendimentoReferencia;
    }

    public LocalDateTime getDataSaidaSugerida() {
        return dataSaidaSugerida;
    }

    public void setDataSaidaSugerida(LocalDateTime dataSaidaSugerida) {
        this.dataSaidaSugerida = dataSaidaSugerida;
    }

    public String getSimulado() {
        return simulado;
    }

    public void setSimulado(String simulado) {
        this.simulado = simulado;
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

