package es.urjc.manualservice.web;

import es.urjc.manualservice.asignatura.AsignaturaRequest;
import es.urjc.manualservice.asignatura.AsignaturaService;
import es.urjc.manualservice.asignatura.SiglasDuplicadaException;
import es.urjc.manualservice.documento.DocumentoService;
import es.urjc.manualservice.web.asignatura.AsignaturaForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

    private final AsignaturaService asignaturaService;
    private final DocumentoService documentoService;

    public WebController(AsignaturaService asignaturaService, DocumentoService documentoService) {
        this.asignaturaService = asignaturaService;
        this.documentoService = documentoService;
    }

    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    @GetMapping("/asignaturas")
    public String listarAsignaturas(Model model) {
        model.addAttribute("asignaturas", asignaturaService.listar());
        model.addAttribute("conteoDocumentos", documentoService.contarPorAsignatura());
        return "asignaturas/lista";
    }

    @GetMapping("/asignaturas/nueva")
    public String formularioNuevaAsignatura(
            @RequestParam(required = false) String volverA, Model model) {
        if (!model.containsAttribute("asignaturaForm")) {
            model.addAttribute("asignaturaForm", new AsignaturaForm());
        }
        model.addAttribute("volverA", volverA);
        return "asignaturas/nueva";
    }

    @PostMapping("/asignaturas")
    public String crearAsignatura(@Valid @ModelAttribute("asignaturaForm") AsignaturaForm form,
                                   BindingResult bindingResult,
                                   @RequestParam(required = false) String volverA,
                                   Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("volverA", volverA);
            return "asignaturas/nueva";
        }
        try {
            AsignaturaRequest req = new AsignaturaRequest(
                    form.getNombre(), form.getSiglas(), form.getCurso(), form.getCuatrimestre());
            asignaturaService.crear(req);
        } catch (SiglasDuplicadaException e) {
            bindingResult.rejectValue("siglas", "duplicado", e.getMessage());
            model.addAttribute("volverA", volverA);
            return "asignaturas/nueva";
        }
        return "documentos".equals(volverA) ? "redirect:/documentos/nuevo" : "redirect:/asignaturas";
    }

    @PostMapping("/asignaturas/{id}/eliminar")
    public String eliminarAsignatura(@PathVariable Long id) {
        asignaturaService.eliminar(id);
        return "redirect:/asignaturas";
    }
}