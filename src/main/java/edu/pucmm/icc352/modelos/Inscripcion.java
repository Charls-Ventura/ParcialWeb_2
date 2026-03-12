package edu.pucmm.icc352.modelos;

import jakarta.persistence.*;

@Entity
@Table(
        name = "inscripciones",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"usuario_id", "evento_id"})
        }
)
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @Column(name = "token_qr", nullable = false, unique = true, length = 255)
    private String tokenQr;

    @Column(nullable = false)
    private boolean cancelada;

    public Inscripcion() {
    }

    public Inscripcion(Usuario usuario, Evento evento, String tokenQr) {
        this.usuario = usuario;
        this.evento = evento;
        this.tokenQr = tokenQr;
        this.cancelada = false;
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

    public String getTokenQr() {
        return tokenQr;
    }

    public void setTokenQr(String tokenQr) {
        this.tokenQr = tokenQr;
    }

    public boolean isCancelada() {
        return cancelada;
    }

    public void setCancelada(boolean cancelada) {
        this.cancelada = cancelada;
    }

    @Override
    public String toString() {
        return "Inscripcion{" +
                "id=" + id +
                ", usuario=" + (usuario != null ? usuario.getId() : null) +
                ", evento=" + (evento != null ? evento.getId() : null) +
                ", cancelada=" + cancelada +
                '}';
    }
}