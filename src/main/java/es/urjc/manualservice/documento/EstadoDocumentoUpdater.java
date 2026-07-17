package es.urjc.manualservice.documento;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstadoDocumentoUpdater {

    private final DocumentoRepository repository;

    public EstadoDocumentoUpdater(DocumentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void actualizar(Long documentoId, EstadoIndexacion estado) {
        repository.findById(documentoId).ifPresent(doc ->
                doc.setEstadoIndexacion(estado));   // dirty checking en transacción real
    }
}