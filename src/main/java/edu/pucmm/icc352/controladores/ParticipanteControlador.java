package edu.pucmm.icc352.controladores;

import edu.pucmm.icc352.modelos.Evento;
import edu.pucmm.icc352.modelos.Inscripcion;
import edu.pucmm.icc352.modelos.Usuario;
import edu.pucmm.icc352.servicios.EventoServicio;
import edu.pucmm.icc352.servicios.InscripcionServicio;
import edu.pucmm.icc352.servicios.UsuarioServicio;
import edu.pucmm.icc352.utilidades.SessionUtil;
import edu.pucmm.icc352.utilidades.LayoutUtil;
import edu.pucmm.icc352.modelos.Asistencia;
import edu.pucmm.icc352.servicios.AsistenciaServicio;
import io.javalin.http.Context;

import java.util.List;

public class ParticipanteControlador {

    private final UsuarioServicio usuarioServicio = new UsuarioServicio();
    private final EventoServicio eventoServicio = new EventoServicio();
    private final InscripcionServicio inscripcionServicio = new InscripcionServicio();
    private final AsistenciaServicio asistenciaServicio = new AsistenciaServicio();

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

        Usuario usuario = obtenerUsuarioSesion(ctx);

        List<Inscripcion> misInscripciones = inscripcionServicio.listarPorUsuario(usuario);
        List<Inscripcion> inscripcionesActivas = misInscripciones.stream()
                .filter(i -> !i.isCancelada())
                .toList();

        List<Asistencia> misAsistencias = asistenciaServicio.listarPorUsuario(usuario);

        StringBuilder eventosInscritos = new StringBuilder();
        if (inscripcionesActivas.isEmpty()) {
            eventosInscritos.append("<p style='color:#6b7280;'>No tienes inscripciones activas en este momento.</p>");
        } else {
            eventosInscritos.append("<ul class='lista-eventos'>");
            for (Inscripcion inscripcion : inscripcionesActivas) {
                Evento evento = inscripcion.getEvento();
                eventosInscritos.append("""
                    <li>
                        <strong>%s</strong><br>
                        <span>%s · %s · %s</span>
                    </li>
                    """.formatted(
                        escaparHtml(evento.getTitulo()),
                        evento.getFecha(),
                        evento.getHora(),
                        escaparHtml(evento.getUbicacion())
                ));
            }
            eventosInscritos.append("</ul>");
        }

        StringBuilder eventosAsistidos = new StringBuilder();
        if (misAsistencias.isEmpty()) {
            eventosAsistidos.append("<p style='color:#6b7280;'>Aún no tienes asistencias registradas.</p>");
        } else {
            eventosAsistidos.append("<ul class='lista-eventos'>");
            for (Asistencia asistencia : misAsistencias) {
                Evento evento = asistencia.getEvento();
                eventosAsistidos.append("""
                    <li>
                        <strong>%s</strong><br>
                        <span>%s · %s</span>
                    </li>
                    """.formatted(
                        escaparHtml(evento.getTitulo()),
                        evento.getFecha(),
                        asistencia.getFechaRegistro()
                ));
            }
            eventosAsistidos.append("</ul>");
        }

