package es.urjc.manualservice.asignatura;

public class SiglasDuplicadaException extends RuntimeException {
    public SiglasDuplicadaException(String siglas) {
        super("Ya existe una asignatura con las siglas '" + siglas + "'");
    }
}