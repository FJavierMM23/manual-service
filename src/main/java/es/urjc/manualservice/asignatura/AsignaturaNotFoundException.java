package es.urjc.manualservice.asignatura;

public class AsignaturaNotFoundException extends RuntimeException {
    public AsignaturaNotFoundException(Long id) {
        super("No existe la asignatura con id " + id);
    }
}