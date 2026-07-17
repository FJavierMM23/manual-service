package es.urjc.manualservice.documento;

import es.urjc.manualservice.asignatura.Asignatura;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "documento")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(name = "nombre_fichero", nullable = false)
    private String nombreFichero;

    private String tema;

    @Column(name = "source_id", nullable = false, unique = true)
    private String sourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_indexacion", nullable = false, length = 20)
    private EstadoIndexacion estadoIndexacion = EstadoIndexacion.PENDIENTE;

    @Column(name = "fecha_subida", nullable = false)
    private OffsetDateTime fechaSubida;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "asignatura_id", nullable = false)
    private Asignatura asignatura;

    protected Documento() { }

    public Documento(String titulo, String nombreFichero, String tema,
                     String sourceId, Asignatura asignatura) {
        this.titulo = titulo;
        this.nombreFichero = nombreFichero;
        this.tema = tema;
        this.sourceId = sourceId;
        this.asignatura = asignatura;
        this.fechaSubida = OffsetDateTime.now();
        this.estadoIndexacion = EstadoIndexacion.PENDIENTE;
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getNombreFichero() { return nombreFichero; }
    public void setNombreFichero(String nombreFichero) { this.nombreFichero = nombreFichero; }
    public String getTema() { return tema; }
    public void setTema(String tema) { this.tema = tema; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public EstadoIndexacion getEstadoIndexacion() { return estadoIndexacion; }
    public void setEstadoIndexacion(EstadoIndexacion estado) { this.estadoIndexacion = estado; }
    public OffsetDateTime getFechaSubida() { return fechaSubida; }
    public Asignatura getAsignatura() { return asignatura; }
    public void setAsignatura(Asignatura asignatura) { this.asignatura = asignatura; }
}