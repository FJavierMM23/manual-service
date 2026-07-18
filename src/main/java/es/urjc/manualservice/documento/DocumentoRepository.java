package es.urjc.manualservice.documento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    List<Documento> findByAsignaturaId(Long asignaturaId);
    Optional<Documento> findBySourceId(String sourceId);
    boolean existsBySourceId(String sourceId);
    @Query("SELECT d.asignatura.id, COUNT(d) FROM Documento d GROUP BY d.asignatura.id")
    List<Object[]> contarPorAsignatura();
}