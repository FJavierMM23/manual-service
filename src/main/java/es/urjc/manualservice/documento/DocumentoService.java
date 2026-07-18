package es.urjc.manualservice.documento;

import es.urjc.manualservice.aiservice.AiServiceClient;
import es.urjc.manualservice.asignatura.Asignatura;
import es.urjc.manualservice.asignatura.AsignaturaNotFoundException;
import es.urjc.manualservice.asignatura.AsignaturaRepository;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.JacksonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentoService {

    private static final Logger log = LoggerFactory.getLogger(DocumentoService.class);

    private final DocumentoRepository documentoRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final IndexacionService indexacionService;
    private final AiServiceClient aiServiceClient;
    private final ObjectMapper objectMapper;
    private final String storagePath;

    public DocumentoService(DocumentoRepository documentoRepository,
                            AsignaturaRepository asignaturaRepository,
                            IndexacionService indexacionService,
                            AiServiceClient aiServiceClient,
                            ObjectMapper objectMapper,
                            @Value("${documento.storage-path}") String storagePath) {
        this.documentoRepository = documentoRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.indexacionService = indexacionService;
        this.aiServiceClient = aiServiceClient;
        this.objectMapper = objectMapper;
        this.storagePath = storagePath;
    }

    @Transactional
    public DocumentoResponse crear(DocumentoRequest req, byte[] fileBytes, String nombreFichero) {
        Asignatura asignatura = asignaturaRepository.findById(req.asignaturaId())
                .orElseThrow(() -> new AsignaturaNotFoundException(req.asignaturaId()));

        String sourceId = asignatura.getSiglas() + "_"
                + UUID.randomUUID().toString().substring(0, 8) + "_"
                + nombreFichero;

        Documento doc = new Documento(
                req.titulo(), nombreFichero, req.tema(), sourceId, asignatura);

        doc.setRutaFichero(guardarEnDisco(fileBytes, sourceId));

        documentoRepository.save(doc);

        String metadataJson = construirMetadata(asignatura.getSiglas(), req.tema());

        indexacionService.indexarEnSegundoPlano(
                doc.getId(), fileBytes, sourceId, metadataJson);

        return DocumentoResponse.from(doc);
    }

    private String guardarEnDisco(byte[] fileBytes, String sourceId) {
        try {
            Path dir = Path.of(storagePath);
            Files.createDirectories(dir);
            Path destino = dir.resolve(sourceId);
            Files.write(destino, fileBytes);
            return destino.toString();
        } catch (IOException e) {
            log.warn("No se pudo guardar copia local de '{}' para lectura posterior", sourceId, e);
            return null;
        }
    }

    @Transactional(readOnly = true)
    public ArchivoDocumento obtenerArchivo(Long id) {
        Documento d = documentoRepository.findById(id)
                .orElseThrow(() -> new DocumentoNotFoundException(id));
        if (d.getRutaFichero() == null) {
            throw new ArchivoNoDisponibleException(id);
        }
        try {
            byte[] contenido = Files.readAllBytes(Path.of(d.getRutaFichero()));
            return new ArchivoDocumento(contenido, d.getNombreFichero());
        } catch (IOException e) {
            throw new ArchivoNoDisponibleException(id);
        }
    }

    private String construirMetadata(String siglas, String tema) {
        try {
            Map<String, String> meta = (tema == null || tema.isBlank())
                    ? Map.of("asignatura", siglas)
                    : Map.of("asignatura", siglas, "tema", tema);
            return objectMapper.writeValueAsString(meta);
        } catch (JacksonException e) {
            return "{\"asignatura\":\"" + siglas + "\"}";
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
    public List<DocumentoResponse> listarTodos() {
        return documentoRepository.findAll().stream()
                .map(DocumentoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> contarPorAsignatura() {
        return documentoRepository.contarPorAsignatura().stream()
                .collect(Collectors.toMap(
                        fila -> (Long) fila[0],
                        fila -> (Long) fila[1]));
    }

    @Transactional(readOnly = true)
    public DocumentoResponse obtener(Long id) {
        Documento d = documentoRepository.findById(id)
                .orElseThrow(() -> new DocumentoNotFoundException(id));
        return DocumentoResponse.from(d);
    }

    /**
     * Elimina el documento en sus tres sitios: ai-service (ChromaDB), la
     * copia en disco, y Postgres. El borrado externo (ai-service/disco) es
     * "best effort": si falla, se registra un aviso pero el borrado en
     * Postgres continúa, para que el documento no quede fantasma en tu
     * listado aunque la limpieza externa no se complete.
     */
    @Transactional
    public void eliminar(Long id) {
        Documento d = documentoRepository.findById(id)
                .orElseThrow(() -> new DocumentoNotFoundException(id));

        try {
            aiServiceClient.deleteDocument(d.getSourceId());
        } catch (Exception e) {
            log.warn("No se pudo eliminar '{}' en ai-service (¿está caído?)", d.getSourceId(), e);
        }

        if (d.getRutaFichero() != null) {
            try {
                Files.deleteIfExists(Path.of(d.getRutaFichero()));
            } catch (IOException e) {
                log.warn("No se pudo eliminar el fichero local '{}'", d.getRutaFichero(), e);
            }
        }

        documentoRepository.deleteById(id);
    }
}