package es.urjc.manualservice.documento;

import es.urjc.manualservice.asignatura.Asignatura;
import es.urjc.manualservice.asignatura.AsignaturaNotFoundException;
import es.urjc.manualservice.asignatura.AsignaturaRepository;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.JacksonException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final IndexacionService indexacionService;
    private final ObjectMapper objectMapper;

    public DocumentoService(DocumentoRepository documentoRepository,
                            AsignaturaRepository asignaturaRepository,
                            IndexacionService indexacionService,
                            ObjectMapper objectMapper) {
        this.documentoRepository = documentoRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.indexacionService = indexacionService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public DocumentoResponse crear(DocumentoRequest req, byte[] fileBytes, String nombreFichero) {
        Asignatura asignatura = asignaturaRepository.findById(req.asignaturaId())
                .orElseThrow(() -> new AsignaturaNotFoundException(req.asignaturaId()));

        // sourceId único: SIGLAS_uuidcorto_nombreoriginal → nunca colisiona en Chroma
        String sourceId = asignatura.getSiglas() + "_"
                + UUID.randomUUID().toString().substring(0, 8) + "_"
                + nombreFichero;

        Documento doc = new Documento(
                req.titulo(), nombreFichero, req.tema(), sourceId, asignatura);
        documentoRepository.save(doc);   // nace como PENDIENTE

        // Metadatos que ai-service guardará en cada chunk (opción B).
        // La clave 'asignatura' = siglas estables, para filtrar luego por where.
        String metadataJson = construirMetadata(asignatura.getSiglas(), req.tema());

        // Dispara la indexación en segundo plano y responde ya.
        indexacionService.indexarEnSegundoPlano(
                doc.getId(), fileBytes, sourceId, metadataJson);

        return DocumentoResponse.from(doc);   // estado PENDIENTE
    }

    private String construirMetadata(String siglas, String tema) {
        try {
            Map<String, String> meta = (tema == null || tema.isBlank())
                    ? Map.of("asignatura", siglas)
                    : Map.of("asignatura", siglas, "tema", tema);
            return objectMapper.writeValueAsString(meta);
        } catch (JacksonException e) {
            return "{\"asignatura\":\"" + siglas + "\"}";   // fallback improbable
        }
    }

    @Transactional(readOnly = true)
    public List<DocumentoResponse> listarPorAsignatura(Long asignaturaId) {
        if (!asignaturaRepository.existsById(asignaturaId)) {
            throw new AsignaturaNotFoundException(asignaturaId);
        }
        return documentoRepository.findByAsignaturaId(asignaturaId).stream()
                .map(DocumentoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentoResponse obtener(Long id) {
        Documento d = documentoRepository.findById(id)
                .orElseThrow(() -> new DocumentoNotFoundException(id));
        return DocumentoResponse.from(d);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!documentoRepository.existsById(id)) {
            throw new DocumentoNotFoundException(id);
        }
        documentoRepository.deleteById(id);
    }
}