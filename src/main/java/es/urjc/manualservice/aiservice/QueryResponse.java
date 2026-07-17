package es.urjc.manualservice.aiservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record QueryResponse(
        String answer,
        List<Source> sources
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Source(
            String source,
            Integer page,      // nuevo: puede venir null (documentos sin página)
            String section,    // puede venir null (documentos sin secciones)
            Double score
    ) {}
}