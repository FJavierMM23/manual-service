package es.urjc.manualservice.asignatura;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AsignaturaService {

    private final AsignaturaRepository repository;

    public AsignaturaService(AsignaturaRepository repository) {
        this.repository = repository;
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

        // Si cambian las siglas a unas que ya usa OTRA asignatura → conflicto
        repository.findBySiglas(req.siglas())
                .filter(otra -> !otra.getId().equals(id))
                .ifPresent(otra -> { throw new SiglasDuplicadaException(req.siglas()); });

        a.setNombre(req.nombre());
        a.setSiglas(req.siglas());
        a.setCurso(req.curso());
        a.setCuatrimestre(req.cuatrimestre());
        return AsignaturaResponse.from(a);   // dirty checking: no hace falta save()
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new AsignaturaNotFoundException(id);
        }
        repository.deleteById(id);
    }
}