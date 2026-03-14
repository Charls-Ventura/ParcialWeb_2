package edu.pucmm.icc352.servicios;

import edu.pucmm.icc352.modelos.Evento;
import edu.pucmm.icc352.modelos.Rol;
import edu.pucmm.icc352.modelos.Usuario;
import edu.pucmm.icc352.repositorios.EventoRepositorio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class EventoServicio {

    private final EventoRepositorio eventoRepositorio = new EventoRepositorio();

    public Evento crearEvento(String titulo,
                              String descripcion,
                              LocalDate fecha,
                              LocalTime hora,
                              String ubicacion,
                              int cupoMaximo,
                              Usuario organizador) {

        validarDatosEvento(titulo, descripcion, fecha, hora, ubicacion, cupoMaximo, organizador);
        validarOrganizador(organizador);
        validarFechaEvento(fecha, hora);

        Evento evento = new Evento(
                titulo.trim(),
                descripcion.trim(),
                fecha,
                hora,
                ubicacion.trim(),
                cupoMaximo,
                organizador
        );

        return eventoRepositorio.guardar(evento);
    }

    public Evento editarEvento(Long id,
                               String titulo,
                               String descripcion,
                               LocalDate fecha,
                               LocalTime hora,
                               String ubicacion,
                               int cupoMaximo) {

        Evento evento = buscarPorId(id);

        if (evento.isCancelado()) {
            throw new RuntimeException("No se puede editar un evento cancelado.");
        }

        if (titulo == null || titulo.isBlank()) {
            throw new RuntimeException("El título es obligatorio.");
        }

        if (descripcion == null || descripcion.isBlank()) {
            throw new RuntimeException("La descripción es obligatoria.");
        }

        if (fecha == null) {
            throw new RuntimeException("La fecha es obligatoria.");
        }

        if (hora == null) {
            throw new RuntimeException("La hora es obligatoria.");
        }

        validarFechaEvento(fecha, hora);

        if (ubicacion == null || ubicacion.isBlank()) {
            throw new RuntimeException("La ubicación es obligatoria.");
        }

        if (cupoMaximo <= 0) {
            throw new RuntimeException("El cupo máximo debe ser mayor que cero.");
        }

        evento.setTitulo(titulo.trim());
        evento.setDescripcion(descripcion.trim());
        evento.setFecha(fecha);
        evento.setHora(hora);
        evento.setUbicacion(ubicacion.trim());
        evento.setCupoMaximo(cupoMaximo);

        return eventoRepositorio.actualizar(evento);
    }

    public Evento publicarEvento(Long id) {
        Evento evento = buscarPorId(id);

        if (evento.isCancelado()) {
            throw new RuntimeException("No se puede publicar un evento cancelado.");
        }

        evento.setPublicado(true);
        return eventoRepositorio.actualizar(evento);
    }

    public Evento despublicarEvento(Long id) {
        Evento evento = buscarPorId(id);
        evento.setPublicado(false);
        return eventoRepositorio.actualizar(evento);
    }

    public Evento cancelarEvento(Long id) {
        Evento evento = buscarPorId(id);
        evento.setCancelado(true);
        evento.setPublicado(false);
        return eventoRepositorio.actualizar(evento);
    }

    public void eliminarEvento(Long id) {
        Evento evento = buscarPorId(id);
        eventoRepositorio.eliminar(evento);
    }

    public Evento buscarPorId(Long id) {
        return eventoRepositorio.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado."));
    }

    public List<Evento> listarTodos() {
        return eventoRepositorio.listarTodos();
    }

    public List<Evento> listarPublicadosNoCancelados() {
        return eventoRepositorio.listarTodos()
                .stream()
                .filter(Evento::isPublicado)
                .filter(e -> !e.isCancelado())
                .toList();
    }

    private void validarDatosEvento(String titulo,
                                    String descripcion,
                                    LocalDate fecha,
                                    LocalTime hora,
                                    String ubicacion,
                                    int cupoMaximo,
                                    Usuario organizador) {

        if (titulo == null || titulo.isBlank()) {
            throw new RuntimeException("El título es obligatorio.");
        }

        if (descripcion == null || descripcion.isBlank()) {
            throw new RuntimeException("La descripción es obligatoria.");
        }

        if (fecha == null) {
            throw new RuntimeException("La fecha es obligatoria.");
        }

        if (hora == null) {
            throw new RuntimeException("La hora es obligatoria.");
        }

        if (ubicacion == null || ubicacion.isBlank()) {
            throw new RuntimeException("La ubicación es obligatoria.");
        }

        if (cupoMaximo <= 0) {
            throw new RuntimeException("El cupo máximo debe ser mayor que cero.");
        }

        if (organizador == null) {
            throw new RuntimeException("El organizador es obligatorio.");
        }
    }

    private void validarFechaEvento(LocalDate fecha, LocalTime hora) {

        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        if (fecha.isBefore(hoy)) {
            throw new RuntimeException("No se puede crear o editar un evento con una fecha pasada.");
        }

        if (fecha.isEqual(hoy)) {
            throw new RuntimeException("No se permiten eventos para el mismo día. Debe seleccionar una fecha futura.");
        }

        if (fecha.isEqual(hoy) && hora.isBefore(ahora)) {
            throw new RuntimeException("La hora del evento no puede ser anterior a la hora actual.");
        }
    }

    private void validarOrganizador(Usuario organizador) {
        if (organizador.getRol() != Rol.ORGANIZADOR && organizador.getRol() != Rol.ADMIN) {
            throw new RuntimeException("Solo un organizador o admin puede crear eventos.");
        }

        if (organizador.isBloqueado()) {
            throw new RuntimeException("El organizador está bloqueado.");
        }
    }

    public boolean eventoEnCurso(Evento evento) {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        return evento.getFecha().isEqual(hoy) && !ahora.isBefore(evento.getHora());
    }

    public String obtenerEstadoTiempo(Evento evento) {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        if (evento.getFecha().isBefore(hoy)) {
            return "FINALIZADO";
        }

        if (evento.getFecha().isEqual(hoy)) {
            if (ahora.isBefore(evento.getHora())) {
                return "PROGRAMADO";
            }
            return "EN_CURSO";
        }

        return "PROGRAMADO";
    }


    public boolean eventoYaPaso(Evento evento) {
        return "FINALIZADO".equals(obtenerEstadoTiempo(evento));
    }
}