        String contenido = """
            <style>
                .hero {
                    display: grid;
                    grid-template-columns: 1.2fr 0.8fr;
                    gap: 22px;
                    margin-bottom: 24px;
                }

                .hero-principal {
                    background: linear-gradient(135deg, #ffffff, #fffdf7);
                    border: 1px solid #f3e6b2;
                    border-radius: 22px;
                    padding: 28px 26px;
                    box-shadow: 0 12px 26px rgba(0, 0, 0, 0.07);
                }

                .hero-principal h2 {
                    margin: 0 0 14px 0;
                    font-size: 30px;
                    color: #0a3d91;
                }

                .hero-principal p {
                    margin: 0;
                    font-size: 17px;
                    line-height: 1.75;
                    color: #4b5563;
                }

                .hero-secundario {
                    background: linear-gradient(135deg, #0a3d91, #174ea6);
                    color: white;
                    border-radius: 22px;
                    padding: 24px 22px;
                    box-shadow: 0 16px 30px rgba(10, 61, 145, 0.22);
                }

                .hero-secundario h3 {
                    margin-top: 0;
                    margin-bottom: 12px;
                    font-size: 24px;
                }

                .hero-secundario p {
                    margin: 0;
                    font-size: 16px;
                    line-height: 1.6;
                    color: rgba(255,255,255,0.94);
                }

                .grid-paneles {
                    display: grid;
                    grid-template-columns: 1fr 1fr;
                    gap: 22px;
                    margin-bottom: 24px;
                }

                .panel-bloque {
                    background: linear-gradient(180deg, #ffffff, #fffdf7);
                    border: 1px solid #f3e6b2;
                    border-radius: 22px;
                    padding: 24px 22px;
                    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.07);
                }

                .panel-bloque h3 {
                    margin: 0 0 14px 0;
                    font-size: 25px;
                    color: #0a3d91;
                }

                .panel-bloque p {
                    margin: 0 0 14px 0;
                    color: #4b5563;
                    line-height: 1.7;
                    font-size: 15px;
                }

                .lista-eventos {
                    margin: 0;
                    padding-left: 18px;
                    color: #374151;
                }

                .lista-eventos li {
                    margin-bottom: 14px;
                    line-height: 1.6;
                }

                .lista-eventos span {
                    color: #6b7280;
                    font-size: 14px;
                }

                .accesos {
                    background: linear-gradient(180deg, #ffffff, #fffdf7);
                    border: 1px solid #f3e6b2;
                    border-radius: 22px;
                    padding: 24px 22px;
                    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.07);
                }

                .accesos h3 {
                    margin: 0 0 14px 0;
                    font-size: 25px;
                    color: #0a3d91;
                }

                .accesos p {
                    margin: 0 0 18px 0;
                    color: #4b5563;
                    line-height: 1.7;
                    font-size: 15px;
                }

                .acciones-rapidas {
                    display: flex;
                    gap: 12px;
                    flex-wrap: wrap;
                }

                @media (max-width: 1100px) {
                    .hero {
                        grid-template-columns: 1fr;
                    }

                    .grid-paneles {
                        grid-template-columns: 1fr;
                    }
                }

                @media (max-width: 700px) {
                    .acciones-rapidas {
                        flex-direction: column;
                    }

                    .acciones-rapidas .btn {
                        width: 100%%;
                        text-align: center;
                    }

                    .hero-principal h2,
                    .panel-bloque h3,
                    .accesos h3 {
                        font-size: 24px;
                    }
                }
            </style>

            <div class="hero">
                <div class="hero-principal">
                    <h2>Bienvenido al portal del participante</h2>
                    <p>
                        Aquí puedes revisar tus inscripciones activas, verificar los eventos en los que ya tienes asistencia registrada y acceder rápidamente a tus funciones principales dentro del sistema.
                    </p>
                </div>

                <div class="hero-secundario">
                    <h3>Rol activo: participante</h3>
                    <p>
                        Tu panel está enfocado en consultar eventos, mostrar tu QR y revisar tu historial de participación.
                    </p>
                </div>
            </div>

            <div class="grid-paneles">
                <div class="panel-bloque">
                    <h3>Eventos inscritos</h3>
                    <p>Estos son los eventos en los que actualmente mantienes una inscripción activa:</p>
                    %s
                </div>

                <div class="panel-bloque">
                    <h3>Eventos asistidos</h3>
                    <p>Aquí puedes ver los eventos donde ya se registró tu asistencia:</p>
                    %s
                </div>
            </div>

            <div class="accesos">
                <h3>Acceso rápido</h3>
                <p>Utiliza estas opciones para moverte rápidamente dentro de tu cuenta:</p>

                <div class="acciones-rapidas">
                    <a href="/participante/eventos" class="btn btn-principal">Explorar eventos</a>
                    <a href="/participante/mis-inscripciones" class="btn btn-principal">Mis inscripciones</a>
                    <a href="/me" class="btn btn-secundario">Mi sesión</a>
                </div>
            </div>
            """.formatted(
                eventosInscritos.toString(),
                eventosAsistidos.toString()
        );

        ctx.html(LayoutUtil.layoutParticipante(
                "Panel del participante",
                "Consulta tu actividad principal dentro de la plataforma de eventos académicos.",
                contenido
        ));
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
                    <td>
                        <a class="btn btn-principal" href='/participante/eventos/inscribirse/%d'>
                            Inscribirme
                        </a>
                    </td>
                </tr>
                """.formatted(
                    evento.getId(),
                    escaparHtml(evento.getTitulo()),
                    evento.getFecha(),
                    evento.getHora(),
                    escaparHtml(evento.getUbicacion()),
                    evento.getCupoMaximo(),
                    evento.getId()
            ));
        }

        String contenido = """
            <style>
                .resumen-superior {
                    display: grid;
                    grid-template-columns: repeat(3, 1fr);
                    gap: 18px;
                    margin-bottom: 24px;
                }

