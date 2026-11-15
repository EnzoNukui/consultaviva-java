package br.com.fiap.to;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AgendarConsultaPacienteTO(
        @NotNull @Positive Long idUnidade,
        @NotNull @Positive Long idHorario) {
}
