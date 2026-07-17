package es.urjc.manualservice.aiservice;

import java.util.Map;

// Lo que enviamos a POST /query de ai-service.
// filters es opcional (null = sin filtro); si va, es {"asignatura": "PC"}.
public record QueryRequest(
        String question,
        Map<String, String> filters
) {}