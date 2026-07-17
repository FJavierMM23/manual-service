package es.urjc.manualservice.aiservice;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class AiServiceClient {

    private final RestClient restClient;

    public AiServiceClient(RestClient aiServiceRestClient) {
        this.restClient = aiServiceRestClient;
    }

    /** Comprueba que ai-service (y su Ollama) están vivos. */
    public boolean isHealthy() {
        try {
            HealthResponse health = restClient.get()
                    .uri("/health")
                    .retrieve()
                    .body(HealthResponse.class);
            return health != null && "ok".equalsIgnoreCase(health.status());
        } catch (Exception e) {
            return false;   // si no responde o falla, lo tratamos como no-sano
        }
    }

    /**
     * Sube e indexa un documento en ai-service.
     * @param fileBytes contenido del fichero
     * @param filename  nombre con el que ai-service lo identificará (su "source")
     * @param metadataJson metadatos como string JSON, o null
     */
    public void indexDocument(byte[] fileBytes, String filename, String metadataJson) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();

        // El fichero, con su nombre. ByteArrayResource + getFilename() hace que
        // llegue como multipart "file" con nombre, igual que un curl -F "file=@..."
        Resource fileResource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
        parts.add("file", fileResource);

        if (metadataJson != null) {
            parts.add("metadata", metadataJson);   // el campo Form "metadata" de ai-service
        }

        restClient.post()
                .uri("/documents")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(parts)
                .retrieve()
                .toBodilessEntity();   // no nos interesa el cuerpo, solo que no falle
    }
}