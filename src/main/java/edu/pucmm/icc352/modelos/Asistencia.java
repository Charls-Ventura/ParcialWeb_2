package edu.pucmm.icc352.modelos;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "asistencias",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"usuario_id", "evento_id"})
        }
)
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    public Asistencia() {
    }

    public Asistencia(Usuario usuario, Evento evento, LocalDateTime fechaRegistro) {
        this.usuario = usuario;
        this.evento = evento;
        this.fechaRegistro = fechaRegistro;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    @Override
    public String toString() {
        return "Asistencia{" +
                "id=" + id +
                ", usuario=" + (usuario != null ? usuario.getId() : null) +
                ", evento=" + (evento != null ? evento.getId() : null) +
                ", fechaRegistro=" + fechaRegistro +
                '}';
    }
}