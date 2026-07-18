package es.urjc.manualservice.documento;

import java.time.OffsetDateTime;

public record DocumentoResponse(
        Long id,
        String titulo,
        String nombreFichero,
        String tema,
        String sourceId,
        EstadoIndexacion estadoIndexacion,
        OffsetDateTime fechaSubida,
        Long asignaturaId,
        String asignaturaSiglas,
        boolean tieneArchivo
) {
    public static DocumentoResponse from(Documento d) {
        return new DocumentoResponse(
                d.getId(),
                d.getTitulo(),
                d.getNombreFichero(),
                d.getTema(),
                d.getSourceId(),
                d.getEstadoIndexacion(),
                d.getFechaSubida(),
                d.getAsignatura().getId(),
                d.getAsignatura().getSiglas(),
                d.getRutaFichero() != null
        );
    }
}