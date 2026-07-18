package es.urjc.manualservice.documento;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DocumentoController {

    private final DocumentoService service;

    public DocumentoController(DocumentoService service) {
        this.service = service;
    }

    @PostMapping(path = "/documentos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoResponse> subir(
            @RequestPart("metadata") @Valid DocumentoRequest metadata,
            @RequestPart("file") MultipartFile file,
            UriComponentsBuilder uriBuilder) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("El fichero está vacío");
        }

        DocumentoResponse creado = service.crear(
                metadata, file.getBytes(), file.getOriginalFilename());

        URI location = uriBuilder.path("/api/documentos/{id}")
                .buildAndExpand(creado.id()).toUri();
        return ResponseEntity.accepted().location(location).body(creado);
    }

    @GetMapping("/asignaturas/{asignaturaId}/documentos")
    public List<DocumentoResponse> listarPorAsignatura(@PathVariable Long asignaturaId) {
        return service.listarPorAsignatura(asignaturaId);
    }

    @GetMapping("/documentos/{id}")
    public DocumentoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/documentos/{id}/archivo")
    public ResponseEntity<byte[]> verArchivo(@PathVariable Long id) {
        ArchivoDocumento archivo = service.obtenerArchivo(id);
        MediaType tipo = detectarTipo(archivo.nombreFichero());
        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + archivo.nombreFichero() + "\"")
                .body(archivo.contenido());
    }

    private MediaType detectarTipo(String nombreFichero) {
        if (nombreFichero.toLowerCase().endsWith(".md")) {
            return MediaType.TEXT_PLAIN;   // el navegador lo abre como texto legible
        }
        String probable = URLConnection.guessContentTypeFromName(nombreFichero);
        return probable != null
                ? MediaType.parseMediaType(probable)
                : MediaType.APPLICATION_OCTET_STREAM;
    }

    @DeleteMapping("/documentos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}