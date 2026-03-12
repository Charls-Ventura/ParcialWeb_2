package edu.pucmm.icc352.modelos;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(nullable = false, length = 150)
    private String ubicacion;

    @Column(nullable = false)
    private int cupoMaximo;

    @Column(nullable = false)
    private boolean publicado;

    @Column(nullable = false)
    private boolean cancelado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organizador_id", nullable = false)
    private Usuario organizador;

    public Evento() {
    }

    public Evento(String titulo, String descripcion, LocalDate fecha, LocalTime hora,
                  String ubicacion, int cupoMaximo, Usuario organizador) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.hora = hora;
        this.ubicacion = ubicacion;
        this.cupoMaximo = cupoMaximo;
        this.organizador = organizador;
        this.publicado = false;
        this.cancelado = false;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public void setCupoMaximo(int cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }

    public boolean isPublicado() {
        return publicado;
    }

    public void setPublicado(boolean publicado) {
        this.publicado = publicado;
    }

    public boolean isCancelado() {
        return cancelado;
    }

    public void setCancelado(boolean cancelado) {
        this.cancelado = cancelado;
    }

    public Usuario getOrganizador() {
        return organizador;
    }

    public void setOrganizador(Usuario organizador) {
        this.organizador = organizador;
    }

    @Override
    public String toString() {
        return "Evento{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", fecha=" + fecha +
                ", hora=" + hora +
                ", ubicacion='" + ubicacion + '\'' +
                ", cupoMaximo=" + cupoMaximo +
                ", publicado=" + publicado +
                ", cancelado=" + cancelado +
                '}';
    }
}