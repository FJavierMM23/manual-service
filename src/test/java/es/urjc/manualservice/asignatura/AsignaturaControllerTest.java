package es.urjc.manualservice.asignatura;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import es.urjc.manualservice.TestcontainersConfiguration;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AsignaturaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AsignaturaRepository repository;

    @BeforeEach
    void limpiar() {
        repository.deleteAll();   // aislamiento: cada test parte de BD vacía
    }

    @Test
    void crear_devuelve201ConLocation() throws Exception {
        mockMvc.perform(post("/api/asignaturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"nombre":"Programación Concurrente","siglas":"PC","curso":2,"cuatrimestre":2}"""))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.siglas").value("PC"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void crear_conSiglasDuplicadas_devuelve409() throws Exception {
        String body = """
            {"nombre":"Prog Concurrente","siglas":"PC","curso":2,"cuatrimestre":2}""";
        mockMvc.perform(post("/api/asignaturas")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/asignaturas")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void crear_conCursoInvalido_devuelve400() throws Exception {
        mockMvc.perform(post("/api/asignaturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"nombre":"Test","siglas":"X","curso":9}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtener_inexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/api/asignaturas/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listar_devuelveLasCreadas() throws Exception {
        repository.save(new Asignatura("Sistemas Operativos", "SO", 2, 1));
        repository.save(new Asignatura("Programación Concurrente", "PC", 2, 2));

        mockMvc.perform(get("/api/asignaturas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void actualizar_modificaLosCampos() throws Exception {
        Asignatura a = repository.save(new Asignatura("Nombre viejo", "SO", 2, 1));

        mockMvc.perform(put("/api/asignaturas/" + a.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"nombre":"Sistemas Operativos","siglas":"SO","curso":2,"cuatrimestre":1}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Sistemas Operativos"));
    }

    @Test
    void eliminar_devuelve204YLuego404() throws Exception {
        Asignatura a = repository.save(new Asignatura("Temporal", "TMP", 1, null));

        mockMvc.perform(delete("/api/asignaturas/" + a.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/asignaturas/" + a.getId()))
                .andExpect(status().isNotFound());
    }
}