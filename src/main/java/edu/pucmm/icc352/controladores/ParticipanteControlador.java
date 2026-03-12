package edu.pucmm.icc352.controladores;

import edu.pucmm.icc352.modelos.Evento;
import edu.pucmm.icc352.modelos.Inscripcion;
import edu.pucmm.icc352.modelos.Usuario;
import edu.pucmm.icc352.servicios.EventoServicio;
import edu.pucmm.icc352.servicios.InscripcionServicio;
import edu.pucmm.icc352.servicios.UsuarioServicio;
import edu.pucmm.icc352.utilidades.SessionUtil;
import io.javalin.http.Context;

import java.util.List;

public class ParticipanteControlador {

    private final UsuarioServicio usuarioServicio = new UsuarioServicio();
    private final EventoServicio eventoServicio = new EventoServicio();
    private final InscripcionServicio inscripcionServicio = new InscripcionServicio();

    private boolean validarParticipante(Context ctx) {
        if (!SessionUtil.estaLogueado(ctx)) {
            ctx.redirect("/login");
            return false;
        }

        boolean permitido = SessionUtil.esParticipante(ctx) || SessionUtil.esAdmin(ctx);

        if (!permitido) {
            ctx.status(403).html("""
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <title>Acceso denegado</title>
                    </head>
                    <body style="font-family: Arial, sans-serif; padding: 40px;">
                        <h1>Acceso denegado</h1>
                        <p>Solo participantes o administradores pueden entrar aquí.</p>
                        <a href="/me">Volver</a>
                    </body>
                    </html>
                    """);
            return false;
        }

        return true;
    }

    private Usuario obtenerUsuarioSesion(Context ctx) {
        Long usuarioId = SessionUtil.obtenerUsuarioId(ctx);
        return usuarioServicio.buscarPorId(usuarioId);
    }

    public void dashboard(Context ctx) {
        if (!validarParticipante(ctx)) {
            return;
        }

        String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>Dashboard Participante</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            background: #f4f4f4;
                            margin: 0;
                            padding: 40px;
                        }
                        .contenedor {
                            max-width: 800px;
                            margin: auto;
                            background: white;
                            padding: 24px;
                            border-radius: 10px;
                            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        }
                        a {
                            display: inline-block;
                            margin-top: 12px;
                            margin-right: 10px;
                            text-decoration: none;
                            padding: 10px 14px;
                            border-radius: 6px;
                            background: #0d6efd;
                            color: white;
                        }
                        .secundario {
                            background: #6c757d;
                        }
                    </style>
                </head>
                <body>
                    <div class="contenedor">
                        <h1>Dashboard del Participante</h1>
                        <a href="/participante/eventos">Ver eventos publicados</a>
                        <a href="/participante/mis-inscripciones">Mis inscripciones</a>
                        <a href="/me" class="secundario">Ver mi sesión</a>
                        <a href="/logout" class="secundario">Cerrar sesión</a>
                    </div>
                </body>
                </html>
                """;

        ctx.html(html);
    }

    public void listarEventosPublicados(Context ctx) {
        if (!validarParticipante(ctx)) {
            return;
        }

        List<Evento> eventos = eventoServicio.listarPublicadosNoCancelados();

        StringBuilder filas = new StringBuilder();

        for (Evento evento : eventos) {
            filas.append("""
                    <tr>
                        <td>%d</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%d</td>
                        <td><a href='/participante/eventos/inscribirse/%d'>Inscribirme</a></td>
                    </tr>
                    """.formatted(
                    evento.getId(),
                    evento.getTitulo(),
                    evento.getFecha(),
                    evento.getHora(),
                    evento.getUbicacion(),
                    evento.getCupoMaximo(),
                    evento.getId()
            ));
        }

        String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>Eventos publicados</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            background: #f4f4f4;
                            margin: 0;
                            padding: 40px;
                        }
                        .contenedor {
                            max-width: 1100px;
                            margin: auto;
                            background: white;
                            padding: 24px;
                            border-radius: 10px;
                            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        }
                        table {
                            width: 100%%;
                            border-collapse: collapse;
                            margin-top: 20px;
                        }
                        th, td {
                            border: 1px solid #ddd;
                            padding: 10px;
                            text-align: left;
                        }
                        th {
                            background: #f1f1f1;
                        }
                        a {
                            display: inline-block;
                            text-decoration: none;
                            padding: 8px 12px;
                            border-radius: 6px;
                            background: #0d6efd;
                            color: white;
                        }
                        .volver {
                            margin-top: 20px;
                            background: #6c757d;
                        }
                    </style>
                </head>
                <body>
                    <div class="contenedor">
                        <h1>Eventos publicados</h1>

                        <table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Título</th>
                                    <th>Fecha</th>
                                    <th>Hora</th>
                                    <th>Ubicación</th>
                                    <th>Cupo</th>
                                    <th>Acción</th>
                                </tr>
                            </thead>
                            <tbody>
                                %s
                            </tbody>
                        </table>

                        <a href="/participante/dashboard" class="volver">Volver</a>
                    </div>
                </body>
                </html>
                """.formatted(filas);

