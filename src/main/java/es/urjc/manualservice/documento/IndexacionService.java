package es.urjc.manualservice.documento;

import es.urjc.manualservice.aiservice.AiServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class IndexacionService {

    private static final Logger log = LoggerFactory.getLogger(IndexacionService.class);

    private final AiServiceClient aiServiceClient;
    private final EstadoDocumentoUpdater estadoUpdater;

    public IndexacionService(AiServiceClient aiServiceClient,
                             EstadoDocumentoUpdater estadoUpdater) {
        this.aiServiceClient = aiServiceClient;
        this.estadoUpdater = estadoUpdater;
    }

    @Async
    public void indexarEnSegundoPlano(Long documentoId, byte[] fileBytes,
                                      String sourceId, String metadataJson) {
        try {
            aiServiceClient.indexDocument(fileBytes, sourceId, metadataJson);
            estadoUpdater.actualizar(documentoId, EstadoIndexacion.INDEXADO);
            log.info("Documento {} indexado correctamente (source={})", documentoId, sourceId);
        } catch (Exception e) {
            estadoUpdater.actualizar(documentoId, EstadoIndexacion.ERROR);
            log.error("Fallo al indexar el documento {} (source={})", documentoId, sourceId, e);
        }
    }
}