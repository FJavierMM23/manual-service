package es.urjc.manualservice.documento;

public enum EstadoIndexacion {
    PENDIENTE,   // creado en MySQL, aún no enviado a ai-service
    INDEXADO,    // ai-service confirmó la indexación
    ERROR        // falló la indexación
}