package es.urjc.manualservice.asignatura;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/asignaturas")
public class AsignaturaController {

    private final AsignaturaService service;

    public AsignaturaController(AsignaturaService service) {
        this.service = service;
    }

    @GetMapping
    public List<AsignaturaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public AsignaturaResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public ResponseEntity<AsignaturaResponse> crear(
            @Valid @RequestBody AsignaturaRequest req,
            UriComponentsBuilder uriBuilder) {

        AsignaturaResponse creada = service.crear(req);
        URI location = uriBuilder.path("/api/asignaturas/{id}")
                .buildAndExpand(creada.id()).toUri();
        return ResponseEntity.created(location).body(creada);   // 201 + Location
    }

    @PutMapping("/{id}")
    public AsignaturaResponse actualizar(
            @PathVariable Long id, @Valid @RequestBody AsignaturaRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)   // 204
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}