                .card-resumen {
                    background: linear-gradient(180deg,#f8fbff,#eef5ff);
                    border: 1px solid #dbe7ff;
                    border-radius: 18px;
                    padding: 20px;
                    box-shadow: 0 10px 24px rgba(13, 110, 253, 0.08);
                }

                .card-resumen h3 {
                    margin: 0 0 10px 0;
                    font-size: 18px;
                    color: #0a3d91;
                }

                .card-resumen p {
                    margin: 0;
                    color: #4b5563;
                    font-size: 15px;
                    line-height: 1.6;
                }

                @media (max-width: 1000px) {
                    .resumen-superior {
                        grid-template-columns: 1fr;
                    }
                }
            </style>

            <div class="resumen-superior">
                <div class="card-resumen">
                    <h3>Explora oportunidades</h3>
                    <p>Consulta los eventos actualmente disponibles para participación e inscripción.</p>
                </div>

                <div class="card-resumen">
                    <h3>Información centralizada</h3>
                    <p>Visualiza de forma clara la fecha, hora, lugar y cupo de cada actividad.</p>
                </div>

                <div class="card-resumen">
                    <h3>Inscripción directa</h3>
                    <p>Accede rápidamente al proceso de inscripción desde esta misma pantalla.</p>
                </div>
            </div>

            <div class="panel">
                <div class="panel-top"></div>

                <div class="tabla-wrap">
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
                </div>
            </div>
            """.formatted(filas);

        ctx.html(LayoutUtil.layoutParticipante(
                "Eventos disponibles",
                "Explora los eventos académicos disponibles para inscripción.",
                contenido
        ));
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
            String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>Error de inscripción</title>
                    <style>
                        * {
                            box-sizing: border-box;
                        }

                        body {
                            margin: 0;
                            min-height: 100vh;
                            font-family: Arial, sans-serif;
                            background:
                                radial-gradient(circle at 10%% 10%%, rgba(255, 214, 77, 0.22), transparent 18%%),
                                radial-gradient(circle at 90%% 85%%, rgba(255, 214, 77, 0.16), transparent 20%%),
                                linear-gradient(135deg, #0a3d91, #0d47a1 55%%, #1565c0);
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            padding: 30px 20px;
                            color: #1f2937;
                        }

                        .contenedor {
                            width: 100%%;
                            max-width: 760px;
                            background: #ffffff;
                            border-radius: 24px;
                            box-shadow: 0 24px 50px rgba(0, 0, 0, 0.24);
                            padding: 34px 32px 30px;
                            position: relative;
                            overflow: hidden;
                        }

                        .decoracion-superior {
                            position: absolute;
                            top: 0;
                            left: 0;
                            width: 100%%;
                            height: 8px;
                            background: linear-gradient(90deg, #f4c542, #ffd95a, #f4c542);
                        }

                        .logo-bloque {
                            width: 88px;
                            height: 88px;
                            border-radius: 18px;
                            background: linear-gradient(180deg, #fff8e1, #fffdf5);
                            border: 2px solid #f4d56a;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            margin-bottom: 16px;
                            box-shadow: 0 8px 18px rgba(244, 197, 66, 0.22);
                            color: #0a3d91;
                            font-weight: bold;
                            font-size: 20px;
                        }

                        .logo-bloque img {
                            width: 62px;
                            height: 62px;
                            object-fit: contain;
                        }

                        .titulo {
                            margin: 0;
                            font-size: 36px;
                            color: #991b1b;
                        }

                        .subtitulo {
                            margin-top: 12px;
                            font-size: 17px;
                            color: #4b5563;
                            line-height: 1.6;
                        }

                        .mensaje-error {
                            margin-top: 22px;
                            background: #fef2f2;
                            border-left: 5px solid #ef4444;
                            color: #991b1b;
                            border-radius: 14px;
                            padding: 16px;
                            font-size: 15px;
                            line-height: 1.6;
                        }

                        .acciones {
                            margin-top: 24px;
                            display: flex;
                            gap: 12px;
                            flex-wrap: wrap;
                        }

                        .btn {
                            display: inline-block;
                            text-decoration: none;
                            padding: 13px 18px;
                            border-radius: 10px;
                            font-weight: bold;
                            font-size: 15px;
                            transition: transform 0.15s ease, opacity 0.2s ease;
                        }

                        .btn:hover {
                            transform: translateY(-1px);
                            opacity: 0.96;
                        }

                        .btn-principal {
                            background: linear-gradient(90deg, #0a3d91, #174ea6);
                            color: white;
                        }

                        .btn-secundario {
                            background: #6c757d;
                            color: white;
                        }
                    </style>
                </head>
                <body>
                    <div class="contenedor">
                        <div class="decoracion-superior"></div>

                        <div class="logo-bloque">
                            <img src="/img/logo-pucmm.png" alt="Logo PUCMM"
                                 onerror="this.style.display='none'; this.parentElement.innerHTML='PUCMM';">
                        </div>

                        <h1 class="titulo">No fue posible completar la inscripción</h1>

                        <p class="subtitulo">
                            La solicitud no pudo procesarse correctamente. Revisa el motivo mostrado abajo y vuelve a intentarlo si corresponde.
                        </p>

                        <div class="mensaje-error">
                            %s
                        </div>

                        <div class="acciones">
                            <a href="/participante/eventos" class="btn btn-principal">Volver a eventos</a>
                            <a href="/participante/dashboard" class="btn btn-secundario">Ir al panel</a>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(e.getMessage());

            ctx.status(400).html(html);
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

            String badge = inscripcion.isCancelada()
                    ? "<span class='badge badge-cancelada'>Cancelada</span>"
                    : "<span class='badge badge-activa'>Activa</span>";

            String accion = inscripcion.isCancelada()
                    ? "<span class='btn-deshabilitado'>Cancelada</span>"
                    : "<a class='btn-cancelar' href='/participante/inscripciones/cancelar/" + evento.getId() + "'>Cancelar</a>";

            String botonQr;
            if (inscripcion.isCancelada()) {
                botonQr = "<span class='btn-deshabilitado'>No disponible</span>";
            } else {
                String qrBase64 = edu.pucmm.icc352.utilidades.QRUtil.generarQrBase64(inscripcion);
                String contenidoQr = inscripcionServicio.obtenerContenidoQR(inscripcion);

                botonQr = """
                    <button type="button"
                            class="btn-ver-qr"
                            data-titulo="%s"
                            data-qr="data:image/png;base64,%s"
                            data-contenido="%s"
                            onclick="abrirModalQr(this)">
                        Ver QR
                    </button>
                    """.formatted(
                        escaparHtml(evento.getTitulo()),
                        qrBase64,
                        escaparHtml(contenidoQr)
                );
            }

            filas.append("""
                <tr>
                    <td>%d</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                </tr>
                """.formatted(
                    evento.getId(),
                    escaparHtml(evento.getTitulo()),
                    evento.getFecha(),
                    evento.getHora(),
                    escaparHtml(evento.getUbicacion()),
                    badge,
                    botonQr,
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
                    *{
                        box-sizing:border-box;
                    }

                    body{
                        margin:0;
                        min-height:100vh;
                        font-family:Arial, sans-serif;
                        background:
                            radial-gradient(circle at 10%% 10%%, rgba(255,214,77,0.22), transparent 18%%),
                            radial-gradient(circle at 90%% 85%%, rgba(255,214,77,0.16), transparent 20%%),
                            linear-gradient(135deg,#0a3d91,#0d47a1 55%%,#1565c0);
                        padding:34px 20px 40px;
                    }

                    .contenedor{
                        max-width:1450px;
                        margin:auto;
                        background:white;
                        border-radius:24px;
                        padding:34px 32px 30px;
                        box-shadow:0 24px 50px rgba(0,0,0,0.24);
                        position:relative;
                        overflow:hidden;
                    }

                    .decoracion-superior{
                        position:absolute;
                        top:0;
                        left:0;
                        width:100%%;
                        height:8px;
                        background:linear-gradient(90deg,#f4c542,#ffd95a,#f4c542);
                    }

                    .decoracion-circulo-1{
                        position:absolute;
                        width:95px;
                        height:95px;
                        border-radius:50%%;
                        background:rgba(244,197,66,0.12);
                        top:-24px;
                        right:-24px;
                    }

                    .decoracion-circulo-2{
                        position:absolute;
                        width:58px;
                        height:58px;
                        border-radius:50%%;
                        background:rgba(244,197,66,0.18);
                        top:34px;
                        right:58px;
                    }

                    .encabezado{
                        position:relative;
                        z-index:1;
                        margin-bottom:28px;
                    }

                    .logo-bloque{
                        width:92px;
                        height:92px;
                        border-radius:18px;
                        background:linear-gradient(180deg,#fff8e1,#fffdf5);
                        border:2px solid #f4d56a;
                        display:flex;
                        align-items:center;
                        justify-content:center;
                        margin-bottom:16px;
                        box-shadow:0 8px 18px rgba(244,197,66,0.22);
                        color:#0a3d91;
                        font-weight:bold;
                        font-size:20px;
                        text-align:center;
                    }

                    .logo-bloque img{
                        width:68px;
                        height:68px;
                        object-fit:contain;
                        display:block;
                    }

                    .titulo-principal{
                        margin:0;
                        font-size:40px;
                        line-height:1.12;
                        color:#0a3d91;
                        font-weight:bold;
                    }

                    .subtitulo{
                        margin:12px 0 0 0;
                        font-size:18px;
                        color:#4b5563;
                        line-height:1.6;
                        max-width:900px;
                    }

                    .linea-dorada{
                        width:145px;
                        height:6px;
                        border-radius:999px;
                        background:linear-gradient(90deg,#f4c542,#ffd95a);
                        margin-top:18px;
                    }

                    .barra-superior{
                        position:relative;
                        z-index:1;
                        display:flex;
                        justify-content:space-between;
                        align-items:center;
                        gap:14px;
                        flex-wrap:wrap;
                        margin-bottom:20px;
                    }

                    .texto-apoyo{
                        color:#4b5563;
                        font-size:16px;
                        line-height:1.6;
                        max-width:820px;
                    }

                    .acciones-superiores{
                        display:flex;
                        gap:12px;
                        flex-wrap:wrap;
                    }

                    .btn-volver{
                        display:inline-block;
                        text-decoration:none;
                        padding:13px 18px;
                        border-radius:10px;
                        font-weight:bold;
                        font-size:15px;
                        background:#6c757d;
                        color:white;
                        transition:transform 0.15s ease, opacity 0.2s ease;
                    }

                    .btn-volver:hover{
                        transform:translateY(-1px);
                        opacity:0.96;
                    }

                    .panel{
                        position:relative;
                        z-index:1;
                        background:linear-gradient(180deg,#ffffff,#fffdf7);
                        border:1px solid #f3e6b2;
                        border-radius:20px;
                        overflow:hidden;
                        box-shadow:inset 0 1px 0 rgba(255,255,255,0.75);
                    }

                    .panel-top{
                        height:6px;
                        background:linear-gradient(90deg,#f4c542,#ffd95a,#f4c542);
                    }

                    .tabla-wrap{
                        overflow-x:auto;
                    }

                    table{
                        width:100%%;
                        border-collapse:collapse;
                    }

                    thead{
                        background:#f8fafc;
                    }

                    th{
                        text-align:left;
                        padding:18px 16px;
                        font-size:16px;
                        color:#374151;
                        border-bottom:1px solid #e5e7eb;
                    }

                    td{
                        padding:16px;
                        border-bottom:1px solid #eef2f7;
                        vertical-align:middle;
                        font-size:16px;
                    }

                    tbody tr:hover{
                        background:#fafcff;
                    }

                    .badge{
                        display:inline-block;
                        padding:8px 14px;
                        border-radius:999px;
                        font-size:14px;
                        font-weight:bold;
                    }

                    .badge-activa{
                        background:#dcfce7;
                        color:#166534;
                    }

                    .badge-cancelada{
                        background:#fee2e2;
                        color:#991b1b;
                    }

                    .btn-cancelar{
                        display:inline-block;
                        text-decoration:none;
                        padding:10px 14px;
                        border-radius:8px;
                        background:#ef4444;
                        color:white;
                        font-size:14px;
                        font-weight:bold;
                    }

                    .btn-ver-qr{
                        display:inline-block;
                        border:none;
                        padding:10px 14px;
                        border-radius:8px;
                        background:linear-gradient(90deg,#0a3d91,#174ea6);
                        color:white;
                        font-size:14px;
                        font-weight:bold;
                        cursor:pointer;
                    }

                    .btn-deshabilitado{
                        display:inline-block;
                        padding:10px 14px;
                        border-radius:8px;
                        background:#d1d5db;
                        color:#374151;
                        font-size:14px;
                        font-weight:bold;
                    }

                    /* MODALES */
                    .modal-qr{
                        display:none;
                        position:fixed;
                        inset:0;
                        background:rgba(15,23,42,0.70);
                        z-index:9999;
                        align-items:center;
                        justify-content:center;
                        padding:20px;
                    }

                    .modal-qr.activo{
                        display:flex;
                    }

                    .modal-contenido{
                        width:100%%;
                        max-width:520px;
                        background:white;
                        border-radius:24px;
                        padding:28px 24px 24px;
                        box-shadow:0 24px 50px rgba(0,0,0,0.25);
                        position:relative;
                        text-align:center;
                    }

                    .modal-cerrar{
                        position:absolute;
                        top:14px;
                        right:16px;
                        background:#ef4444;
                        color:white;
                        border:none;
                        width:38px;
                        height:38px;
                        border-radius:50%%;
                        cursor:pointer;
                        font-size:18px;
                        font-weight:bold;
                    }

                    .modal-titulo{
                        margin:0 0 14px 0;
                        font-size:28px;
                        color:#0a3d91;
                    }

                    .modal-subtitulo{
                        margin:0 0 18px 0;
                        font-size:15px;
                        color:#4b5563;
                        line-height:1.6;
                    }

                    .modal-qr-img{
                        width:230px;
                        height:230px;
                        object-fit:contain;
                        border:1px solid #d1d5db;
                        border-radius:16px;
                        padding:10px;
                        background:white;
                        box-shadow:0 4px 12px rgba(0,0,0,0.08);
                    }

                    .modal-evento{
                        margin-top:16px;
                        font-size:18px;
                        font-weight:bold;
                        color:#111827;
                    }

                    .modal-contenido-qr{
                        margin-top:12px;
                        font-size:12px;
                        line-height:1.6;
                        color:#6b7280;
                        word-break:break-word;
                        background:#f8fafc;
                        border-radius:12px;
                        padding:12px;
                    }

                    .modal-nota{
                        margin-top:16px;
                        background:#fff9e8;
                        border-left:5px solid #f4c542;
                        border-radius:14px;
                        padding:14px 15px;
                        color:#5b4a17;
                        font-size:14px;
                        line-height:1.6;
                        text-align:left;
                    }

                    @media (max-width: 1000px){
                        .titulo-principal{
                            font-size:32px;
                        }

                        .subtitulo{
                            font-size:16px;
                        }
                    }

                    @media (max-width: 560px){
                        .contenedor{
                            padding:24px 18px 20px;
                        }

                        .titulo-principal{
                            font-size:27px;
                        }

                        .barra-superior{
                            flex-direction:column;
                            align-items:flex-start;
                        }

                        th, td{
                            font-size:15px;
                        }

                        .modal-contenido{
                            padding:24px 18px 20px;
                        }

                        .modal-titulo{
                            font-size:24px;
                        }

                        .modal-qr-img{
                            width:190px;
                            height:190px;
                        }

                        .acciones-superiores{
                            width:100%%;
                            flex-direction:column;
                        }

                        .acciones-superiores button,
                        .acciones-superiores a{
                            width:100%%;
                            text-align:center;
                        }
                    }
                </style>
            </head>

            <body>
                <div class="contenedor">
                    <div class="decoracion-superior"></div>
                    <div class="decoracion-circulo-1"></div>
                    <div class="decoracion-circulo-2"></div>

                    <div class="encabezado">
                        <div class="logo-bloque">
                            <img src="/img/logo-pucmm.png"
                                 alt="Logo PUCMM"
                                 onerror="this.style.display='none'; this.parentElement.innerHTML='PUCMM';">
                        </div>

                        <h1 class="titulo-principal">Mis inscripciones</h1>

                        <p class="subtitulo">
                            Consulta los eventos en los que estás inscrito, revisa el estado de tu participación y muestra tu código QR cuando sea necesario para validación de asistencia.
                        </p>

                        <div class="linea-dorada"></div>
                    </div>

                    <div class="barra-superior">
                        <div class="texto-apoyo">
                            Utiliza <strong>Ver QR</strong> para mostrar tu código individual, o <strong>Escanear QR</strong> para validar con cámara un código y comprobar que corresponde a tu cuenta.
                        </div>

                        <div class="acciones-superiores">
                            <button type="button" class="btn-ver-qr" onclick="abrirModalEscaner()">Escanear QR</button>
                            <a href="/participante/dashboard" class="btn-volver">Volver</a>
                        </div>
                    </div>

                    <div class="panel">
                        <div class="panel-top"></div>

                        <div class="tabla-wrap">
                            <table>
                                <thead>
                                    <tr>
                                        <th>ID Evento</th>
                                        <th>Título</th>
                                        <th>Fecha</th>
                                        <th>Hora</th>
                                        <th>Ubicación</th>
                                        <th>Estado</th>
                                        <th>QR</th>
                                        <th>Acción</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    %s
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <div id="modalQr" class="modal-qr">
                    <div class="modal-contenido">
                        <button class="modal-cerrar" onclick="cerrarModalQr()">×</button>

                        <h2 class="modal-titulo">Código QR</h2>

                        <p class="modal-subtitulo">
                            Muestra este código al administrador u organizador para registrar tu asistencia.
                        </p>

                        <img id="modalQrImg" class="modal-qr-img" src="" alt="QR">

                        <div id="modalEvento" class="modal-evento"></div>

                        <div id="modalContenidoQr" class="modal-contenido-qr"></div>

                        <div class="modal-nota">
                            Instrucción: al presionar <strong>Ver QR</strong>, se mostrará tu código individual de inscripción. Este código puede ser escaneado por el personal autorizado para validar tu asistencia al evento.
                        </div>
                    </div>
                </div>

                <div id="modalEscaner" class="modal-qr">
                    <div class="modal-contenido">
                        <button class="modal-cerrar" onclick="cerrarModalEscaner()">×</button>

                        <h2 class="modal-titulo">Escanear QR</h2>

                        <p class="modal-subtitulo">
                            Permite acceso a la cámara para escanear un código QR. El sistema verificará que el QR pertenezca al participante autenticado.
                        </p>

                        <div id="readerParticipante" style="width:100%%; min-height:260px; border:1px solid #d1d5db; border-radius:16px; overflow:hidden; background:#f8fafc;"></div>

                        <div class="modal-nota" style="margin-top:16px;">
                            Instrucción para demostración: al presionar <strong>Escanear QR</strong>, el navegador solicitará acceso a la cámara. Luego muestra un QR. Si el código pertenece al usuario autenticado, se mostrará una validación exitosa; si no, aparecerá un mensaje de error.
                        </div>

                        <form id="formValidarQrParticipante" method="post" action="/participante/validar-qr" style="display:none;">
                            <input type="hidden" name="qrContenido" id="qrContenidoParticipante">
                        </form>
                    </div>
                </div>

                <script src="https://unpkg.com/html5-qrcode" type="text/javascript"></script>
                <script>
                    function abrirModalQr(boton) {
                        const titulo = boton.getAttribute('data-titulo');
                        const qr = boton.getAttribute('data-qr');
                        const contenido = boton.getAttribute('data-contenido');

                        document.getElementById('modalEvento').textContent = titulo;
                        document.getElementById('modalQrImg').src = qr;
                        document.getElementById('modalContenidoQr').textContent = contenido;

                        document.getElementById('modalQr').classList.add('activo');
                    }

                    function cerrarModalQr() {
                        document.getElementById('modalQr').classList.remove('activo');
                    }

                    let html5QrCodeParticipante = null;
                    let lectorParticipanteActivo = false;

                    function abrirModalEscaner() {
                        document.getElementById('modalEscaner').classList.add('activo');
                        iniciarEscaneoParticipante();
                    }

                    function cerrarModalEscaner() {
                        document.getElementById('modalEscaner').classList.remove('activo');
                        detenerEscaneoParticipante();
                    }

                    function iniciarEscaneoParticipante() {
                        if (lectorParticipanteActivo) {
                            return;
                        }

                        html5QrCodeParticipante = new Html5Qrcode("readerParticipante");

                        Html5Qrcode.getCameras().then(cameras => {
                            if (!cameras || cameras.length === 0) {
                                alert("No se encontró ninguna cámara disponible.");
                                return;
                            }

                            const camara = cameras[0].id;

                            html5QrCodeParticipante.start(
                                camara,
                                {
                                    fps: 10,
                                    qrbox: { width: 220, height: 220 }
                                },
                                (decodedText, decodedResult) => {
                                    document.getElementById("qrContenidoParticipante").value = decodedText;

                                    detenerEscaneoParticipante().then(() => {
                                        document.getElementById("formValidarQrParticipante").submit();
                                    });
                                },
                                (errorMessage) => {
                                }
                            ).then(() => {
                                lectorParticipanteActivo = true;
                            }).catch(err => {
                                alert("No fue posible iniciar la cámara: " + err);
                            });

                        }).catch(err => {
                            alert("Error obteniendo cámaras: " + err);
                        });
                    }

                    function detenerEscaneoParticipante() {
                        if (!html5QrCodeParticipante || !lectorParticipanteActivo) {
                            return Promise.resolve();
                        }

                        return html5QrCodeParticipante.stop()
                            .then(() => {
                                lectorParticipanteActivo = false;
                            })
                            .catch(err => {
                            });
                    }

                    window.addEventListener('click', function(e) {
                        const modalQr = document.getElementById('modalQr');
                        const modalEscaner = document.getElementById('modalEscaner');

                        if (e.target === modalQr) {
                            cerrarModalQr();
                        }

                        if (e.target === modalEscaner) {
                            cerrarModalEscaner();
                        }
                    });
                </script>
            </body>
            </html>
            """.formatted(filas);

        ctx.html(LayoutUtil.layoutParticipante(
                "Mis inscripciones",
                "Consulta los eventos en los que estás inscrito y muestra tu código QR para validar asistencia.",
                html
        ));

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
            String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>Error al cancelar inscripción</title>
                    <style>
                        * {
                            box-sizing: border-box;
                        }

                        body {
                            margin: 0;
                            min-height: 100vh;
                            font-family: Arial, sans-serif;
                            background:
                                radial-gradient(circle at 10%% 10%%, rgba(255, 214, 77, 0.22), transparent 18%%),
                                radial-gradient(circle at 90%% 85%%, rgba(255, 214, 77, 0.16), transparent 20%%),
                                linear-gradient(135deg, #0a3d91, #0d47a1 55%%, #1565c0);
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            padding: 30px 20px;
                            color: #1f2937;
                        }

                        .contenedor {
                            width: 100%%;
                            max-width: 760px;
                            background: #ffffff;
                            border-radius: 24px;
                            box-shadow: 0 24px 50px rgba(0, 0, 0, 0.24);
                            padding: 34px 32px 30px;
                            position: relative;
                            overflow: hidden;
                        }

                        .decoracion-superior {
                            position: absolute;
                            top: 0;
                            left: 0;
                            width: 100%%;
                            height: 8px;
                            background: linear-gradient(90deg, #f4c542, #ffd95a, #f4c542);
                        }

                        .logo-bloque {
                            width: 88px;
                            height: 88px;
                            border-radius: 18px;
                            background: linear-gradient(180deg, #fff8e1, #fffdf5);
                            border: 2px solid #f4d56a;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            margin-bottom: 16px;
                            box-shadow: 0 8px 18px rgba(244, 197, 66, 0.22);
                            color: #0a3d91;
                            font-weight: bold;
                            font-size: 20px;
                        }

                        .logo-bloque img {
                            width: 62px;
                            height: 62px;
                            object-fit: contain;
                        }

                        .titulo {
                            margin: 0;
                            font-size: 36px;
                            color: #991b1b;
                        }

                        .subtitulo {
                            margin-top: 12px;
                            font-size: 17px;
                            color: #4b5563;
                            line-height: 1.6;
                        }

                        .mensaje-error {
                            margin-top: 22px;
                            background: #fef2f2;
                            border-left: 5px solid #ef4444;
                            color: #991b1b;
                            border-radius: 14px;
                            padding: 16px;
                            font-size: 15px;
                            line-height: 1.6;
                        }

                        .acciones {
                            margin-top: 24px;
                            display: flex;
                            gap: 12px;
                            flex-wrap: wrap;
                        }

                        .btn {
                            display: inline-block;
                            text-decoration: none;
                            padding: 13px 18px;
                            border-radius: 10px;
                            font-weight: bold;
                            font-size: 15px;
                            transition: transform 0.15s ease, opacity 0.2s ease;
                        }

                        .btn:hover {
                            transform: translateY(-1px);
                            opacity: 0.96;
                        }

                        .btn-principal {
                            background: linear-gradient(90deg, #0a3d91, #174ea6);
                            color: white;
                        }

                        .btn-secundario {
                            background: #6c757d;
                            color: white;
                        }
                    </style>
                </head>
                <body>
                    <div class="contenedor">
                        <div class="decoracion-superior"></div>

                        <div class="logo-bloque">
                            <img src="/img/logo-pucmm.png" alt="Logo PUCMM"
                                 onerror="this.style.display='none'; this.parentElement.innerHTML='PUCMM';">
                        </div>

                        <h1 class="titulo">No fue posible cancelar la inscripción</h1>

                        <p class="subtitulo">
                            Ocurrió un inconveniente al procesar la cancelación. Revisa el motivo y vuelve a intentarlo si es necesario.
                        </p>

                        <div class="mensaje-error">
                            %s
                        </div>

                        <div class="acciones">
                            <a href="/participante/mis-inscripciones" class="btn btn-principal">Volver a mis inscripciones</a>
                            <a href="/participante/dashboard" class="btn btn-secundario">Ir al panel</a>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(e.getMessage());

            ctx.status(400).html(html);
        }
    }

    private String extraerTokenDesdeContenidoQr(String contenidoQr) {
        if (contenidoQr == null || contenidoQr.isBlank()) {
            throw new RuntimeException("El contenido del QR está vacío.");
        }

        String[] partes = contenidoQr.split(";");

        for (String parte : partes) {
            if (parte.startsWith("token=")) {
                return parte.substring("token=".length()).trim();
            }
        }

        throw new RuntimeException("No se encontró el token dentro del código QR.");
    }

    private String escaparHtml(String texto) {
        if (texto == null) {
            return "";
        }

        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    public void validarQrPropio(Context ctx) {
        if (!validarParticipante(ctx)) {
            return;
        }

        try {
            Usuario usuario = obtenerUsuarioSesion(ctx);

            String qrContenido = ctx.formParam("qrContenido");
            String tokenQr = extraerTokenDesdeContenidoQr(qrContenido);

            Inscripcion inscripcion = inscripcionServicio.buscarPorTokenQr(tokenQr);

            if (!inscripcion.getUsuario().getId().equals(usuario.getId())) {
                throw new RuntimeException("El código QR escaneado no corresponde al participante autenticado.");
            }

            if (inscripcion.isCancelada()) {
                throw new RuntimeException("La inscripción asociada a este QR está cancelada.");
            }

            String contenido = """
                <style>
                    .mensaje-panel {
                        max-width: 820px;
                        margin: 0 auto;
                        background: linear-gradient(180deg,#ffffff,#fffdf7);
                        border: 1px solid #f3e6b2;
                        border-radius: 22px;
                        padding: 28px 26px;
                        box-shadow: 0 12px 24px rgba(0,0,0,0.07);
                    }

                    .titulo-exito {
                        margin: 0 0 12px 0;
                        font-size: 34px;
                        color: #166534;
                    }

                    .texto-base {
                        font-size: 16px;
                        color: #4b5563;
                        line-height: 1.7;
                    }

                    .mensaje {
                        margin-top: 20px;
                        background: #ecfdf3;
                        border-left: 5px solid #22c55e;
                        color: #166534;
                        border-radius: 14px;
                        padding: 16px;
                        line-height: 1.7;
                        font-size: 15px;
                    }

                    .acciones {
                        margin-top: 24px;
                        display: flex;
                        gap: 12px;
                        flex-wrap: wrap;
                    }

                    @media (max-width: 560px) {
                        .acciones {
                            flex-direction: column;
                        }

                        .acciones .btn {
                            width: 100%%;
                            text-align: center;
                        }

                        .titulo-exito {
                            font-size: 28px;
                        }
                    }
                </style>

                <div class="mensaje-panel">
                    <h2 class="titulo-exito">Validación exitosa</h2>

                    <p class="texto-base">
                        El QR escaneado corresponde correctamente al participante autenticado.
                    </p>

                    <div class="mensaje">
                        El participante <strong>%s</strong> fue validado correctamente para el evento <strong>%s</strong>.
                    </div>

                    <div class="acciones">
                        <a href="/participante/mis-inscripciones" class="btn btn-principal">Volver a mis inscripciones</a>
                        <a href="/participante/dashboard" class="btn btn-secundario">Ir al dashboard</a>
                    </div>
                </div>
                """.formatted(
                    escaparHtml(usuario.getNombre()),
                    escaparHtml(inscripcion.getEvento().getTitulo())
            );

            ctx.html(LayoutUtil.layoutParticipante(
                    "Validación exitosa",
                    "El código QR fue validado correctamente para el participante autenticado.",
                    contenido
            ));

        } catch (Exception e) {
            String contenido = """
                <style>
                    .mensaje-panel {
                        max-width: 820px;
                        margin: 0 auto;
                        background: linear-gradient(180deg,#ffffff,#fffdf7);
                        border: 1px solid #f3e6b2;
                        border-radius: 22px;
                        padding: 28px 26px;
                        box-shadow: 0 12px 24px rgba(0,0,0,0.07);
                    }

                    .titulo-error {
                        margin: 0 0 12px 0;
                        font-size: 34px;
                        color: #991b1b;
                    }

                    .texto-base {
                        font-size: 16px;
                        color: #4b5563;
                        line-height: 1.7;
                    }

                    .mensaje {
                        margin-top: 20px;
                        background: #fef2f2;
                        border-left: 5px solid #ef4444;
                        color: #991b1b;
                        border-radius: 14px;
                        padding: 16px;
                        line-height: 1.7;
                        font-size: 15px;
                    }

                    .acciones {
                        margin-top: 24px;
                        display: flex;
                        gap: 12px;
                        flex-wrap: wrap;
                    }

                    @media (max-width: 560px) {
                        .acciones {
                            flex-direction: column;
                        }

                        .acciones .btn {
                            width: 100%%;
                            text-align: center;
                        }

                        .titulo-error {
                            font-size: 28px;
                        }
                    }
                </style>

                <div class="mensaje-panel">
                    <h2 class="titulo-error">QR inválido</h2>

                    <p class="texto-base">
                        No fue posible validar el código escaneado para el participante autenticado.
                    </p>

                    <div class="mensaje">%s</div>

                    <div class="acciones">
                        <a href="/participante/mis-inscripciones" class="btn btn-principal">Volver a mis inscripciones</a>
                        <a href="/participante/dashboard" class="btn btn-secundario">Ir al dashboard</a>
                    </div>
                </div>
                """.formatted(escaparHtml(e.getMessage()));

            ctx.status(400).html(LayoutUtil.layoutParticipante(
                    "QR inválido",
                    "Ocurrió un problema validando el código QR del participante.",
                    contenido
            ));
        }
    }

}