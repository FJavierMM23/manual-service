package es.urjc.manualservice.asignatura;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignaturaServiceTest {

    @Mock
    private AsignaturaRepository repository;

    @InjectMocks
    private AsignaturaService service;

    @Test
    void crear_conSiglasNuevas_guardaYDevuelve() {
        when(repository.existsBySiglas("PC")).thenReturn(false);
        when(repository.save(any(Asignatura.class)))
                .thenAnswer(inv -> inv.getArgument(0));   // devuelve lo que se guardó

        var req = new AsignaturaRequest("Programación Concurrente", "PC", 2, 2);
        var resp = service.crear(req);

        assertThat(resp.siglas()).isEqualTo("PC");
        verify(repository).save(any(Asignatura.class));
    }

    @Test
    void crear_conSiglasDuplicadas_lanzaExcepcionYNoGuarda() {
        when(repository.existsBySiglas("PC")).thenReturn(true);

        var req = new AsignaturaRequest("X", "PC", 1, null);

        assertThatThrownBy(() -> service.crear(req))
                .isInstanceOf(SiglasDuplicadaException.class);
        verify(repository, never()).save(any());   // clave: NO debe guardar
    }
}