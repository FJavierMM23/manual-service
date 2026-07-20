package es.urjc.manualservice.aiservice;

import java.util.Map;

// Lo que enviamos a POST /query de ai-service.
// filters y model son opcionales (null = sin filtro / modelo por defecto).
public record QueryRequest(
        String question,
        Map<String, String> filters,
        String model
) {}