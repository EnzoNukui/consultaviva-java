package br.com.fiap.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UnidadeTO {
    private Long idUnidade;

    @NotBlank
    @Size(max = 100)
    private String nomeUnidade;

    @NotBlank
    @Size(max = 250)
    private String endereco;

    public UnidadeTO() {
    }

    public UnidadeTO(Long idUnidade, String nomeUnidade, String endereco) {
        this.idUnidade = idUnidade;
        this.nomeUnidade = nomeUnidade;
        this.endereco = endereco;
    }

    public Long getIdUnidade() {
        return idUnidade;
    }

    public void setIdUnidade(Long idUnidade) {
        this.idUnidade = idUnidade;
    }

    public String getNomeUnidade() {
        return nomeUnidade;
    }

    public void setNomeUnidade(String nomeUnidade) {
        this.nomeUnidade = nomeUnidade;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }
}

