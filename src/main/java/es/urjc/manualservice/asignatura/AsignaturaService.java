package es.urjc.manualservice.asignatura;

import es.urjc.manualservice.documento.DocumentoResponse;
import es.urjc.manualservice.documento.DocumentoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AsignaturaService {

    private final AsignaturaRepository repository;
    private final DocumentoService documentoService;

    public AsignaturaService(AsignaturaRepository repository, DocumentoService documentoService) {
        this.repository = repository;
        this.documentoService = documentoService;
    }

    @Transactional(readOnly = true)
    public List<AsignaturaResponse> listar() {
        return repository.findAll().stream()
                .map(AsignaturaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AsignaturaResponse obtener(Long id) {
        Asignatura a = repository.findById(id)
                .orElseThrow(() -> new AsignaturaNotFoundException(id));
        return AsignaturaResponse.from(a);
    }

    @Transactional
    public AsignaturaResponse crear(AsignaturaRequest req) {
        if (repository.existsBySiglas(req.siglas())) {
            throw new SiglasDuplicadaException(req.siglas());
        }
        Asignatura a = new Asignatura(
                req.nombre(), req.siglas(), req.curso(), req.cuatrimestre());
        return AsignaturaResponse.from(repository.save(a));
    }

    @Transactional
    public AsignaturaResponse actualizar(Long id, AsignaturaRequest req) {
        Asignatura a = repository.findById(id)
                .orElseThrow(() -> new AsignaturaNotFoundException(id));

        repository.findBySiglas(req.siglas())
                .filter(otra -> !otra.getId().equals(id))
                .ifPresent(otra -> { throw new SiglasDuplicadaException(req.siglas()); });

        a.setNombre(req.nombre());
        a.setSiglas(req.siglas());
        a.setCurso(req.curso());
        a.setCuatrimestre(req.cuatrimestre());
        return AsignaturaResponse.from(a);
    }

    /**
     * Elimina la asignatura y, en cascada, todos sus documentos (Postgres +
     * ai-service + disco), reutilizando DocumentoService.eliminar() por
     * cada uno. listarPorAsignatura ya valida que la asignatura existe.
     */
    @Transactional
    public void eliminar(Long id) {
        List<DocumentoResponse> documentos = documentoService.listarPorAsignatura(id);
        for (DocumentoResponse doc : documentos) {
            documentoService.eliminar(doc.id());
        }
        repository.deleteById(id);
    }
}