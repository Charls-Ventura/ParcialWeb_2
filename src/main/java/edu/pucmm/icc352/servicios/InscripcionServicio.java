package edu.pucmm.icc352.servicios;

import edu.pucmm.icc352.modelos.Evento;
import edu.pucmm.icc352.modelos.Inscripcion;
import edu.pucmm.icc352.modelos.Usuario;
import edu.pucmm.icc352.repositorios.InscripcionRepositorio;
import edu.pucmm.icc352.utilidades.QRUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class InscripcionServicio {

    private final InscripcionRepositorio inscripcionRepositorio = new InscripcionRepositorio();

    public Inscripcion inscribirUsuario(Usuario usuario, Evento evento) {
        validarInscripcion(usuario, evento);

        long inscritosActivos = inscripcionRepositorio.contarInscripcionesActivasPorEvento(evento);
        if (inscritosActivos >= evento.getCupoMaximo()) {
            throw new RuntimeException("No hay cupo disponible para este evento.");
        }

        Optional<Inscripcion> existente = inscripcionRepositorio.buscarPorUsuarioYEvento(usuario, evento);

        if (existente.isPresent()) {
            Inscripcion inscripcion = existente.get();

            if (!inscripcion.isCancelada()) {
                throw new RuntimeException("El usuario ya está inscrito en este evento.");
            }

            inscripcion.setCancelada(false);
            inscripcion.setTokenQr(QRUtil.generarTokenUnico());
            return inscripcionRepositorio.actualizar(inscripcion);
        }

        Inscripcion nuevaInscripcion = new Inscripcion(
                usuario,
                evento,
                QRUtil.generarTokenUnico()
        );

        return inscripcionRepositorio.guardar(nuevaInscripcion);
    }

    public Inscripcion cancelarInscripcion(Usuario usuario, Evento evento) {
        if (usuario == null) {
            throw new RuntimeException("El usuario es obligatorio.");
        }

        if (evento == null) {
            throw new RuntimeException("El evento es obligatorio.");
        }

        Inscripcion inscripcion = inscripcionRepositorio.buscarPorUsuarioYEvento(usuario, evento)
                .orElseThrow(() -> new RuntimeException("La inscripción no existe."));

        if (inscripcion.isCancelada()) {
            throw new RuntimeException("La inscripción ya fue cancelada.");
        }

        if (eventoYaInicio(evento)) {
            throw new RuntimeException("Solo se puede cancelar la inscripción antes del evento.");
        }

        inscripcion.setCancelada(true);
        return inscripcionRepositorio.actualizar(inscripcion);
    }

    public Inscripcion buscarPorTokenQr(String tokenQr) {
        if (tokenQr == null || tokenQr.isBlank()) {
            throw new RuntimeException("El token QR es obligatorio.");
        }

        return inscripcionRepositorio.buscarPorTokenQr(tokenQr)
                .orElseThrow(() -> new RuntimeException("No se encontró una inscripción con ese token QR."));
    }

    public List<Inscripcion> listarPorEvento(Evento evento) {
        return inscripcionRepositorio.listarPorEvento(evento);
    }

    public List<Inscripcion> listarPorUsuario(Usuario usuario) {
        return inscripcionRepositorio.listarPorUsuario(usuario);
    }

    public long contarInscripcionesActivasPorEvento(Evento evento) {
        return inscripcionRepositorio.contarInscripcionesActivasPorEvento(evento);
    }

    public String obtenerContenidoQR(Inscripcion inscripcion) {
        return QRUtil.generarContenidoQR(inscripcion);
    }

    private void validarInscripcion(Usuario usuario, Evento evento) {
        if (usuario == null) {
            throw new RuntimeException("El usuario es obligatorio.");
        }

        if (evento == null) {
            throw new RuntimeException("El evento es obligatorio.");
        }

        if (usuario.isBloqueado()) {
            throw new RuntimeException("El usuario está bloqueado.");
        }

        if (evento.isCancelado()) {
            throw new RuntimeException("No se puede inscribir en un evento cancelado.");
        }

        if (!evento.isPublicado()) {
            throw new RuntimeException("No se puede inscribir en un evento no publicado.");
        }

        if (eventoYaInicio(evento)) {
            throw new RuntimeException("No se puede inscribir en un evento ya iniciado o pasado.");
        }
    }

    private boolean eventoYaInicio(Evento evento) {
        LocalDate hoy = LocalDate.now();

        if (hoy.isAfter(evento.getFecha())) {
            return true;
        }

        if (hoy.isEqual(evento.getFecha())) {
            LocalTime ahora = LocalTime.now();
            return !ahora.isBefore(evento.getHora());
        }

        return false;
    }
}