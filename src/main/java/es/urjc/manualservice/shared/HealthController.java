package es.urjc.manualservice.shared;

import es.urjc.manualservice.aiservice.AiServiceClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    private final AiServiceClient aiServiceClient;

    public HealthController(AiServiceClient aiServiceClient) {
        this.aiServiceClient = aiServiceClient;
    }

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        boolean aiOk = aiServiceClient.isHealthy();
        return Map.of(
                "manualService", "ok",
                "aiService", aiOk ? "ok" : "unreachable"
        );
    }
}