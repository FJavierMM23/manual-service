package es.urjc.manualservice.web.asignatura;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

public class AsignaturaForm {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150)
    private String nombre;

    @NotBlank(message = "Las siglas son obligatorias")
    @Size(max = 10)
    private String siglas;

    @NotNull(message = "Selecciona un curso")
    @Min(1) @Max(4)
    private Integer curso;

    @Min(1) @Max(2)
    private Integer cuatrimestre;   // opcional

    public AsignaturaForm() { }   // requerido por Thymeleaf/Spring MVC

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getSiglas() { return siglas; }
    public void setSiglas(String siglas) { this.siglas = siglas; }
    public Integer getCurso() { return curso; }
    public void setCurso(Integer curso) { this.curso = curso; }
    public Integer getCuatrimestre() { return cuatrimestre; }
    public void setCuatrimestre(Integer cuatrimestre) { this.cuatrimestre = cuatrimestre; }
}