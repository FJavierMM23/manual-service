package es.urjc.manualservice.aiservice;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class AiServiceClient {

    private final RestClient restClient;

    public AiServiceClient(RestClient aiServiceRestClient) {
        this.restClient = aiServiceRestClient;
    }

    public boolean isHealthy() {
        try {
            HealthResponse health = restClient.get()
                    .uri("/health")
                    .retrieve()
                    .body(HealthResponse.class);
            return health != null && "ok".equalsIgnoreCase(health.status());
        } catch (Exception e) {
            return false;
        }
    }

    public void indexDocument(byte[] fileBytes, String filename, String metadataJson) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();

        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentDispositionFormData("file", filename);
        fileHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        parts.add("file", new HttpEntity<>(fileBytes, fileHeaders));

        if (metadataJson != null) {
            parts.add("metadata", metadataJson);
        }

        restClient.post()
                .uri("/documents")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(parts)
                .retrieve()
                .toBodilessEntity();
    }

    public QueryResponse query(String question, String asignaturaSiglas) {
        Map<String, String> filters = (asignaturaSiglas == null || asignaturaSiglas.isBlank())
                ? null
                : Map.of("asignatura", asignaturaSiglas);

        return restClient.post()
                .uri("/query")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new QueryRequest(question, filters))
                .retrieve()
                .body(QueryResponse.class);
    }

    /** Borra el documento en ai-service (ChromaDB) por su sourceId. */
    public void deleteDocument(String sourceId) {
        restClient.delete()
                .uri("/documents/{source}", sourceId)
                .retrieve()
                .toBodilessEntity();
    }
}