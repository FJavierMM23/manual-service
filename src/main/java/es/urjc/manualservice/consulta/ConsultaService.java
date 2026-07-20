package es.urjc.manualservice.consulta;

import es.urjc.manualservice.aiservice.AiServiceClient;
import es.urjc.manualservice.aiservice.ModelsResponse;
import es.urjc.manualservice.aiservice.QueryResponse;
import org.springframework.stereotype.Service;

@Service
public class ConsultaService {

    private final AiServiceClient aiServiceClient;

    public ConsultaService(AiServiceClient aiServiceClient) {
        this.aiServiceClient = aiServiceClient;
    }

    public QueryResponse preguntar(PreguntaRequest req) {
        return aiServiceClient.query(req.pregunta(), req.asignaturaSiglas(), req.model());
    }

    public ModelsResponse listarModelos() {
        return aiServiceClient.listModels();
    }
}