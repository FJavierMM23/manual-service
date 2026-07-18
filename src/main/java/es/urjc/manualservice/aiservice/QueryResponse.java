package es.urjc.manualservice.aiservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record QueryResponse(
        String answer,
        List<Source> sources
) implements Serializable {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Source(
            String source,
            Integer page,
            String section,
            Double score
    ) implements Serializable {}
}