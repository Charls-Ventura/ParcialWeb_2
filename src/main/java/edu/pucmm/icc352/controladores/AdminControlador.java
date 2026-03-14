package edu.pucmm.icc352.controladores;

import edu.pucmm.icc352.modelos.Evento;
import edu.pucmm.icc352.modelos.Rol;
import edu.pucmm.icc352.modelos.Usuario;
import edu.pucmm.icc352.servicios.EventoServicio;
import edu.pucmm.icc352.servicios.UsuarioServicio;
import edu.pucmm.icc352.utilidades.LayoutUtil;
import edu.pucmm.icc352.utilidades.SessionUtil;
import io.javalin.http.Context;

import java.util.List;

public class AdminControlador {

    private final UsuarioServicio usuarioServicio = new UsuarioServicio();
    private final EventoServicio eventoServicio = new EventoServicio();

    private boolean validarAdmin(Context ctx) {
        if (!SessionUtil.estaLogueado(ctx)) {
            ctx.redirect("/login");
            return false;
        }

        if (!SessionUtil.esAdmin(ctx)) {
            ctx.status(403).html("""
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <title>Acceso denegado</title>
                    </head>
                    <body style="font-family: Arial, sans-serif; padding: 40px;">
                        <h1>Acceso denegado</h1>
                        <p>Solo el administrador puede acceder a esta sección.</p>
                        <a href="/me">Volver</a>
                    </body>
                    </html>
                    """);
            return false;
        }

        return true;
    }

