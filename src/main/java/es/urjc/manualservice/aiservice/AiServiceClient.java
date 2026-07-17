package es.urjc.manualservice.aiservice;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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

        // Parte "file": Content-Disposition con filename EXPLÍCITO.
        // Esto es lo que faltaba: sin filename, FastAPI no la trata como UploadFile.
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentDispositionFormData("file", filename);
        fileHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        parts.add("file", new HttpEntity<>(fileBytes, fileHeaders));

        // Parte "metadata": string JSON como campo de formulario normal
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
}