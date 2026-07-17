package es.urjc.manualservice.aiservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Solo mapeamos el campo que nos interesa; ignoramos el resto (ollama, etc.)
@JsonIgnoreProperties(ignoreUnknown = true)
public record HealthResponse(String status) {}