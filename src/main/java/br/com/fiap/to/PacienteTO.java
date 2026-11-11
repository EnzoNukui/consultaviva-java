package br.com.fiap.to;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class PacienteTO {
    private Long idPaciente;

    @NotBlank
    @Size(max = 100)
    private String nomePaciente;

    @NotNull
    @Past
    private LocalDate dataNascimento;

    @Size(max = 20)
    private String telefone;

    @Email
    @Size(max = 150)
    private String email;

    public PacienteTO() {
    }

    public PacienteTO(Long idPaciente, String nomePaciente, LocalDate dataNascimento, String telefone) {
        this(idPaciente, nomePaciente, dataNascimento, telefone, null);
    }

    public PacienteTO(Long idPaciente, String nomePaciente, LocalDate dataNascimento, String telefone, String email) {
        this.idPaciente = idPaciente;
        this.nomePaciente = nomePaciente;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.email = email;
    }

    public Long getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(Long idPaciente) {
        this.idPaciente = idPaciente;
    }

    public String getNomePaciente() {
        return nomePaciente;
    }

    public void setNomePaciente(String nomePaciente) {
        this.nomePaciente = nomePaciente;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

