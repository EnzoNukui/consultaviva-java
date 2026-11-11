package br.com.fiap.to;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CadastroSolicitacaoAjudaTO {
    @NotNull
    @Positive
    private Long idAgendamento;

    @Size(max = 500)
    private String relato;

    private List<@NotNull @Positive Long> idsDificuldade;

    @NotEmpty
    private List<@Valid DisponibilidadeTO> disponibilidades;

    public CadastroSolicitacaoAjudaTO() {
    }

    public Long getIdAgendamento() {
        return idAgendamento;
    }

    public void setIdAgendamento(Long idAgendamento) {
        this.idAgendamento = idAgendamento;
    }

    public String getRelato() {
        return relato;
    }

    public void setRelato(String relato) {
        this.relato = relato;
    }

    public List<Long> getIdsDificuldade() {
        return idsDificuldade;
    }

    public void setIdsDificuldade(List<Long> idsDificuldade) {
        this.idsDificuldade = idsDificuldade;
    }

    public List<DisponibilidadeTO> getDisponibilidades() {
        return disponibilidades;
    }

    public void setDisponibilidades(List<DisponibilidadeTO> disponibilidades) {
        this.disponibilidades = disponibilidades;
    }
}
