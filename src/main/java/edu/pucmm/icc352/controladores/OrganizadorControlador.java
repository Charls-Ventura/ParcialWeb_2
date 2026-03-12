package edu.pucmm.icc352.controladores;

import edu.pucmm.icc352.modelos.Evento;
import edu.pucmm.icc352.modelos.Usuario;
import edu.pucmm.icc352.servicios.EventoServicio;
import edu.pucmm.icc352.servicios.UsuarioServicio;
import edu.pucmm.icc352.utilidades.SessionUtil;
import io.javalin.http.Context;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class OrganizadorControlador {

    private final UsuarioServicio usuarioServicio = new UsuarioServicio();
    private final EventoServicio eventoServicio = new EventoServicio();

    private boolean validarOrganizador(Context ctx) {
        if (!SessionUtil.estaLogueado(ctx)) {
            ctx.redirect("/login");
            return false;
        }

        boolean permitido = SessionUtil.esOrganizador(ctx) || SessionUtil.esAdmin(ctx);

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
                        <p>Solo organizadores o administradores pueden entrar aquí.</p>
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

    private Evento buscarEventoDelOrganizador(Context ctx) {
        Long idEvento = Long.parseLong(ctx.pathParam("id"));
        Evento evento = eventoServicio.buscarPorId(idEvento);
        Usuario usuario = obtenerUsuarioSesion(ctx);

        if (evento.getOrganizador() == null || !evento.getOrganizador().getId().equals(usuario.getId())) {
            throw new RuntimeException("No puedes gestionar un evento que no te pertenece.");
        }

        return evento;
    }

    public void dashboard(Context ctx) {
        if (!validarOrganizador(ctx)) {
            return;
        }

        String html = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <title>Dashboard Organizador</title>
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
                    <h1>Dashboard del Organizador</h1>
                    <a href="/organizador/eventos">Mis eventos</a>
                    <a href="/organizador/eventos/nuevo">Crear evento</a>
                    <a href="/asistencia/registrar">Registrar asistencia</a>
                    <a href="/me" class="secundario">Ver mi sesión</a>
                    <a href="/logout" class="secundario">Cerrar sesión</a>
                </div>
            </body>
            </html>
            """;

        ctx.html(html);
    }

    public void listarMisEventos(Context ctx) {
        if (!validarOrganizador(ctx)) {
            return;
        }

        Usuario usuario = obtenerUsuarioSesion(ctx);

        List<Evento> eventos = eventoServicio.listarTodos()
                .stream()
                .filter(e -> e.getOrganizador() != null && e.getOrganizador().getId().equals(usuario.getId()))
                .toList();

        StringBuilder filas = new StringBuilder();

        for (Evento evento : eventos) {
            String acciones = """
                    <a href='/organizador/eventos/editar/%d'>Editar</a>
                    <a href='/organizador/eventos/publicar/%d'>Publicar</a>
                    <a href='/organizador/eventos/despublicar/%d'>Despublicar</a>
                    <a href='/organizador/eventos/cancelar/%d'>Cancelar</a>
                    <a href='/eventos/resumen/%d'>Resumen</a>
                    """.formatted(
                    evento.getId(),
                    evento.getId(),
                    evento.getId(),
                    evento.getId(),
                    evento.getId()
            );

            filas.append("""
                    <tr>
                        <td>%d</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                    </tr>
                    """.formatted(
                    evento.getId(),
                    evento.getTitulo(),
                    evento.getFecha(),
                    evento.getHora(),
                    evento.isPublicado() ? "Sí" : "No",
                    evento.isCancelado() ? "Sí" : "No",
                    acciones
            ));
        }

        String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>Mis eventos</title>
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
                            margin-right: 6px;
                            margin-bottom: 6px;
                        }
                        .volver {
                            margin-top: 20px;
                            background: #6c757d;
                        }
                    </style>
                </head>
                <body>
                    <div class="contenedor">
                        <h1>Mis eventos</h1>
                        <a href="/organizador/eventos/nuevo">Crear evento</a>

                        <table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Título</th>
                                    <th>Fecha</th>
                                    <th>Hora</th>
                                    <th>Publicado</th>
                                    <th>Cancelado</th>
                                    <th>Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                %s
                            </tbody>
                        </table>

                        <a href="/organizador/dashboard" class="volver">Volver</a>
                    </div>
                </body>
                </html>
                """.formatted(filas);

        ctx.html(html);
    }

    public void mostrarFormularioNuevoEvento(Context ctx) {
        if (!validarOrganizador(ctx)) {
            return;
        }

        String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>Crear evento</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            background: #f4f4f4;
                            margin: 0;
                            padding: 40px;
                        }
                        .contenedor {
                            max-width: 600px;
                            margin: auto;
                            background: white;
                            padding: 24px;
                            border-radius: 10px;
                            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        }
                        input, textarea {
                            width: 100%;
                            padding: 10px;
                            margin-top: 8px;
                            margin-bottom: 16px;
                            box-sizing: border-box;
                        }
                        button, a {
                            display: inline-block;
                            text-decoration: none;
                            padding: 10px 14px;
                            border-radius: 6px;
                            border: none;
                            background: #0d6efd;
                            color: white;
                            cursor: pointer;
                        }
                        .secundario {
                            background: #6c757d;
                        }
                    </style>
                </head>
                <body>
                    <div class="contenedor">
                        <h1>Crear evento</h1>
                        <form method="post" action="/organizador/eventos/nuevo">
                            <label>Título</label>
                            <input type="text" name="titulo" required>

                            <label>Descripción</label>
                            <textarea name="descripcion" required></textarea>

                            <label>Fecha</label>
                            <input type="date" name="fecha" required>

                            <label>Hora</label>
                            <input type="time" name="hora" required>

                            <label>Ubicación</label>
                            <input type="text" name="ubicacion" required>

                            <label>Cupo máximo</label>
                            <input type="number" name="cupoMaximo" min="1" required>

                            <button type="submit">Guardar evento</button>
                            <a href="/organizador/eventos" class="secundario">Volver</a>
                        </form>
                    </div>
                </body>
                </html>
                """;

        ctx.html(html);
    }

    public void guardarNuevoEvento(Context ctx) {
        if (!validarOrganizador(ctx)) {
            return;
        }

        try {
            Usuario organizador = obtenerUsuarioSesion(ctx);

            String titulo = ctx.formParam("titulo");
            String descripcion = ctx.formParam("descripcion");
            String fechaTexto = ctx.formParam("fecha");
            String horaTexto = ctx.formParam("hora");
            String ubicacion = ctx.formParam("ubicacion");
            String cupoTexto = ctx.formParam("cupoMaximo");

            LocalDate fecha = LocalDate.parse(fechaTexto);
            LocalTime hora = LocalTime.parse(horaTexto);
            int cupoMaximo = Integer.parseInt(cupoTexto);

            eventoServicio.crearEvento(
                    titulo,
                    descripcion,
                    fecha,
                    hora,
                    ubicacion,
                    cupoMaximo,
                    organizador
            );

            ctx.redirect("/organizador/eventos");
        } catch (Exception e) {
            ctx.status(400).html("""
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <title>Error</title>
                    </head>
                    <body style="font-family: Arial, sans-serif; padding: 40px;">
                        <h2>Error al crear evento</h2>
                        <p>%s</p>
                        <a href="/organizador/eventos/nuevo">Volver</a>
                    </body>
                    </html>
                    """.formatted(e.getMessage()));
        }
    }

    public void mostrarFormularioEditarEvento(Context ctx) {
        if (!validarOrganizador(ctx)) {
            return;
        }

        try {
            Evento evento = buscarEventoDelOrganizador(ctx);

            String html = """
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <title>Editar evento</title>
                        <style>
                            body {
                                font-family: Arial, sans-serif;
                                background: #f4f4f4;
                                margin: 0;
                                padding: 40px;
                            }
                            .contenedor {
                                max-width: 600px;
                                margin: auto;
                                background: white;
                                padding: 24px;
                                border-radius: 10px;
                                box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                            }
                            input, textarea {
                                width: 100%%;
                                padding: 10px;
                                margin-top: 8px;
                                margin-bottom: 16px;
                                box-sizing: border-box;
                            }
                            button, a {
                                display: inline-block;
                                text-decoration: none;
                                padding: 10px 14px;
                                border-radius: 6px;
                                border: none;
                                background: #0d6efd;
                                color: white;
                                cursor: pointer;
                            }
                            .secundario {
                                background: #6c757d;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="contenedor">
                            <h1>Editar evento</h1>
                            <form method="post" action="/organizador/eventos/editar/%d">
                                <label>Título</label>
                                <input type="text" name="titulo" value="%s" required>

                                <label>Descripción</label>
                                <textarea name="descripcion" required>%s</textarea>

                                <label>Fecha</label>
                                <input type="date" name="fecha" value="%s" required>

                                <label>Hora</label>
                                <input type="time" name="hora" value="%s" required>

                                <label>Ubicación</label>
                                <input type="text" name="ubicacion" value="%s" required>

                                <label>Cupo máximo</label>
                                <input type="number" name="cupoMaximo" min="1" value="%d" required>

                                <button type="submit">Guardar cambios</button>
                                <a href="/organizador/eventos" class="secundario">Volver</a>
                            </form>
                        </div>
                    </body>
                    </html>
                    """.formatted(
                    evento.getId(),
                    evento.getTitulo(),
                    evento.getDescripcion(),
                    evento.getFecha(),
                    evento.getHora(),
                    evento.getUbicacion(),
                    evento.getCupoMaximo()
            );

            ctx.html(html);
        } catch (Exception e) {
            ctx.status(400).result(e.getMessage());
        }
    }

    public void guardarEdicionEvento(Context ctx) {
        if (!validarOrganizador(ctx)) {
            return;
        }

        try {
            Evento evento = buscarEventoDelOrganizador(ctx);

            String titulo = ctx.formParam("titulo");
            String descripcion = ctx.formParam("descripcion");
            String fechaTexto = ctx.formParam("fecha");
            String horaTexto = ctx.formParam("hora");
            String ubicacion = ctx.formParam("ubicacion");
            String cupoTexto = ctx.formParam("cupoMaximo");

            LocalDate fecha = LocalDate.parse(fechaTexto);
            LocalTime hora = LocalTime.parse(horaTexto);
            int cupoMaximo = Integer.parseInt(cupoTexto);

            eventoServicio.editarEvento(
                    evento.getId(),
                    titulo,
                    descripcion,
                    fecha,
                    hora,
                    ubicacion,
                    cupoMaximo
            );

            ctx.redirect("/organizador/eventos");
        } catch (Exception e) {
            ctx.status(400).html("""
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <title>Error</title>
                    </head>
                    <body style="font-family: Arial, sans-serif; padding: 40px;">
                        <h2>Error al editar evento</h2>
                        <p>%s</p>
                        <a href="/organizador/eventos">Volver</a>
                    </body>
                    </html>
                    """.formatted(e.getMessage()));
        }
    }

    public void publicarEvento(Context ctx) {
        if (!validarOrganizador(ctx)) {
            return;
        }

        try {
            Evento evento = buscarEventoDelOrganizador(ctx);
            eventoServicio.publicarEvento(evento.getId());
            ctx.redirect("/organizador/eventos");
        } catch (Exception e) {
            ctx.status(400).result(e.getMessage());
        }
    }

    public void despublicarEvento(Context ctx) {
        if (!validarOrganizador(ctx)) {
            return;
        }

        try {
            Evento evento = buscarEventoDelOrganizador(ctx);
            eventoServicio.despublicarEvento(evento.getId());
            ctx.redirect("/organizador/eventos");
        } catch (Exception e) {
            ctx.status(400).result(e.getMessage());
        }
    }

    public void cancelarEvento(Context ctx) {
        if (!validarOrganizador(ctx)) {
            return;
        }

        try {
            Evento evento = buscarEventoDelOrganizador(ctx);
            eventoServicio.cancelarEvento(evento.getId());
            ctx.redirect("/organizador/eventos");
        } catch (Exception e) {
            ctx.status(400).result(e.getMessage());
        }
    }
}