    public void dashboard(Context ctx) {
        if (!validarAdmin(ctx)) {
            return;
        }

        long totalUsuarios = usuarioServicio.listarTodos().size();
        long totalEventos = eventoServicio.listarTodos().size();
        long eventosPublicados = eventoServicio.listarTodos().stream().filter(Evento::isPublicado).count();
        long eventosCancelados = eventoServicio.listarTodos().stream().filter(Evento::isCancelado).count();
        long eventosFinalizados = eventoServicio.listarTodos().stream().filter(eventoServicio::eventoYaPaso).count();
        long eventosActivos = eventoServicio.listarTodos().stream()
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
                    grid-template-columns: repeat(6, 1fr);
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

                .m6 {
                    background: linear-gradient(180deg,#eff6ff,#f8fbff);
                    border: 1px solid #bfdbfe;
                    color: #1d4ed8;
                }

                .grid-principal {
                    display: grid;
                    grid-template-columns: 1.2fr 0.8fr;
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
                    <h2>Centro ejecutivo del sistema</h2>
                    <p>
                        Este panel concentra la administración general de la plataforma. Desde aquí puedes controlar usuarios, supervisar la actividad académica, revisar el estado de los eventos y mantener una visión clara del comportamiento global del sistema.
                    </p>
                </div>

                <div class="hero-secundario">
                    <div>
                        <h3>Rol activo: administrador</h3>
                        <p>
                            Tienes acceso transversal a usuarios, eventos, asistencia y monitoreo operativo.
                        </p>
                    </div>

                    <div class="mini-etiquetas">
                        <span class="mini-etiqueta">Usuarios</span>
                        <span class="mini-etiqueta">Eventos</span>
                        <span class="mini-etiqueta">Asistencia</span>
                        <span class="mini-etiqueta">Control total</span>
                    </div>
                </div>
            </div>

            <div class="metricas">
                <div class="metrica m1">
                    <h3>Total usuarios</h3>
                    <div class="valor">%d</div>
                    <div class="texto">Cuentas registradas dentro de la plataforma.</div>
                </div>

                <div class="metrica m2">
                    <h3>Total eventos</h3>
                    <div class="valor">%d</div>
                    <div class="texto">Eventos creados en el sistema.</div>
                </div>

                <div class="metrica m3">
                    <h3>Publicados</h3>
                    <div class="valor">%d</div>
                    <div class="texto">Eventos visibles o disponibles para operar.</div>
                </div>

                <div class="metrica m4">
                    <h3>Cancelados</h3>
                    <div class="valor">%d</div>
                    <div class="texto">Eventos retirados o anulados.</div>
                </div>

                <div class="metrica m5">
                    <h3>Finalizados</h3>
                    <div class="valor">%d</div>
                    <div class="texto">Eventos cuya fecha ya pasó.</div>
                </div>

                <div class="metrica m6">
                    <h3>Activos</h3>
                    <div class="valor">%d</div>
                    <div class="texto">Eventos no cancelados y aún vigentes.</div>
                </div>
            </div>

            <div class="grid-principal">
                <div class="panel-bloque">
                    <h3>Resumen operativo</h3>
                    <p>
                        El sistema mantiene una estructura centralizada donde los eventos pueden encontrarse en estado programado, en curso, finalizado o cancelado. Las métricas superiores permiten una lectura rápida del comportamiento institucional del entorno académico.
                    </p>

                    <ul class="lista-resumen">
                        <li>Los usuarios pueden dividirse entre administradores, organizadores y participantes.</li>
                        <li>Los eventos publicados y activos son los que sostienen la operación académica vigente.</li>
                        <li>Los eventos finalizados y cancelados ayudan a evaluar el historial operativo del sistema.</li>
                    </ul>
                </div>

                <div class="panel-bloque">
                    <h3>Acceso rápido</h3>
                    <p>Utiliza estos accesos para navegar rápidamente por las áreas más importantes del sistema:</p>

                    <div style="display:flex; flex-direction:column; gap:12px; margin-top:14px;">
                        <a href="/admin/usuarios" class="btn btn-principal">Gestionar usuarios</a>
                        <a href="/admin/eventos" class="btn btn-principal">Gestionar eventos</a>
                        <a href="/asistencia/registrar" class="btn btn-secundario">Ir a asistencia</a>
                    </div>
                </div>
            </div>

            <div class="grid-acciones">
                <div class="card">
                    <h3>Usuarios</h3>
                    <p>Consulta el listado completo de usuarios, sus roles y su estado dentro del sistema.</p>
                    <a href="/admin/usuarios" class="btn btn-principal">Gestionar usuarios</a>
                </div>

                <div class="card">
                    <h3>Registrar usuario</h3>
                    <p>Crea nuevas cuentas para administradores, organizadores o participantes.</p>
                    <a href="/admin/usuarios/nuevo" class="btn btn-principal">Nuevo usuario</a>
                </div>

                <div class="card">
                    <h3>Eventos</h3>
                    <p>Revisa todos los eventos del sistema, sus estados y sus funciones administrativas.</p>
                    <a href="/admin/eventos" class="btn btn-principal">Ver eventos</a>
                </div>

                <div class="card">
                    <h3>Asistencia</h3>
                    <p>Accede al módulo de registro de asistencia mediante token, cámara o lista de inscritos.</p>
                    <a href="/asistencia/registrar" class="btn btn-principal">Registrar asistencia</a>
                </div>

                <div class="card">
                    <h3>Mi sesión</h3>
                    <p>Consulta la información del usuario autenticado y el acceso administrativo actual.</p>
                    <a href="/me" class="btn btn-secundario">Ver sesión</a>
                </div>

                <div class="card">
                    <h3>Cerrar sesión</h3>
                    <p>Finaliza de forma segura el acceso administrativo actual dentro del sistema.</p>
                    <a href="/logout" class="btn btn-alerta">Cerrar sesión</a>
                </div>
            </div>
            """.formatted(
                totalUsuarios,
                totalEventos,
                eventosPublicados,
                eventosCancelados,
                eventosFinalizados,
                eventosActivos
        );

        ctx.html(LayoutUtil.layoutAdmin(
                "Panel de administración",
                "Supervisa usuarios, eventos, estados operativos y accesos principales desde una vista administrativa integral.",
                contenido
        ));
    }

    public void listarUsuarios(Context ctx) {
        if (!validarAdmin(ctx)) {
            return;
        }

        List<Usuario> usuarios = usuarioServicio.listarTodos();

        StringBuilder filas = new StringBuilder();

        for (Usuario usuario : usuarios) {
            String badgeRol = switch (usuario.getRol()) {
                case ADMIN -> "<span class='badge badge-admin'>ADMIN</span>";
                case ORGANIZADOR -> "<span class='badge badge-organizador'>ORGANIZADOR</span>";
                case PARTICIPANTE -> "<span class='badge badge-participante'>PARTICIPANTE</span>";
            };

            String badgeEstado = usuario.isBloqueado()
                    ? "<span class='badge badge-bloqueado'>Bloqueado</span>"
                    : "<span class='badge badge-activo'>Activo</span>";

            String accion = usuario.isBloqueado()
                    ? "<a href='/admin/usuarios/desbloquear/" + usuario.getId() + "' class='btn btn-principal'>Desbloquear</a>"
                    : "<a href='/admin/usuarios/bloquear/" + usuario.getId() + "' class='btn btn-alerta'>Bloquear</a>";

            filas.append("""
                <tr>
                    <td>%d</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                </tr>
                """.formatted(
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getCorreo(),
                    badgeRol,
                    badgeEstado,
                    accion
            ));
        }

        String contenido = """
            <div class="panel">
                <div class="panel-top"></div>

                <div style="padding: 22px 22px 0 22px; display:flex; justify-content:space-between; align-items:center; gap:16px; flex-wrap:wrap;">
                    <div style="color:#4b5563; font-size:16px;">
                        Administra los usuarios registrados, consulta sus roles y controla su estado dentro del sistema.
                    </div>

                    <a href="/admin/usuarios/nuevo" class="btn btn-principal">Registrar usuario</a>
                </div>

                <div class="tabla-wrap">
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Nombre</th>
                                <th>Correo</th>
                                <th>Rol</th>
                                <th>Estado</th>
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

        ctx.html(LayoutUtil.layoutAdmin(
                "Gestión de usuarios",
                "Consulta los usuarios del sistema, revisa sus roles y administra bloqueos o desbloqueos de manera centralizada.",
                contenido
        ));
    }

    public void mostrarFormularioNuevoUsuario(Context ctx) {
        if (!validarAdmin(ctx)) {
            return;
        }

        String html = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <title>Registrar usuario</title>
                <style>
                    * {
                        box-sizing: border-box;
                    }

                    body {
                        margin: 0;
                        min-height: 100vh;
                        font-family: Arial, sans-serif;
                        background:
                            radial-gradient(circle at 10% 10%, rgba(255, 214, 77, 0.20), transparent 18%),
                            radial-gradient(circle at 90% 85%, rgba(255, 214, 77, 0.14), transparent 20%),
                            linear-gradient(135deg, #0a3d91, #0d47a1 55%, #1565c0);
                        padding: 34px 20px 40px;
                        color: #1f2937;
                    }

                    .contenedor {
                        max-width: 1280px;
                        margin: auto;
                        background: #ffffff;
                        border-radius: 24px;
                        box-shadow: 0 24px 50px rgba(0, 0, 0, 0.24);
                        padding: 32px;
                        position: relative;
                        overflow: hidden;
                    }

                    .decoracion-superior {
                        position: absolute;
                        top: 0;
                        left: 0;
                        width: 100%;
                        height: 8px;
                        background: linear-gradient(90deg, #f4c542, #ffd95a, #f4c542);
                    }

                    .decoracion-circulo-1 {
                        position: absolute;
                        width: 95px;
                        height: 95px;
                        border-radius: 50%;
                        background: rgba(244, 197, 66, 0.12);
                        top: -24px;
                        right: -24px;
                    }

                    .decoracion-circulo-2 {
                        position: absolute;
                        width: 58px;
                        height: 58px;
                        border-radius: 50%;
                        background: rgba(244, 197, 66, 0.18);
                        top: 34px;
                        right: 58px;
                    }

                    .encabezado {
                        position: relative;
                        z-index: 1;
                        margin-bottom: 28px;
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
                        font-size: 19px;
                        text-align: center;
                    }

                    .logo-bloque img {
                        width: 64px;
                        height: 64px;
                        object-fit: contain;
                        display: block;
                    }

                    .titulo-principal {
                        margin: 0;
                        font-size: 40px;
                        line-height: 1.12;
                        color: #0a3d91;
                        font-weight: bold;
                    }

                    .subtitulo {
                        margin: 12px 0 0 0;
                        font-size: 18px;
                        color: #4b5563;
                        line-height: 1.6;
                        max-width: 840px;
                    }

                    .linea-dorada {
                        width: 140px;
                        height: 6px;
                        border-radius: 999px;
                        background: linear-gradient(90deg, #f4c542, #ffd95a);
                        margin-top: 18px;
                    }

                    .contenido {
                        position: relative;
                        z-index: 1;
                        display: grid;
                        grid-template-columns: 1fr 1.2fr;
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
                    select {
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

                    input:focus,
                    select:focus {
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

                    button,
                    .btn-volver {
                        display: inline-block;
                        text-decoration: none;
                        padding: 13px 18px;
                        border-radius: 10px;
                        font-weight: bold;
                        font-size: 15px;
                        border: none;
                        cursor: pointer;
                        transition: transform 0.15s ease, opacity 0.2s ease;
                    }

                    button:hover,
                    .btn-volver:hover {
                        transform: translateY(-1px);
                        opacity: 0.96;
                    }

                    button {
                        background: linear-gradient(90deg, #0a3d91, #174ea6);
                        color: white;
                        box-shadow: 0 8px 18px rgba(10, 61, 145, 0.18);
                    }

                    .btn-volver {
                        background: #6c757d;
                        color: white;
                    }

                    @media (max-width: 980px) {
                        .contenido {
                            grid-template-columns: 1fr;
                        }

                        .titulo-principal {
                            font-size: 32px;
                        }

                        .subtitulo {
                            font-size: 16px;
                        }
                    }

                    @media (max-width: 560px) {
                        .contenedor {
                            padding: 24px 18px 20px;
                        }

                        .titulo-principal {
                            font-size: 27px;
                        }

                        .panel-info h2,
                        .titulo-formulario {
                            font-size: 24px;
                        }

                        .acciones {
                            flex-direction: column;
                        }

                        button,
                        .btn-volver {
                            width: 100%;
                            text-align: center;
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
                            <img src="/img/logo-pucmm.png" alt="Logo PUCMM"
                                 onerror="this.style.display='none'; this.parentElement.innerHTML='PUCMM';">
                        </div>

                        <h1 class="titulo-principal">Registrar nuevo usuario</h1>
                        <p class="subtitulo">
                            Crea nuevos administradores, organizadores o participantes dentro del sistema de eventos académicos.
                        </p>
                        <div class="linea-dorada"></div>
                    </div>

                    <div class="contenido">
                        <div class="panel-info">
                            <h2>Panel de control</h2>
                            <p>
                                Desde esta sección puedes registrar manualmente usuarios dentro de la plataforma y asignarles el rol correspondiente.
                            </p>

                            <ul>
                                <li><strong>ADMIN:</strong> acceso total al sistema y gestión completa.</li>
                                <li><strong>ORGANIZADOR:</strong> creación y administración de eventos.</li>
                                <li><strong>PARTICIPANTE:</strong> inscripción y asistencia a eventos.</li>
                            </ul>

                            <div class="nota">
                                Verifica cuidadosamente el correo y el rol antes de guardar, para evitar accesos o permisos incorrectos.
                            </div>
                        </div>

                        <div class="panel-formulario">
                            <h2 class="titulo-formulario">Formulario de registro</h2>

                            <form method="post" action="/admin/usuarios/nuevo">
                                <label for="nombre">Nombre</label>
                                <input id="nombre" type="text" name="nombre" required>

                                <label for="correo">Correo</label>
                                <input id="correo" type="email" name="correo" required>

                                <label for="password">Contraseña</label>
                                <input id="password" type="password" name="password" required>

                                <label for="rol">Rol</label>
                                <select id="rol" name="rol" required>
                                    <option value="ORGANIZADOR">ORGANIZADOR</option>
                                    <option value="PARTICIPANTE">PARTICIPANTE</option>
                                    <option value="ADMIN">ADMIN</option>
                                </select>

                                <div class="acciones">
                                    <button type="submit">Guardar usuario</button>
                                    <a href="/admin/usuarios" class="btn-volver">Volver</a>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """;

        ctx.html(html);
    }

    public void guardarNuevoUsuario(Context ctx) {
        if (!validarAdmin(ctx)) {
            return;
        }

        try {
            String nombre = ctx.formParam("nombre");
            String correo = ctx.formParam("correo");
            String password = ctx.formParam("password");
            String rolTexto = ctx.formParam("rol");

            Rol rol = Rol.valueOf(rolTexto);
            usuarioServicio.registrarUsuario(nombre, correo, password, rol);

            ctx.redirect("/admin/usuarios");
        } catch (Exception e) {
            String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>Error al registrar usuario</title>
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

                        <h1 class="titulo">No fue posible registrar el usuario</h1>

                        <p class="subtitulo">
                            El sistema no pudo completar el registro con la información suministrada. Revisa el detalle mostrado y vuelve a intentarlo.
                        </p>

                        <div class="mensaje-error">
                            %s
                        </div>

                        <div class="acciones">
                            <a href="/admin/usuarios/nuevo" class="btn btn-principal">Volver al formulario</a>
                            <a href="/admin/usuarios" class="btn btn-secundario">Ir a usuarios</a>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(e.getMessage());

            ctx.status(400).html(html);
        }
    }

    public void listarEventos(Context ctx) {
        if (!validarAdmin(ctx)) {
            return;
        }

        List<Evento> eventos = eventoServicio.listarTodos();
        StringBuilder filas = new StringBuilder();

        for (Evento evento : eventos) {
            String organizador = evento.getOrganizador() != null
                    ? evento.getOrganizador().getNombre()
                    : "Sin organizador";

            String badgePublicado = evento.isPublicado()
                    ? "<span class='badge badge-publicado'>Publicado</span>"
                    : "<span class='badge badge-no-publicado'>No publicado</span>";

            String badgeEstado;

            if (evento.isCancelado()) {
                badgeEstado = "<span class='badge badge-cancelado'>Cancelado</span>";
            } else {
                String estadoTiempo = eventoServicio.obtenerEstadoTiempo(evento);

                if ("FINALIZADO".equals(estadoTiempo)) {
                    badgeEstado = "<span class='badge badge-finalizado'>Finalizado</span>";
                } else if ("EN_CURSO".equals(estadoTiempo)) {
                    badgeEstado = "<span class='badge badge-en-curso'>En curso</span>";
                } else {
                    badgeEstado = "<span class='badge badge-programado'>Programado</span>";
                }
            }

            String acciones = """
                <div class="acciones-tabla">
                    <a class="btn-accion btn-resumen" href="/eventos/resumen/%d">Resumen</a>
                    <a class="btn-accion btn-inscritos" href="/asistencia/evento/%d">Ver inscritos</a>
                    <a class="btn-accion btn-eliminar" href="/admin/eventos/eliminar/%d">Eliminar</a>
                </div>
                """.formatted(
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
                    <td>%s</td>
                </tr>
                """.formatted(
                    evento.getId(),
                    evento.getTitulo(),
                    evento.getFecha(),
                    evento.getHora(),
                    organizador,
                    badgePublicado,
                    badgeEstado,
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

                .btn-resumen {
                    background: #7c3aed;
                }

                .btn-inscritos {
                    background: #f59e0b;
                }

                .btn-eliminar {
                    background: #ef4444;
                }

                @media (max-width: 1000px) {
                    .resumen-superior {
                        grid-template-columns: 1fr;
                    }
                }
            </style>

            <div class="resumen-superior">
                <div class="card-resumen">
                    <h3>Control centralizado</h3>
                    <p>Consulta todos los eventos del sistema desde una sola vista administrativa.</p>
                </div>

                <div class="card-resumen">
                    <h3>Resumen y asistencia</h3>
                    <p>Accede al resumen del evento y a la lista de inscritos para control de asistencia.</p>
                </div>

                <div class="card-resumen">
                    <h3>Gestión administrativa</h3>
                    <p>Elimina eventos cuando sea necesario y supervisa su estado general dentro de la plataforma.</p>
                </div>
            </div>

            <div class="barra-superior">
                <div class="texto-apoyo">
                    Supervisa todos los eventos académicos registrados y accede a sus funciones de control.
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
                                <th>Organizador</th>
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

        ctx.html(LayoutUtil.layoutAdmin(
                "Gestión de eventos",
                "Consulta y administra los eventos del sistema desde una vista centralizada.",
                contenido
        ));
    }


    public void eliminarEvento(Context ctx) {
        if (!validarAdmin(ctx)) {
            return;
        }

        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            eventoServicio.eliminarEvento(id);
            ctx.redirect("/admin/eventos");
        } catch (Exception e) {
            ctx.status(400).html("""
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <title>Error</title>
                    </head>
                    <body style="font-family: Arial, sans-serif; padding: 40px;">
                        <h2>Error al eliminar evento</h2>
                        <p>%s</p>
                        <a href="/admin/eventos">Volver</a>
                    </body>
                    </html>
                    """.formatted(e.getMessage()));
        }
    }

    public void bloquearUsuario(Context ctx) {
        if (!validarAdmin(ctx)) {
            return;
        }

        Long id = Long.parseLong(ctx.pathParam("id"));
        Long usuarioActualId = SessionUtil.obtenerUsuarioId(ctx);

        if (id.equals(usuarioActualId)) {
            ctx.status(400).result("No puedes bloquear tu propio usuario administrador.");
            return;
        }

        usuarioServicio.bloquearUsuario(id);
        ctx.redirect("/admin/usuarios");
    }

    public void desbloquearUsuario(Context ctx) {
        if (!validarAdmin(ctx)) {
            return;
        }

        Long id = Long.parseLong(ctx.pathParam("id"));
        usuarioServicio.desbloquearUsuario(id);
        ctx.redirect("/admin/usuarios");
    }
}