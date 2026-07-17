package es.urjc.manualservice.documento;

public class DocumentoNotFoundException extends RuntimeException {
    public DocumentoNotFoundException(Long id) {
        super("No existe el documento con id " + id);
    }
}