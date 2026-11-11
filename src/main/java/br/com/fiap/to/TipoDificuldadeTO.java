package br.com.fiap.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TipoDificuldadeTO {
    private Long idDificuldade;

    @NotBlank
    @Size(max = 60)
    private String nomeDificuldade;

    public TipoDificuldadeTO() {
    }

    public TipoDificuldadeTO(Long idDificuldade, String nomeDificuldade) {
        this.idDificuldade = idDificuldade;
        this.nomeDificuldade = nomeDificuldade;
    }

    public Long getIdDificuldade() {
        return idDificuldade;
    }

    public void setIdDificuldade(Long idDificuldade) {
        this.idDificuldade = idDificuldade;
    }

    public String getNomeDificuldade() {
        return nomeDificuldade;
    }

    public void setNomeDificuldade(String nomeDificuldade) {
        this.nomeDificuldade = nomeDificuldade;
    }
}

