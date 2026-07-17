package es.urjc.manualservice.asignatura;

import jakarta.persistence.*;

@Entity
@Table(name = "asignatura")
public class Asignatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 10, unique = true)
    private String siglas;

    @Column(nullable = false)
    private Integer curso;

    private Integer cuatrimestre;   // nullable: se deja sin @Column extra

    protected Asignatura() { }      // JPA lo exige (constructor sin args)

    public Asignatura(String nombre, String siglas, Integer curso, Integer cuatrimestre) {
        this.nombre = nombre;
        this.siglas = siglas;
        this.curso = curso;
        this.cuatrimestre = cuatrimestre;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getSiglas() { return siglas; }
    public void setSiglas(String siglas) { this.siglas = siglas; }
    public Integer getCurso() { return curso; }
    public void setCurso(Integer curso) { this.curso = curso; }
    public Integer getCuatrimestre() { return cuatrimestre; }
    public void setCuatrimestre(Integer cuatrimestre) { this.cuatrimestre = cuatrimestre; }
}