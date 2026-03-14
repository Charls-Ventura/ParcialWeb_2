package edu.pucmm.icc352.servicios;

import edu.pucmm.icc352.modelos.Asistencia;
import edu.pucmm.icc352.modelos.Evento;
import edu.pucmm.icc352.modelos.Inscripcion;
import edu.pucmm.icc352.modelos.Usuario;
import edu.pucmm.icc352.repositorios.AsistenciaRepositorio;
import edu.pucmm.icc352.repositorios.InscripcionRepositorio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AsistenciaServicio {

    private final AsistenciaRepositorio asistenciaRepositorio = new AsistenciaRepositorio();
    private final InscripcionRepositorio inscripcionRepositorio = new InscripcionRepositorio();

    public Asistencia registrarAsistenciaPorTokenQr(String tokenQr) {

        if (tokenQr == null || tokenQr.isBlank()) {
            throw new RuntimeException("El token QR es obligatorio.");
        }

        Inscripcion inscripcion = inscripcionRepositorio.buscarPorTokenQr(tokenQr)
                .orElseThrow(() -> new RuntimeException("QR inválido o no encontrado."));

        if (inscripcion.isCancelada()) {
            throw new RuntimeException("La inscripción está cancelada.");
        }

        Usuario usuario = inscripcion.getUsuario();
        Evento evento = inscripcion.getEvento();

        if (evento.isCancelado()) {
            throw new RuntimeException("El evento está cancelado.");
        }

        LocalDate hoy = LocalDate.now();

        // no permitir si el evento ya pasó
        if (evento.getFecha().isBefore(hoy)) {
            throw new RuntimeException("El evento ya ocurrió. No se puede registrar asistencia.");
        }

        // evitar registrar asistencia dos veces
        if (asistenciaRepositorio.buscarPorUsuarioYEvento(usuario, evento).isPresent()) {
            throw new RuntimeException("La asistencia ya fue registrada para este usuario.");
        }

        Asistencia asistencia = new Asistencia(usuario, evento, LocalDateTime.now());
        return asistenciaRepositorio.guardar(asistencia);
    }

    public boolean yaAsistio(Usuario usuario, Evento evento) {
        return asistenciaRepositorio.buscarPorUsuarioYEvento(usuario, evento).isPresent();
    }

    public List<Asistencia> listarPorUsuario(Usuario usuario) {
        return asistenciaRepositorio.listarPorUsuario(usuario);
    }



    public List<Asistencia> listarPorEvento(Evento evento) {
        return asistenciaRepositorio.listarPorEvento(evento);
    }

    public long contarAsistenciasPorEvento(Evento evento) {
        return asistenciaRepositorio.contarPorEvento(evento);
    }
}