package edu.pucmm.icc352.controladores;

import edu.pucmm.icc352.modelos.Evento;
import edu.pucmm.icc352.modelos.Usuario;
import edu.pucmm.icc352.servicios.EventoServicio;
import edu.pucmm.icc352.servicios.UsuarioServicio;
import edu.pucmm.icc352.utilidades.SessionUtil;
import io.javalin.http.Context;
import edu.pucmm.icc352.utilidades.LayoutUtil;

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

        Usuario usuario = obtenerUsuarioSesion(ctx);

        List<Evento> misEventos = eventoServicio.listarTodos()
                .stream()
                .filter(e -> e.getOrganizador() != null && e.getOrganizador().getId().equals(usuario.getId()))
                .toList();

        long totalEventos = misEventos.size();
        long eventosPublicados = misEventos.stream().filter(Evento::isPublicado).count();
        long eventosCancelados = misEventos.stream().filter(Evento::isCancelado).count();
        long eventosFinalizados = misEventos.stream().filter(eventoServicio::eventoYaPaso).count();
        long eventosActivos = misEventos.stream()
                .filter(e -> !e.isCancelado())
                .filter(e -> !eventoServicio.eventoYaPaso(e))
                .count();

        String contenido = """
            <style>
                .hero {
                    display: grid;
                    grid-template-columns: 1.25fr 0.95fr;
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
                    font-size: 32px;
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
                    display: flex;
                    flex-direction: column;
                    justify-content: space-between;
                }

                .hero-secundario h3 {
                    margin-top: 0;
                    margin-bottom: 12px;
                    font-size: 24px;
                }

                .hero-secundario p {
                    margin: 0 0 14px 0;
                    font-size: 16px;
                    line-height: 1.6;
                    color: rgba(255,255,255,0.94);
                }

                .mini-etiquetas {
                    display: flex;
                    flex-wrap: wrap;
                    gap: 10px;
                    margin-top: 10px;
                }

                .mini-etiqueta {
                    display: inline-block;
                    padding: 8px 12px;
                    border-radius: 999px;
                    background: rgba(255,255,255,0.14);
                    border: 1px solid rgba(255,255,255,0.18);
                    color: white;
                    font-size: 13px;
                    font-weight: bold;
                }

                .metricas {
                    display: grid;
                    grid-template-columns: repeat(5, 1fr);
                    gap: 16px;
                    margin-bottom: 24px;
                }

                .metrica {
                    border-radius: 20px;
                    padding: 22px 20px;
                    box-shadow: 0 12px 24px rgba(0,0,0,0.07);
                    min-height: 150px;
                }

                .metrica h3 {
                    margin: 0 0 10px 0;
                    font-size: 16px;
                }

                .metrica .valor {
                    font-size: 32px;
                    font-weight: bold;
                    line-height: 1.1;
                }

                .metrica .texto {
                    margin-top: 10px;
                    font-size: 13px;
                    line-height: 1.6;
                }

                .m1 {
                    background: linear-gradient(180deg,#eef5ff,#f8fbff);
                    border: 1px solid #dbe7ff;
                    color: #0a3d91;
                }

                .m2 {
                    background: linear-gradient(180deg,#fff9e8,#fffdf7);
                    border: 1px solid #f3e6b2;
                    color: #8a6200;
                }

                .m3 {
                    background: linear-gradient(180deg,#ecfdf3,#f4fff8);
                    border: 1px solid #bbf7d0;
                    color: #166534;
                }

                .m4 {
                    background: linear-gradient(180deg,#fef2f2,#fff7f7);
                    border: 1px solid #fecaca;
                    color: #991b1b;
                }

                .m5 {
                    background: linear-gradient(180deg,#f5f3ff,#fbfaff);
                    border: 1px solid #ddd6fe;
                    color: #6d28d9;
                }

                .grid-principal {
                    display: grid;
                    grid-template-columns: 1.15fr 0.85fr;
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
                    font-size: 26px;
                    color: #0a3d91;
                }

                .panel-bloque p {
                    margin: 0 0 14px 0;
                    font-size: 15px;
                    line-height: 1.7;
                    color: #4b5563;
                }

                .lista-resumen {
                    margin: 0;
                    padding-left: 18px;
                    color: #374151;
                }

                .lista-resumen li {
                    margin-bottom: 10px;
                    line-height: 1.6;
                    font-size: 15px;
                }

                .grid-acciones {
                    display: grid;
                    grid-template-columns: repeat(3, 1fr);
                    gap: 20px;
                }

                .card {
                    background: linear-gradient(180deg, #ffffff, #fffdf7);
                    border: 1px solid #f3e6b2;
                    border-radius: 20px;
                    padding: 24px 22px;
                    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.07);
                    transition: transform 0.18s ease, box-shadow 0.2s ease;
                }

                .card:hover {
                    transform: translateY(-3px);
                    box-shadow: 0 18px 30px rgba(0, 0, 0, 0.10);
                }

                .card h3 {
                    margin-top: 0;
                    margin-bottom: 12px;
                    font-size: 24px;
                    color: #0a3d91;
                }

                .card p {
                    margin: 0 0 18px 0;
                    color: #4b5563;
                    font-size: 16px;
                    line-height: 1.6;
                }

                @media (max-width: 1250px) {
                    .metricas {
                        grid-template-columns: repeat(3, 1fr);
                    }

                    .grid-principal {
                        grid-template-columns: 1fr;
                    }
                }

                @media (max-width: 1180px) {
                    .hero {
                        grid-template-columns: 1fr;
                    }

                    .grid-acciones {
                        grid-template-columns: 1fr 1fr;
                    }
                }

                @media (max-width: 720px) {
                    .metricas {
                        grid-template-columns: 1fr;
                    }

                    .grid-acciones {
                        grid-template-columns: 1fr;
                    }

                    .hero-principal h2,
                    .panel-bloque h3 {
                        font-size: 25px;
                    }
                }
            </style>

            <div class="hero">
                <div class="hero-principal">
                    <h2>Centro operativo del organizador</h2>
                    <p>
                        Desde este panel puedes administrar la vida completa de tus eventos: creación, edición, publicación, revisión de inscritos, validación de asistencia y seguimiento de estado. Todo está concentrado en una vista operativa pensada para gestión en vivo.
                    </p>
                </div>

                <div class="hero-secundario">
                    <div>
                        <h3>Rol activo: organizador</h3>
                        <p>
                            Tienes control directo sobre los eventos asociados a tu cuenta y acceso a sus funciones principales.
                        </p>
                    </div>

                    <div class="mini-etiquetas">
                        <span class="mini-etiqueta">Mis eventos</span>
                        <span class="mini-etiqueta">Inscritos</span>
                        <span class="mini-etiqueta">QR</span>
                        <span class="mini-etiqueta">Asistencia</span>
                    </div>
                </div>
            </div>

            <div class="metricas">
                <div class="metrica m1">
                    <h3>Total eventos</h3>
                    <div class="valor">%d</div>
                    <div class="texto">Eventos creados bajo tu cuenta.</div>
                </div>

                <div class="metrica m2">
                    <h3>Publicados</h3>
                    <div class="valor">%d</div>
                    <div class="texto">Eventos visibles para los participantes.</div>
                </div>

                <div class="metrica m3">
                    <h3>Activos</h3>
                    <div class="valor">%d</div>
                    <div class="texto">Eventos no cancelados y todavía vigentes.</div>
                </div>

                <div class="metrica m4">
                    <h3>Cancelados</h3>
                    <div class="valor">%d</div>
                    <div class="texto">Eventos que ya no están operativos.</div>
                </div>

                <div class="metrica m5">
                    <h3>Finalizados</h3>
                    <div class="valor">%d</div>
                    <div class="texto">Eventos cuya fecha ya pasó.</div>
                </div>
            </div>

            <div class="grid-principal">
                <div class="panel-bloque">
                    <h3>Resumen operativo</h3>
                    <p>
                        Este panel te permite organizar la operación diaria de los eventos académicos. Puedes identificar rápidamente qué actividades están vigentes, cuáles ya finalizaron y cuáles requieren acciones como publicación, revisión de inscritos o validación por QR.
                    </p>

                    <ul class="lista-resumen">
                        <li>Los eventos publicados son los visibles para inscripción de participantes.</li>
                        <li>Los eventos activos permiten un seguimiento real de asistencia e inscritos.</li>
                        <li>Los eventos finalizados y cancelados te ayudan a mantener trazabilidad del trabajo realizado.</li>
                    </ul>
                </div>

                <div class="panel-bloque">
                    <h3>Acceso rápido</h3>
                    <p>Utiliza estos accesos para operar sin perder tiempo durante la gestión de tus actividades:</p>

                    <div style="display:flex; flex-direction:column; gap:12px; margin-top:14px;">
                        <a href="/organizador/eventos" class="btn btn-principal">Ir a mis eventos</a>
                        <a href="/organizador/eventos/nuevo" class="btn btn-principal">Crear evento</a>
                        <a href="/asistencia/registrar" class="btn btn-secundario">Ir a asistencia</a>
                    </div>
                </div>
            </div>

            <div class="grid-acciones">
                <div class="card">
                    <h3>Mis eventos</h3>
                    <p>Consulta el listado completo de los eventos bajo tu responsabilidad y opera sus acciones principales.</p>
                    <a href="/organizador/eventos" class="btn btn-principal">Ver eventos</a>
                </div>

                <div class="card">
                    <h3>Crear evento</h3>
                    <p>Registra nuevas actividades académicas y déjalas listas para publicación.</p>
                    <a href="/organizador/eventos/nuevo" class="btn btn-principal">Nuevo evento</a>
                </div>

                <div class="card">
                    <h3>Asistencia</h3>
                    <p>Registra asistencia por token, cámara o validación de QR según el flujo del sistema.</p>
                    <a href="/asistencia/registrar" class="btn btn-principal">Registrar asistencia</a>
                </div>

                <div class="card">
                    <h3>Inscritos</h3>
                    <p>Accede desde tus eventos a las listas de participantes inscritos y controla su validación.</p>
                    <a href="/organizador/eventos" class="btn btn-secundario">Explorar eventos</a>
                </div>

                <div class="card">
                    <h3>Mi sesión</h3>
                    <p>Consulta la información de tu cuenta autenticada y tu acceso actual como organizador.</p>
                    <a href="/me" class="btn btn-secundario">Ver sesión</a>
                </div>

                <div class="card">
                    <h3>Cerrar sesión</h3>
                    <p>Finaliza de forma segura la sesión actual del organizador dentro de la plataforma.</p>
                    <a href="/logout" class="btn btn-alerta">Cerrar sesión</a>
                </div>
            </div>
            """.formatted(
                totalEventos,
                eventosPublicados,
                eventosActivos,
                eventosCancelados,
                eventosFinalizados
        );

        ctx.html(LayoutUtil.layoutOrganizador(
                "Panel del organizador",
                "Administra tus eventos, controla asistencia, revisa estados y accede rápidamente a la operación principal de tu cuenta.",
                contenido
        ));
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
            String badgePublicado = evento.isPublicado()
                    ? "<span class='badge badge-publicado'>Publicado</span>"
                    : "<span class='badge badge-borrador'>Borrador</span>";

            String badgeCancelado;

            if (evento.isCancelado()) {
                badgeCancelado = "<span class='badge badge-cancelado'>Cancelado</span>";
            } else {
                String estadoTiempo = eventoServicio.obtenerEstadoTiempo(evento);

                if ("FINALIZADO".equals(estadoTiempo)) {
                    badgeCancelado = "<span class='badge badge-finalizado'>Finalizado</span>";
                } else if ("EN_CURSO".equals(estadoTiempo)) {
                    badgeCancelado = "<span class='badge badge-en-curso'>En curso</span>";
                } else {
                    badgeCancelado = "<span class='badge badge-programado'>Programado</span>";
                }
            }


            String acciones = """
                <div class="acciones-tabla">
                    <a class="btn-accion btn-editar" href='/organizador/eventos/editar/%d'>Editar</a>
                    <a class="btn-accion btn-publicar" href='/organizador/eventos/publicar/%d'>Publicar</a>
                    <a class="btn-accion btn-secundario" href='/organizador/eventos/despublicar/%d'>Despublicar</a>
                    <a class="btn-accion btn-cancelar" href='/organizador/eventos/cancelar/%d'>Cancelar</a>
                    <a class="btn-accion btn-resumen" href='/eventos/resumen/%d'>Resumen</a>
                    <a class="btn-accion btn-inscritos" href='/asistencia/evento/%d'>Ver inscritos</a>
                </div>
                """.formatted(
                    evento.getId(),
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
                    badgePublicado,
                    badgeCancelado,
                    acciones
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

                .barra-superior {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    gap: 16px;
                    flex-wrap: wrap;
                    margin-bottom: 20px;
                }

                .texto-apoyo {
                    color: #4b5563;
                    font-size: 16px;
                }

                .acciones-superiores {
                    display: flex;
                    gap: 12px;
                    flex-wrap: wrap;
                }

                .acciones-tabla {
                    display: flex;
                    flex-wrap: wrap;
                    gap: 8px;
                }

                .btn-accion {
                    display: inline-block;
                    text-decoration: none;
                    padding: 10px 13px;
                    border-radius: 8px;
                    color: white;
                    font-size: 14px;
                    font-weight: bold;
                }

                .btn-editar {
                    background: #0d6efd;
                }

                .btn-publicar {
                    background: #22c55e;
                }

                .btn-secundario {
                    background: #6c757d;
                }

                .btn-cancelar {
                    background: #ef4444;
                }

                .btn-resumen {
                    background: #7c3aed;
                }

                .btn-inscritos {
                    background: #f59e0b;
                }

                @media (max-width: 1000px) {
                    .resumen-superior {
                        grid-template-columns: 1fr;
                    }
                }
            </style>

            <div class="resumen-superior">
                <div class="card-resumen">
                    <h3>Gestión central</h3>
                    <p>Consulta todos tus eventos creados y administra sus acciones principales desde una sola vista.</p>
                </div>

                <div class="card-resumen">
                    <h3>Control de publicación</h3>
                    <p>Identifica rápidamente cuáles eventos están publicados, en borrador o cancelados.</p>
                </div>

                <div class="card-resumen">
                    <h3>Acceso a inscritos</h3>
                    <p>Desde cada evento puedes entrar a la lista de inscritos y registrar asistencia sin escribir tokens.</p>
                </div>
            </div>

            <div class="barra-superior">
                <div class="texto-apoyo">
                    Gestiona tus eventos académicos y accede directamente a sus funciones operativas.
                </div>

                <div class="acciones-superiores">
                    <a href="/organizador/eventos/nuevo" class="btn btn-principal">Crear evento</a>
                    <a href="/organizador/dashboard" class="btn btn-secundario">Volver</a>
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
                                <th>Publicado</th>
                                <th>Estado</th>
                                <th>Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            %s
                        </tbody>
                    </table>
                </div>
            </div>
            """.formatted(filas);

        ctx.html(LayoutUtil.layoutOrganizador(
                "Mis eventos",
                "Consulta, edita y controla los eventos creados bajo tu cuenta de organizador.",
                contenido
        ));
    }

    public void mostrarFormularioNuevoEvento(Context ctx) {
        if (!validarOrganizador(ctx)) {
            return;
        }

        String contenido = """
            <style>
                .contenido-grid {
                    display: grid;
                    grid-template-columns: 1fr 1.15fr;
                    gap: 24px;
                    align-items: start;
                }

                .panel-info {
                    background: linear-gradient(180deg, #f8fbff, #eef5ff);
                    border: 1px solid #dbe7ff;
                    border-radius: 20px;
                    padding: 24px;
                    box-shadow: 0 10px 24px rgba(13, 110, 253, 0.10);
                }

                .panel-info h2 {
                    margin-top: 0;
                    margin-bottom: 14px;
                    font-size: 28px;
                    color: #0a3d91;
                }

                .panel-info p {
                    margin: 0 0 16px 0;
                    font-size: 16px;
                    line-height: 1.6;
                    color: #374151;
                }

                .panel-info ul {
                    margin: 0;
                    padding-left: 20px;
                    color: #374151;
                }

                .panel-info li {
                    margin-bottom: 12px;
                    font-size: 15px;
                    line-height: 1.5;
                }

                .nota {
                    margin-top: 18px;
                    background: #fff9e8;
                    border-left: 5px solid #f4c542;
                    border-radius: 14px;
                    padding: 14px 15px;
                    color: #5b4a17;
                    font-size: 14px;
                    line-height: 1.6;
                }

                .panel-formulario {
                    background: linear-gradient(180deg, #ffffff, #fffdf7);
                    border: 1px solid #f3e6b2;
                    border-radius: 20px;
                    padding: 26px 24px 22px;
                    box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.75);
                }

                .titulo-formulario {
                    margin: 0 0 22px 0;
                    font-size: 28px;
                    color: #111827;
                }

                label {
                    display: block;
                    margin-bottom: 8px;
                    font-size: 15px;
                    font-weight: bold;
                    color: #1f2937;
                }

                input,
                textarea {
                    width: 100%;
                    padding: 14px 15px;
                    margin-bottom: 18px;
                    border: 1px solid #d7dde7;
                    border-radius: 12px;
                    font-size: 15px;
                    outline: none;
                    background: #ffffff;
                    transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
                }

                textarea {
                    min-height: 130px;
                    resize: vertical;
                }

                input:focus,
                textarea:focus {
                    border-color: #f4c542;
                    box-shadow: 0 0 0 4px rgba(244, 197, 66, 0.18);
                    transform: translateY(-1px);
                }

                .acciones {
                    display: flex;
                    gap: 12px;
                    flex-wrap: wrap;
                    margin-top: 8px;
                }

                @media (max-width: 980px) {
                    .contenido-grid {
                        grid-template-columns: 1fr;
                    }
                }

                @media (max-width: 560px) {
                    .titulo-formulario,
                    .panel-info h2 {
                        font-size: 24px;
                    }

                    .acciones {
                        flex-direction: column;
                    }

                    .acciones .btn {
                        width: 100%;
                        text-align: center;
                    }
                }
            </style>

            <div class="contenido-grid">
                <div class="panel-info">
                    <h2>Crear nuevo evento</h2>

                    <p>
                        Desde esta sección puedes registrar una nueva actividad académica y dejarla lista para publicación dentro del sistema.
                    </p>

                    <ul>
                        <li><strong>Título:</strong> nombre claro del evento.</li>
                        <li><strong>Descripción:</strong> detalle breve de la actividad.</li>
                        <li><strong>Fecha y hora:</strong> planificación oficial del evento.</li>
                        <li><strong>Ubicación:</strong> lugar donde se desarrollará.</li>
                        <li><strong>Cupo máximo:</strong> cantidad límite de participantes.</li>
                    </ul>

                    <div class="nota">
                        Recuerda que luego de crear el evento podrás editarlo, publicarlo, cancelarlo y revisar sus inscritos desde tu panel.
                    </div>
                </div>

                <div class="panel-formulario">
                    <h2 class="titulo-formulario">Formulario del evento</h2>

                    <form method="post" action="/organizador/eventos/nuevo">
                        <label for="titulo">Título</label>
                        <input id="titulo" type="text" name="titulo" required>

                        <label for="descripcion">Descripción</label>
                        <textarea id="descripcion" name="descripcion" required></textarea>

                        <label for="fecha">Fecha</label>
                        <input id="fecha" type="date" name="fecha" required>

                        <label for="hora">Hora</label>
                        <input id="hora" type="time" name="hora" required>

                        <label for="ubicacion">Ubicación</label>
                        <input id="ubicacion" type="text" name="ubicacion" required>

                        <label for="cupoMaximo">Cupo máximo</label>
                        <input id="cupoMaximo" type="number" name="cupoMaximo" min="1" required>

                        <div class="acciones">
                            <button type="submit" class="btn btn-principal">Guardar evento</button>
                            <a href="/organizador/eventos" class="btn btn-secundario">Volver</a>
                        </div>
                    </form>
                </div>
            </div>
            """;

        ctx.html(LayoutUtil.layoutOrganizador(
                "Crear evento",
                "Registra una nueva actividad académica y deja lista su estructura principal dentro de la plataforma.",
                contenido
        ));
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
            String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>Error al crear evento</title>
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
                            max-width: 780px;
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
                            line-height: 1.15;
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

                        <h1 class="titulo">No fue posible crear el evento</h1>

                        <p class="subtitulo">
                            El sistema no pudo registrar el evento con la información suministrada. Revisa el detalle mostrado y vuelve a intentarlo.
                        </p>

                        <div class="mensaje-error">
                            %s
                        </div>

                        <div class="acciones">
                            <a href="/organizador/eventos/nuevo" class="btn btn-principal">Volver al formulario</a>
                            <a href="/organizador/eventos" class="btn btn-secundario">Ir a mis eventos</a>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(e.getMessage());

            ctx.status(400).html(html);
        }
    }

    public void mostrarFormularioEditarEvento(Context ctx) {
        if (!validarOrganizador(ctx)) {
            return;
        }

        try {
            Evento evento = buscarEventoDelOrganizador(ctx);

            String contenido = """
                <style>
                    .contenido-grid {
                        display: grid;
                        grid-template-columns: 1fr 1.15fr;
                        gap: 24px;
                        align-items: start;
                    }

                    .panel-info {
                        background: linear-gradient(180deg, #f8fbff, #eef5ff);
                        border: 1px solid #dbe7ff;
                        border-radius: 20px;
                        padding: 24px;
                        box-shadow: 0 10px 24px rgba(13, 110, 253, 0.10);
                    }

                    .panel-info h2 {
                        margin-top: 0;
                        margin-bottom: 14px;
                        font-size: 28px;
                        color: #0a3d91;
                    }

                    .panel-info p {
                        margin: 0 0 16px 0;
                        font-size: 16px;
                        line-height: 1.6;
                        color: #374151;
                    }

                    .panel-info ul {
                        margin: 0;
                        padding-left: 20px;
                        color: #374151;
                    }

                    .panel-info li {
                        margin-bottom: 12px;
                        font-size: 15px;
                        line-height: 1.5;
                    }

                    .nota {
                        margin-top: 18px;
                        background: #fff9e8;
                        border-left: 5px solid #f4c542;
                        border-radius: 14px;
                        padding: 14px 15px;
                        color: #5b4a17;
                        font-size: 14px;
                        line-height: 1.6;
                    }

                    .panel-formulario {
                        background: linear-gradient(180deg, #ffffff, #fffdf7);
                        border: 1px solid #f3e6b2;
                        border-radius: 20px;
                        padding: 26px 24px 22px;
                        box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.75);
                    }

                    .titulo-formulario {
                        margin: 0 0 22px 0;
                        font-size: 28px;
                        color: #111827;
                    }

                    label {
                        display: block;
                        margin-bottom: 8px;
                        font-size: 15px;
                        font-weight: bold;
                        color: #1f2937;
                    }

                    input,
                    textarea {
                        width: 100%%;
                        padding: 14px 15px;
                        margin-bottom: 18px;
                        border: 1px solid #d7dde7;
                        border-radius: 12px;
                        font-size: 15px;
                        outline: none;
                        background: #ffffff;
                        transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
                    }

                    textarea {
                        min-height: 130px;
                        resize: vertical;
                    }

                    input:focus,
                    textarea:focus {
                        border-color: #f4c542;
                        box-shadow: 0 0 0 4px rgba(244, 197, 66, 0.18);
                        transform: translateY(-1px);
                    }

                    .meta-grid {
                        display: grid;
                        grid-template-columns: 1fr 1fr;
                        gap: 14px;
                        margin-bottom: 18px;
                    }

                    .meta-card {
                        background: #f8fafc;
                        border: 1px solid #e5e7eb;
                        border-radius: 16px;
                        padding: 16px;
                    }

                    .meta-titulo {
                        font-size: 13px;
                        color: #6b7280;
                        font-weight: bold;
                        text-transform: uppercase;
                        letter-spacing: 0.4px;
                        margin-bottom: 8px;
                    }

                    .meta-valor {
                        font-size: 17px;
                        color: #111827;
                        font-weight: bold;
                        line-height: 1.5;
                    }

                    .acciones {
                        display: flex;
                        gap: 12px;
                        flex-wrap: wrap;
                        margin-top: 8px;
                    }

                    @media (max-width: 980px) {
                        .contenido-grid {
                            grid-template-columns: 1fr;
                        }
                    }

                    @media (max-width: 680px) {
                        .meta-grid {
                            grid-template-columns: 1fr;
                        }
                    }

                    @media (max-width: 560px) {
                        .titulo-formulario,
                        .panel-info h2 {
                            font-size: 24px;
                        }

                        .acciones {
                            flex-direction: column;
                        }

                        .acciones .btn {
                            width: 100%%;
                            text-align: center;
                        }
                    }
                </style>

                <div class="contenido-grid">
                    <div class="panel-info">
                        <h2>Editar evento</h2>

                        <p>
                            Modifica la información principal del evento seleccionado y mantén actualizados sus datos antes o después de publicarlo.
                        </p>

                        <div class="meta-grid">
                            <div class="meta-card">
                                <div class="meta-titulo">ID del evento</div>
                                <div class="meta-valor">%d</div>
                            </div>

                            <div class="meta-card">
                                <div class="meta-titulo">Estado actual</div>
                                <div class="meta-valor">%s</div>
                            </div>
                        </div>

                        <ul>
                            <li><strong>Título:</strong> corrige el nombre visible del evento.</li>
                            <li><strong>Descripción:</strong> actualiza la explicación general de la actividad.</li>
                            <li><strong>Fecha y hora:</strong> ajusta la programación académica.</li>
                            <li><strong>Ubicación:</strong> modifica el lugar del evento si es necesario.</li>
                            <li><strong>Cupo máximo:</strong> cambia la capacidad disponible.</li>
                        </ul>

                        <div class="nota">
                            Verifica cuidadosamente cualquier cambio en fecha, hora o cupo, ya que puede afectar la participación e inscripción de los usuarios.
                        </div>
                    </div>

                    <div class="panel-formulario">
                        <h2 class="titulo-formulario">Formulario de edición</h2>

                        <form method="post" action="/organizador/eventos/editar/%d">
                            <label for="titulo">Título</label>
                            <input id="titulo" type="text" name="titulo" value="%s" required>

                            <label for="descripcion">Descripción</label>
                            <textarea id="descripcion" name="descripcion" required>%s</textarea>

                            <label for="fecha">Fecha</label>
                            <input id="fecha" type="date" name="fecha" value="%s" required>

                            <label for="hora">Hora</label>
                            <input id="hora" type="time" name="hora" value="%s" required>

                            <label for="ubicacion">Ubicación</label>
                            <input id="ubicacion" type="text" name="ubicacion" value="%s" required>

                            <label for="cupoMaximo">Cupo máximo</label>
                            <input id="cupoMaximo" type="number" name="cupoMaximo" min="1" value="%d" required>

                            <div class="acciones">
                                <button type="submit" class="btn btn-principal">Guardar cambios</button>
                                <a href="/organizador/eventos" class="btn btn-secundario">Volver</a>
                            </div>
                        </form>
                    </div>
                </div>
                """.formatted(
                    evento.getId(),
                    evento.isCancelado() ? "Cancelado" : (evento.isPublicado() ? "Publicado" : "Borrador"),
                    evento.getId(),
                    escaparHtml(evento.getTitulo()),
                    escaparHtml(evento.getDescripcion()),
                    evento.getFecha(),
                    evento.getHora(),
                    escaparHtml(evento.getUbicacion()),
                    evento.getCupoMaximo()
            );

            ctx.html(LayoutUtil.layoutOrganizador(
                    "Editar evento",
                    "Actualiza la información principal del evento y mantén su configuración al día dentro de la plataforma.",
                    contenido
            ));
        } catch (Exception e) {
            String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>Error al cargar edición</title>
                    <style>
                        * {
                            box-sizing: border-box;
                        }

                        body {
                            margin: 0;
                            min-height: 100vh;
                            font-family: Arial, sans-serif;
                            background:
                                radial-gradient(circle at 10%% 10%%, rgba(255,214,77,0.25), transparent 20%%),
                                radial-gradient(circle at 90%% 85%%, rgba(255,214,77,0.18), transparent 22%%),
                                linear-gradient(135deg,#0a3d91,#0d47a1 55%%,#1565c0);
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            padding: 30px 20px;
                        }

                        .contenedor {
                            width: 100%%;
                            max-width: 760px;
                            background: white;
                            border-radius: 24px;
                            box-shadow: 0 24px 50px rgba(0,0,0,0.25);
                            padding: 34px 32px 30px;
                            position: relative;
                            overflow: hidden;
                        }

                        .decoracion {
                            position: absolute;
                            top: 0;
                            left: 0;
                            width: 100%%;
                            height: 8px;
                            background: linear-gradient(90deg,#f4c542,#ffd95a,#f4c542);
                        }

                        h2 {
                            margin: 0 0 12px 0;
                            font-size: 34px;
                            color: #991b1b;
                        }

                        p {
                            font-size: 16px;
                            color: #4b5563;
                            line-height: 1.6;
                        }

                        .mensaje {
                            margin-top: 20px;
                            background: #fef2f2;
                            border-left: 5px solid #ef4444;
                            color: #991b1b;
                            border-radius: 14px;
                            padding: 16px;
                        }

                        a {
                            display: inline-block;
                            margin-top: 22px;
                            text-decoration: none;
                            padding: 13px 18px;
                            border-radius: 10px;
                            background: linear-gradient(90deg,#0a3d91,#174ea6);
                            color: white;
                            font-weight: bold;
                        }
                    </style>
                </head>
                <body>
                    <div class="contenedor">
                        <div class="decoracion"></div>
                        <h2>Error al cargar la edición</h2>
                        <p>No fue posible mostrar el formulario de edición del evento solicitado.</p>
                        <div class="mensaje">%s</div>
                        <a href="/organizador/eventos">Volver</a>
                    </div>
                </body>
                </html>
                """.formatted(e.getMessage());

            ctx.status(400).html(html);
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
            String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>Error al editar evento</title>
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
                            max-width: 780px;
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
                            line-height: 1.15;
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

                        <h1 class="titulo">No fue posible actualizar el evento</h1>

                        <p class="subtitulo">
                            La edición del evento no pudo completarse correctamente. Revisa el mensaje mostrado y vuelve a intentarlo.
                        </p>

                        <div class="mensaje-error">
                            %s
                        </div>

                        <div class="acciones">
                            <a href="/organizador/eventos" class="btn btn-principal">Volver a mis eventos</a>
                            <a href="/organizador/dashboard" class="btn btn-secundario">Ir al panel</a>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(e.getMessage());

            ctx.status(400).html(html);
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