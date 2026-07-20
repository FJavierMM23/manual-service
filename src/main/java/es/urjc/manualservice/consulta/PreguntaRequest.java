package es.urjc.manualservice.consulta;

import jakarta.validation.constraints.NotBlank;

public record PreguntaRequest(
        @NotBlank(message = "La pregunta no puede estar vacía")
        String pregunta,
        String asignaturaSiglas,  // opcional: null/vacío = pregunta a todo
        String model              // opcional: null/vacío = modelo por defecto de ai-service
) {}