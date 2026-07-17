package es.urjc.manualservice.consulta;

import es.urjc.manualservice.aiservice.QueryResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ConsultaController {

    private final ConsultaService service;

    public ConsultaController(ConsultaService service) {
        this.service = service;
    }

    @PostMapping("/preguntar")
    public QueryResponse preguntar(@Valid @RequestBody PreguntaRequest req) {
        return service.preguntar(req);
    }
}