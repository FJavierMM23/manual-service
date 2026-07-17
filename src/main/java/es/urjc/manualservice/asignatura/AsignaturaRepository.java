package es.urjc.manualservice.asignatura;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AsignaturaRepository extends JpaRepository<Asignatura, Long> {
    Optional<Asignatura> findBySiglas(String siglas);
    boolean existsBySiglas(String siglas);
}