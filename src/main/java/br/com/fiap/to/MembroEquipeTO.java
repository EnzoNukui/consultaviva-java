package br.com.fiap.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class MembroEquipeTO {
    private Long idMembroEquipe;

    @NotBlank
    @Size(max = 100)
    private String nomeMembroEquipe;

    @NotBlank
    @Size(max = 60)
    private String cargo;

    @NotBlank
    @Size(max = 1)
    @Pattern(regexp = "[SN]")
    private String atendimento;

    @NotBlank
    @Size(max = 1)
    @Pattern(regexp = "[SN]")
    private String apoioAdministrativo;

    @NotBlank
    @Size(max = 1)
    @Pattern(regexp = "[SN]")
    private String autorizaModalidade;

    @Size(max = 40)
    private String registroProfissional;

    public MembroEquipeTO() {
    }

    public MembroEquipeTO(
            Long idMembroEquipe, String nomeMembroEquipe, String cargo, String atendimento,
            String apoioAdministrativo, String autorizaModalidade, String registroProfissional) {
        this.idMembroEquipe = idMembroEquipe;
        this.nomeMembroEquipe = nomeMembroEquipe;
        this.cargo = cargo;
        this.atendimento = atendimento;
        this.apoioAdministrativo = apoioAdministrativo;
        this.autorizaModalidade = autorizaModalidade;
        this.registroProfissional = registroProfissional;
    }

    public Long getIdMembroEquipe() {
        return idMembroEquipe;
    }

    public void setIdMembroEquipe(Long idMembroEquipe) {
        this.idMembroEquipe = idMembroEquipe;
    }

    public String getNomeMembroEquipe() {
        return nomeMembroEquipe;
    }

    public void setNomeMembroEquipe(String nomeMembroEquipe) {
        this.nomeMembroEquipe = nomeMembroEquipe;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getAtendimento() {
        return atendimento;
    }

    public void setAtendimento(String atendimento) {
        this.atendimento = atendimento;
    }

    public String getApoioAdministrativo() {
        return apoioAdministrativo;
    }

    public void setApoioAdministrativo(String apoioAdministrativo) {
        this.apoioAdministrativo = apoioAdministrativo;
    }

    public String getAutorizaModalidade() {
        return autorizaModalidade;
    }

    public void setAutorizaModalidade(String autorizaModalidade) {
        this.autorizaModalidade = autorizaModalidade;
    }

    public String getRegistroProfissional() {
        return registroProfissional;
    }

    public void setRegistroProfissional(String registroProfissional) {
        this.registroProfissional = registroProfissional;
    }
}

