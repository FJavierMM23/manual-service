package es.urjc.manualservice.documento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DocumentoRequest(
        @NotBlank @Size(max = 255)
        String titulo,

        @Size(max = 150)
        String tema,          // opcional

        @NotNull
        Long asignaturaId
) {}