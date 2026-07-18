package es.urjc.manualservice.web.documento;

import es.urjc.manualservice.asignatura.AsignaturaService;
import es.urjc.manualservice.documento.DocumentoRequest;
import es.urjc.manualservice.documento.DocumentoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
@RequestMapping("/documentos")
public class DocumentoWebController {

    private final DocumentoService documentoService;
    private final AsignaturaService asignaturaService;

    public DocumentoWebController(DocumentoService documentoService,
                                  AsignaturaService asignaturaService) {
        this.documentoService = documentoService;
        this.asignaturaService = asignaturaService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) Long asignaturaId, Model model) {
        model.addAttribute("documentos",
                asignaturaId == null
                        ? documentoService.listarTodos()
                        : documentoService.listarPorAsignatura(asignaturaId));
        model.addAttribute("asignaturas", asignaturaService.listar());
        model.addAttribute("asignaturaSeleccionada", asignaturaId);
        return "documentos/lista";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        if (!model.containsAttribute("documentoForm")) {
            model.addAttribute("documentoForm", new DocumentoForm());
        }
        model.addAttribute("asignaturas", asignaturaService.listar());
        return "documentos/nueva";
    }

    @PostMapping
    public String subir(@Valid @ModelAttribute("documentoForm") DocumentoForm form,
                        BindingResult bindingResult,
                        Model model) {

        if (form.getFile() == null || form.getFile().isEmpty()) {
            bindingResult.rejectValue("file", "vacio", "Selecciona un fichero");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("asignaturas", asignaturaService.listar());
            return "documentos/nueva";
        }

        try {
            DocumentoRequest req = new DocumentoRequest(
                    form.getTitulo(), form.getTema(), form.getAsignaturaId());
            documentoService.crear(req, form.getFile().getBytes(),
                    form.getFile().getOriginalFilename());
        } catch (IOException e) {
            bindingResult.reject("errorLectura", "No se pudo leer el fichero");
            model.addAttribute("asignaturas", asignaturaService.listar());
            return "documentos/nueva";
        }

        return "redirect:/documentos";   // PRG, igual que con asignaturas
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {
        documentoService.eliminar(id);
        return "redirect:/documentos";
    }
}
