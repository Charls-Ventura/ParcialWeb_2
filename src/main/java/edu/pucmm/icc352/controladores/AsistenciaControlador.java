package edu.pucmm.icc352.controladores;

import edu.pucmm.icc352.modelos.Evento;
import edu.pucmm.icc352.servicios.AsistenciaServicio;
import edu.pucmm.icc352.servicios.EventoServicio;
import edu.pucmm.icc352.servicios.InscripcionServicio;
import edu.pucmm.icc352.utilidades.SessionUtil;
import io.javalin.http.Context;

public class AsistenciaControlador {

    private final AsistenciaServicio asistenciaServicio = new AsistenciaServicio();
    private final EventoServicio eventoServicio = new EventoServicio();
    private final InscripcionServicio inscripcionServicio = new InscripcionServicio();

    private boolean validarOrganizadorOAdmin(Context ctx) {
        if (!SessionUtil.estaLogueado(ctx)) {
            ctx.redirect("/login");
            return false;
        }

        if (!(SessionUtil.esAdmin(ctx) || SessionUtil.esOrganizador(ctx))) {
            ctx.status(403).html("""
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <title>Acceso denegado</title>
                    </head>
                    <body style="font-family: Arial, sans-serif; padding: 40px;">
                        <h1>Acceso denegado</h1>
                        <p>Solo administradores u organizadores pueden entrar aquí.</p>
                        <a href="/me">Volver</a>
                    </body>
                    </html>
                    """);
            return false;
        }

        return true;
    }

    public void mostrarFormularioEscaneo(Context ctx) {
        if (!validarOrganizadorOAdmin(ctx)) {
            return;
        }

        String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>Registrar asistencia</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            background: #f4f4f4;
                            margin: 0;
                            padding: 40px;
                        }
                        .contenedor {
                            max-width: 520px;
                            margin: auto;
                            background: white;
                            padding: 24px;
                            border-radius: 10px;
                            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        }
                        input {
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
                        <h1>Registrar asistencia por QR</h1>
                        <form method="post" action="/asistencia/registrar">
                            <label>Token QR</label>
                            <input type="text" name="tokenQr" required>

                            <button type="submit">Registrar asistencia</button>
                            <a href="/me" class="secundario">Volver</a>
                        </form>
                    </div>
                </body>
                </html>
                """;

        ctx.html(html);
    }

    public void registrarAsistencia(Context ctx) {
        if (!validarOrganizadorOAdmin(ctx)) {
            return;
        }

        try {
            String tokenQr = ctx.formParam("tokenQr");
            var asistencia = asistenciaServicio.registrarAsistenciaPorTokenQr(tokenQr);

            String html = """
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <title>Asistencia registrada</title>
                        <style>
                            body {
                                font-family: Arial, sans-serif;
                                background: #f4f4f4;
                                margin: 0;
                                padding: 40px;
                            }
                            .contenedor {
                                max-width: 650px;
                                margin: auto;
                                background: white;
                                padding: 24px;
                                border-radius: 10px;
                                box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                            }
                            a {
                                display: inline-block;
                                margin-top: 12px;
                                text-decoration: none;
                                padding: 10px 14px;
                                border-radius: 6px;
                                background: #0d6efd;
                                color: white;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="contenedor">
                            <h1>Asistencia registrada correctamente</h1>
                            <p><strong>Usuario:</strong> %s</p>
                            <p><strong>Evento:</strong> %s</p>
                            <p><strong>Fecha registro:</strong> %s</p>
                            <a href="/asistencia/registrar">Registrar otra asistencia</a>
                        </div>
                    </body>
                    </html>
                    """.formatted(
                    asistencia.getUsuario().getNombre(),
                    asistencia.getEvento().getTitulo(),
                    asistencia.getFechaRegistro()
            );

            ctx.html(html);
        } catch (Exception e) {
            String html = """
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <title>Error</title>
                        <style>
                            body {
                                font-family: Arial, sans-serif;
                                background: #f4f4f4;
                                margin: 0;
                                padding: 40px;
                            }
                            .contenedor {
                                max-width: 650px;
                                margin: auto;
                                background: white;
                                padding: 24px;
                                border-radius: 10px;
                                box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                            }
                            a {
                                display: inline-block;
                                margin-top: 12px;
                                text-decoration: none;
                                padding: 10px 14px;
                                border-radius: 6px;
                                background: #0d6efd;
                                color: white;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="contenedor">
                            <h2>Error al registrar asistencia</h2>
                            <p>%s</p>
                            <a href="/asistencia/registrar">Volver</a>
                        </div>
                    </body>
                    </html>
                    """.formatted(e.getMessage());

            ctx.status(400).html(html);
        }
    }

    public void verResumenEvento(Context ctx) {
        if (!validarOrganizadorOAdmin(ctx)) {
            return;
        }

        try {
            Long eventoId = Long.parseLong(ctx.pathParam("id"));
            Evento evento = eventoServicio.buscarPorId(eventoId);

            long inscritos = inscripcionServicio.contarInscripcionesActivasPorEvento(evento);
            long asistentes = asistenciaServicio.contarAsistenciasPorEvento(evento);

            double porcentaje = 0;
            if (inscritos > 0) {
                porcentaje = (asistentes * 100.0) / inscritos;
            }

            String html = """
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <title>Resumen del evento</title>
                        <style>
                            body {
                                font-family: Arial, sans-serif;
                                background: #f4f4f4;
                                margin: 0;
                                padding: 40px;
                            }
                            .contenedor {
                                max-width: 700px;
                                margin: auto;
                                background: white;
                                padding: 24px;
                                border-radius: 10px;
                                box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                            }
                            a {
                                display: inline-block;
                                margin-top: 12px;
                                text-decoration: none;
                                padding: 10px 14px;
                                border-radius: 6px;
                                background: #0d6efd;
                                color: white;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="contenedor">
                            <h1>Resumen del evento</h1>
                            <p><strong>ID:</strong> %d</p>
                            <p><strong>Título:</strong> %s</p>
                            <p><strong>Fecha:</strong> %s</p>
                            <p><strong>Hora:</strong> %s</p>
                            <hr>
                            <p><strong>Total inscritos activos:</strong> %d</p>
                            <p><strong>Total asistentes:</strong> %d</p>
                            <p><strong>Porcentaje de asistencia:</strong> %.2f%%</p>

                            <a href="/organizador/eventos">Volver a eventos</a>
                        </div>
                    </body>
                    </html>
                    """.formatted(
                    evento.getId(),
                    evento.getTitulo(),
                    evento.getFecha(),
                    evento.getHora(),
                    inscritos,
                    asistentes,
                    porcentaje
            );

            ctx.html(html);
        } catch (Exception e) {
            String html = """
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <title>Error</title>
                        <style>
                            body {
                                font-family: Arial, sans-serif;
                                background: #f4f4f4;
                                margin: 0;
                                padding: 40px;
                            }
                            .contenedor {
                                max-width: 650px;
                                margin: auto;
                                background: white;
                                padding: 24px;
                                border-radius: 10px;
                                box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                            }
                            a {
                                display: inline-block;
                                margin-top: 12px;
                                text-decoration: none;
                                padding: 10px 14px;
                                border-radius: 6px;
                                background: #0d6efd;
                                color: white;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="contenedor">
                            <h2>Error al mostrar resumen</h2>
                            <p>%s</p>
                            <a href="/organizador/eventos">Volver</a>
                        </div>
                    </body>
                    </html>
                    """.formatted(e.getMessage());

            ctx.status(400).html(html);
        }
    }
}