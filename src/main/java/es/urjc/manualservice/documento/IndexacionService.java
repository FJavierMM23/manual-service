package es.urjc.manualservice.documento;

import es.urjc.manualservice.aiservice.AiServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IndexacionService {

    private static final Logger log = LoggerFactory.getLogger(IndexacionService.class);

    private final DocumentoRepository documentoRepository;
    private final AiServiceClient aiServiceClient;

    public IndexacionService(DocumentoRepository documentoRepository,
                             AiServiceClient aiServiceClient) {
        this.documentoRepository = documentoRepository;
        this.aiServiceClient = aiServiceClient;
    }

    /**
     * Indexa el documento en ai-service en segundo plano y actualiza su estado.
     * Se ejecuta en un hilo aparte: quien lo llama no espera.
     */
    @Async
    public void indexarEnSegundoPlano(Long documentoId, byte[] fileBytes,
                                      String sourceId, String metadataJson) {
        try {
            aiServiceClient.indexDocument(fileBytes, sourceId, metadataJson);
            actualizarEstado(documentoId, EstadoIndexacion.INDEXADO);
            log.info("Documento {} indexado correctamente (source={})", documentoId, sourceId);
        } catch (Exception e) {
            actualizarEstado(documentoId, EstadoIndexacion.ERROR);
            log.error("Fallo al indexar el documento {} (source={})", documentoId, sourceId, e);
        }
    }

    @Transactional
    protected void actualizarEstado(Long documentoId, EstadoIndexacion estado) {
        documentoRepository.findById(documentoId).ifPresent(doc -> {
            doc.setEstadoIndexacion(estado);
            // dirty checking: al confirmar la transacción se persiste solo
        });
    }
}