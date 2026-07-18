package es.urjc.manualservice.documento;

public class ArchivoNoDisponibleException extends RuntimeException {
    public ArchivoNoDisponibleException(Long documentoId) {
        super("El fichero del documento " + documentoId + " no está disponible");
    }
}