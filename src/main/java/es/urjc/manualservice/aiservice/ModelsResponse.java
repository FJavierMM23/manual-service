package es.urjc.manualservice.aiservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

// Mapea GET /models. "default" es palabra reservada en Java, así que el
// componente se llama defaultModel y se mapea al JSON "default" con @JsonProperty.
@JsonIgnoreProperties(ignoreUnknown = true)
public record ModelsResponse(
        List<String> models,
        @JsonProperty("default") String defaultModel
) implements Serializable {}