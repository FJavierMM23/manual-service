package es.urjc.manualservice.asignatura;

public record AsignaturaResponse(
        Long id,
        String nombre,
        String siglas,
        Integer curso,
        Integer cuatrimestre
) {
    public static AsignaturaResponse from(Asignatura a) {
        return new AsignaturaResponse(
                a.getId(), a.getNombre(), a.getSiglas(),
                a.getCurso(), a.getCuatrimestre());
    }
}