        ctx.html(html);
    }

    public void inscribirseEvento(Context ctx) {
        if (!validarParticipante(ctx)) {
            return;
        }

        try {
            Usuario usuario = obtenerUsuarioSesion(ctx);
            Long eventoId = Long.parseLong(ctx.pathParam("id"));
            Evento evento = eventoServicio.buscarPorId(eventoId);

            inscripcionServicio.inscribirUsuario(usuario, evento);
            ctx.redirect("/participante/mis-inscripciones");
        } catch (Exception e) {
            ctx.status(400).html("""
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <title>Error</title>
                    </head>
                    <body style="font-family: Arial, sans-serif; padding: 40px;">
                        <h2>Error al inscribirse</h2>
                        <p>%s</p>
                        <a href="/participante/eventos">Volver</a>
                    </body>
                    </html>
                    """.formatted(e.getMessage()));
        }
    }

    public void listarMisInscripciones(Context ctx) {
        if (!validarParticipante(ctx)) {
            return;
        }

        Usuario usuario = obtenerUsuarioSesion(ctx);
        List<Inscripcion> inscripciones = inscripcionServicio.listarPorUsuario(usuario);

        StringBuilder filas = new StringBuilder();

        for (Inscripcion inscripcion : inscripciones) {
            Evento evento = inscripcion.getEvento();

            String accion = inscripcion.isCancelada()
                    ? "Cancelada"
                    : "<a href='/participante/inscripciones/cancelar/" + evento.getId() + "'>Cancelar inscripción</a>";

            String qr = inscripcion.isCancelada()
                    ? "No disponible"
                    : inscripcionServicio.obtenerContenidoQR(inscripcion);

            filas.append("""
                    <tr>
                        <td>%d</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td style='max-width: 260px; word-break: break-word;'>%s</td>
                        <td>%s</td>
                    </tr>
                    """.formatted(
                    evento.getId(),
                    evento.getTitulo(),
                    evento.getFecha(),
                    evento.getHora(),
                    evento.getUbicacion(),
                    inscripcion.isCancelada() ? "Sí" : "No",
                    qr,
                    accion
            ));
        }

        String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>Mis inscripciones</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            background: #f4f4f4;
                            margin: 0;
                            padding: 40px;
                        }
                        .contenedor {
                            max-width: 1200px;
                            margin: auto;
                            background: white;
                            padding: 24px;
                            border-radius: 10px;
                            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        }
                        table {
                            width: 100%%;
                            border-collapse: collapse;
                            margin-top: 20px;
                        }
                        th, td {
                            border: 1px solid #ddd;
                            padding: 10px;
                            text-align: left;
                            vertical-align: top;
                        }
                        th {
                            background: #f1f1f1;
                        }
                        a {
                            display: inline-block;
                            text-decoration: none;
                            padding: 8px 12px;
                            border-radius: 6px;
                            background: #0d6efd;
                            color: white;
                        }
                        .volver {
                            margin-top: 20px;
                            background: #6c757d;
                        }
                    </style>
                </head>
                <body>
                    <div class="contenedor">
                        <h1>Mis inscripciones</h1>

                        <table>
                            <thead>
                                <tr>
                                    <th>ID Evento</th>
                                    <th>Título</th>
                                    <th>Fecha</th>
                                    <th>Hora</th>
                                    <th>Ubicación</th>
                                    <th>Cancelada</th>
                                    <th>Contenido QR</th>
                                    <th>Acción</th>
                                </tr>
                            </thead>
                            <tbody>
                                %s
                            </tbody>
                        </table>

                        <a href="/participante/dashboard" class="volver">Volver</a>
                    </div>
                </body>
                </html>
                """.formatted(filas);

        ctx.html(html);
    }

    public void cancelarInscripcion(Context ctx) {
        if (!validarParticipante(ctx)) {
            return;
        }

        try {
            Usuario usuario = obtenerUsuarioSesion(ctx);
            Long eventoId = Long.parseLong(ctx.pathParam("id"));
            Evento evento = eventoServicio.buscarPorId(eventoId);

            inscripcionServicio.cancelarInscripcion(usuario, evento);
            ctx.redirect("/participante/mis-inscripciones");
        } catch (Exception e) {
            ctx.status(400).html("""
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <title>Error</title>
                    </head>
                    <body style="font-family: Arial, sans-serif; padding: 40px;">
                        <h2>Error al cancelar inscripción</h2>
                        <p>%s</p>
                        <a href="/participante/mis-inscripciones">Volver</a>
                    </body>
                    </html>
                    """.formatted(e.getMessage()));
        }
    }
}