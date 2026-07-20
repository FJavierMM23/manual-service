package es.urjc.manualservice.web.consulta;

import es.urjc.manualservice.aiservice.ModelsResponse;
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
                        @RequestParam(required = false) String model,
                        HttpSession session, Model uiModel) {
        rellenarSelectores(uiModel, asignaturaSiglas, model);
        uiModel.addAttribute("historial", obtenerHistorial(session));
        return "preguntar";
    }

    @PostMapping("/preguntar")
    public String preguntar(@RequestParam String pregunta,
                            @RequestParam(required = false) String asignaturaSiglas,
                            @RequestParam(required = false) String model,
                            HttpSession session, Model uiModel) {

        List<MensajeChat> historial = obtenerHistorial(session);

        if (pregunta != null && !pregunta.isBlank()) {
            try {
                var respuesta = consultaService.preguntar(
                        new PreguntaRequest(pregunta, asignaturaSiglas, model));
                historial.add(MensajeChat.deUsuarioYRespuesta(pregunta, respuesta));
            } catch (Exception e) {
                historial.add(MensajeChat.deError(pregunta,
                        "No se pudo obtener respuesta. ¿Está ai-service en marcha?"));
            }
            session.setAttribute(SESSION_KEY, historial);
        }

        rellenarSelectores(uiModel, asignaturaSiglas, model);
        uiModel.addAttribute("historial", historial);
        return "preguntar";
    }

    @PostMapping("/preguntar/nueva")
    public String nuevaConversacion(HttpSession session) {
        session.removeAttribute(SESSION_KEY);
        return "redirect:/preguntar";
    }

    /**
     * Rellena los dos desplegables (asignatura y modelo LLM). Si ai-service
     * no responde al listar modelos, degrada a lista vacía en vez de romper
     * la página: el formulario sigue siendo usable con el modelo por defecto.
     */
    private void rellenarSelectores(Model uiModel, String asignaturaSiglas, String modeloSeleccionado) {
        uiModel.addAttribute("asignaturas", asignaturaService.listar());
        uiModel.addAttribute("asignaturaSiglas", asignaturaSiglas);

        List<String> modelos = List.of();
        String modeloPorDefecto = null;
        try {
            ModelsResponse resp = consultaService.listarModelos();
            modelos = resp.models();
            modeloPorDefecto = resp.defaultModel();
        } catch (Exception ignored) {
            // ai-service caído: seguimos sin lista de modelos.
        }
        uiModel.addAttribute("modelos", modelos);
        uiModel.addAttribute("modeloSeleccionado",
                (modeloSeleccionado != null && !modeloSeleccionado.isBlank())
                        ? modeloSeleccionado : modeloPorDefecto);
    }

    @SuppressWarnings("unchecked")
    private List<MensajeChat> obtenerHistorial(HttpSession session) {
        Object existente = session.getAttribute(SESSION_KEY);
        return existente != null ? (List<MensajeChat>) existente : new ArrayList<>();
    }
}