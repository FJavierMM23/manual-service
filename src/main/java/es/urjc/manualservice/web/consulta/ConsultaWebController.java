package es.urjc.manualservice.web.consulta;

import es.urjc.manualservice.asignatura.AsignaturaService;
import es.urjc.manualservice.consulta.ConsultaService;
import es.urjc.manualservice.consulta.PreguntaRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ConsultaWebController {

    private static final String SESSION_KEY = "historialChat";

    private final ConsultaService consultaService;
    private final AsignaturaService asignaturaService;

    public ConsultaWebController(ConsultaService consultaService,
                                 AsignaturaService asignaturaService) {
        this.consultaService = consultaService;
        this.asignaturaService = asignaturaService;
    }

    @GetMapping("/preguntar")
    public String vista(@RequestParam(required = false) String asignaturaSiglas,
                        HttpSession session, Model model) {
        model.addAttribute("asignaturas", asignaturaService.listar());
        model.addAttribute("asignaturaSiglas", asignaturaSiglas);
        model.addAttribute("historial", obtenerHistorial(session));
        return "preguntar";
    }

    @PostMapping("/preguntar")
    public String preguntar(@RequestParam String pregunta,
                            @RequestParam(required = false) String asignaturaSiglas,
                            HttpSession session, Model model) {

        List<MensajeChat> historial = obtenerHistorial(session);

        if (pregunta != null && !pregunta.isBlank()) {
            try {
                var respuesta = consultaService.preguntar(
                        new PreguntaRequest(pregunta, asignaturaSiglas));
                historial.add(MensajeChat.deUsuarioYRespuesta(pregunta, respuesta));
            } catch (Exception e) {
                historial.add(MensajeChat.deError(pregunta,
                        "No se pudo obtener respuesta. ¿Está ai-service en marcha?"));
            }
            session.setAttribute(SESSION_KEY, historial);
        }

        model.addAttribute("asignaturas", asignaturaService.listar());
        model.addAttribute("asignaturaSiglas", asignaturaSiglas);
        model.addAttribute("historial", historial);
        return "preguntar";
    }

    @PostMapping("/preguntar/nueva")
    public String nuevaConversacion(HttpSession session) {
        session.removeAttribute(SESSION_KEY);
        return "redirect:/preguntar";
    }

    @SuppressWarnings("unchecked")
    private List<MensajeChat> obtenerHistorial(HttpSession session) {
        Object existente = session.getAttribute(SESSION_KEY);
        return existente != null ? (List<MensajeChat>) existente : new ArrayList<>();
    }
}