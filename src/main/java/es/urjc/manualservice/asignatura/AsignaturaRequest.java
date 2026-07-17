package es.urjc.manualservice.asignatura;

import jakarta.validation.constraints.*;

public record AsignaturaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150)
        String nombre,

        @NotBlank(message = "Las siglas son obligatorias")
        @Size(max = 10)
        String siglas,

        @NotNull @Min(1) @Max(4)
        Integer curso,

        @Min(1) @Max(2)
        Integer cuatrimestre   // opcional: sin @NotNull
) {}