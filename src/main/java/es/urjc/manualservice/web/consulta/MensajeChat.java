package es.urjc.manualservice.web.consulta;

import es.urjc.manualservice.aiservice.QueryResponse;

import java.io.Serializable;
import java.util.List;

/**
 * Un turno de la conversación. Serializable porque vive en HttpSession.
 */
public record MensajeChat(
        String pregunta,
        String respuesta,
        List<QueryResponse.Source> fuentes,
        boolean esError
) implements Serializable {

    public static MensajeChat deUsuarioYRespuesta(String pregunta, QueryResponse resp) {
        return new MensajeChat(pregunta, resp.answer(), resp.sources(), false);
    }

    public static MensajeChat deError(String pregunta, String mensajeError) {
        return new MensajeChat(pregunta, mensajeError, List.of(), true);
    }
}