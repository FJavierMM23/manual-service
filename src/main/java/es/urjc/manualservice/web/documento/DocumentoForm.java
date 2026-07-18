package es.urjc.manualservice.web.documento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class DocumentoForm {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255)
    private String titulo;

    @Size(max = 150)
    private String tema;   // opcional

    @NotNull(message = "Selecciona una asignatura")
    private Long asignaturaId;

    private MultipartFile file;   // Spring lo rellena solo si enctype=multipart/form-data

    public DocumentoForm() { }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getTema() { return tema; }
    public void setTema(String tema) { this.tema = tema; }
    public Long getAsignaturaId() { return asignaturaId; }
    public void setAsignaturaId(Long asignaturaId) { this.asignaturaId = asignaturaId; }
    public MultipartFile getFile() { return file; }
    public void setFile(MultipartFile file) { this.file = file; }
}