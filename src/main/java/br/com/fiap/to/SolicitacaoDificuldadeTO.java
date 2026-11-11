package br.com.fiap.to;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SolicitacaoDificuldadeTO {
    private Long idSolicitacao;

    @NotNull
    @Positive
    private Long idDificuldade;

    public SolicitacaoDificuldadeTO() {
    }

    public SolicitacaoDificuldadeTO(Long idSolicitacao, Long idDificuldade) {
        this.idSolicitacao = idSolicitacao;
        this.idDificuldade = idDificuldade;
    }

    public Long getIdSolicitacao() {
        return idSolicitacao;
    }

    public void setIdSolicitacao(Long idSolicitacao) {
        this.idSolicitacao = idSolicitacao;
    }

    public Long getIdDificuldade() {
        return idDificuldade;
    }

    public void setIdDificuldade(Long idDificuldade) {
        this.idDificuldade = idDificuldade;
    }
}

