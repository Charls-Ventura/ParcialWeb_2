package edu.pucmm.icc352.controladores;

import edu.pucmm.icc352.modelos.Rol;
import edu.pucmm.icc352.modelos.Usuario;
import edu.pucmm.icc352.servicios.UsuarioServicio;
import edu.pucmm.icc352.utilidades.SessionUtil;
import io.javalin.http.Context;
import edu.pucmm.icc352.utilidades.LayoutUtil;

public class AuthControlador {

    private final UsuarioServicio usuarioServicio = new UsuarioServicio();

    public void mostrarLogin(Context ctx) {
        if (SessionUtil.estaLogueado(ctx)) {
            ctx.redirect("/me");
            return;
        }

        String mensajeExito = null;
        if ("ok".equals(ctx.queryParam("registro"))) {
            mensajeExito = "Tu cuenta fue creada correctamente. Ya puedes iniciar sesión.";
        }

        ctx.html(construirHtmlLogin(mensajeExito, null));
    }

    public void procesarLogin(Context ctx) {
        try {
            String correo = ctx.formParam("correo");
            String password = ctx.formParam("password");

            Usuario usuario = usuarioServicio.autenticar(correo, password);
            SessionUtil.guardarUsuarioEnSesion(ctx, usuario);

            ctx.redirect("/me");
        } catch (Exception e) {
            ctx.status(401).html(construirHtmlLogin(null, e.getMessage()));
        }
    }

    public void mostrarFormularioRegistro(Context ctx) {
        if (SessionUtil.estaLogueado(ctx)) {
            ctx.redirect("/me");
            return;
        }

        ctx.html(construirHtmlRegistro("", "", null));
    }

    public void procesarRegistro(Context ctx) {
        if (SessionUtil.estaLogueado(ctx)) {
            ctx.redirect("/me");
            return;
        }

        String nombre = valorSeguro(ctx.formParam("nombre"));
        String correo = valorSeguro(ctx.formParam("correo"));
        String password = valorSeguro(ctx.formParam("password"));
        String confirmarPassword = valorSeguro(ctx.formParam("confirmarPassword"));

        try {
            if (!password.equals(confirmarPassword)) {
                throw new RuntimeException("Las contraseñas no coinciden.");
            }

            usuarioServicio.registrarUsuario(
                    nombre,
                    correo,
                    password,
                    Rol.PARTICIPANTE
            );

            ctx.redirect("/login?registro=ok");
        } catch (Exception e) {
            ctx.status(400).html(construirHtmlRegistro(nombre, correo, e.getMessage()));
        }
    }

    public void verSesionActual(Context ctx) {
        if (!SessionUtil.estaLogueado(ctx)) {
            ctx.redirect("/login");
            return;
        }

        Long usuarioId = SessionUtil.obtenerUsuarioId(ctx);
        Usuario usuario = usuarioServicio.buscarPorId(usuarioId);

        if ("ADMIN".equals(usuario.getRol().name())) {
            String contenido = """
                <div class="panel">
                    <div class="panel-top"></div>

                    <div class="tabla-wrap">
                        <table>
                            <tbody>
                                <tr>
                                    <th style="width:220px;">ID</th>
                                    <td>%d</td>
                                </tr>
                                <tr>
                                    <th>Nombre</th>
                                    <td>%s</td>
                                </tr>
                                <tr>
                                    <th>Correo</th>
                                    <td>%s</td>
                                </tr>
                                <tr>
                                    <th>Rol</th>
                                    <td><span class="badge badge-admin">ADMIN</span></td>
                                </tr>
                                <tr>
                                    <th>Estado</th>
                                    <td>%s</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div style="padding:22px; display:flex; gap:12px; flex-wrap:wrap;">
                        <a href="/admin/dashboard" class="btn btn-principal">Ir al dashboard</a>
                        <a href="/logout" class="btn btn-alerta">Cerrar sesión</a>
                    </div>
                </div>
                """.formatted(
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getCorreo(),
                    usuario.isBloqueado()
                            ? "<span class='badge badge-bloqueado'>Bloqueado</span>"
                            : "<span class='badge badge-activo'>Activo</span>"
            );

            ctx.html(LayoutUtil.layoutAdmin(
                    "Mi sesión",
                    "Consulta la información del usuario autenticado con permisos de administrador.",
                    contenido
            ));
            return;
        }

        if ("ORGANIZADOR".equals(usuario.getRol().name())) {
            String contenido = """
                <div class="panel">
                    <div class="panel-top"></div>

                    <div class="tabla-wrap">
                        <table>
                            <tbody>
                                <tr>
                                    <th style="width:220px;">ID</th>
                                    <td>%d</td>
                                </tr>
                                <tr>
                                    <th>Nombre</th>
                                    <td>%s</td>
                                </tr>
                                <tr>
                                    <th>Correo</th>
                                    <td>%s</td>
                                </tr>
                                <tr>
                                    <th>Rol</th>
                                    <td><span class="badge badge-organizador">ORGANIZADOR</span></td>
                                </tr>
                                <tr>
                                    <th>Estado</th>
                                    <td>%s</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div style="padding:22px; display:flex; gap:12px; flex-wrap:wrap;">
                        <a href="/organizador/dashboard" class="btn btn-principal">Ir al dashboard</a>
                        <a href="/logout" class="btn btn-alerta">Cerrar sesión</a>
                    </div>
                </div>
                """.formatted(
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getCorreo(),
                    usuario.isBloqueado()
                            ? "<span class='badge badge-bloqueado'>Bloqueado</span>"
                            : "<span class='badge badge-activo'>Activo</span>"
            );

            ctx.html(LayoutUtil.layoutOrganizador(
                    "Mi sesión",
                    "Consulta la información del usuario autenticado con permisos de organizador.",
                    contenido
            ));
            return;
        }

        String contenido = """
            <style>
                .grid-principal {
                    display:grid;
                    grid-template-columns: 0.95fr 1.05fr;
                    gap:22px;
                    align-items:start;
                }

                .panel-info {
                    background: linear-gradient(180deg,#f8fbff,#eef5ff);
                    border: 1px solid #dbe7ff;
                    border-radius: 20px;
                    padding: 24px;
                    box-shadow: 0 10px 24px rgba(13, 110, 253, 0.10);
                }

                .panel-info h2 {
                    margin-top:0;
                    margin-bottom:14px;
                    font-size:28px;
                    color:#0a3d91;
                }

                .panel-info p {
                    margin:0 0 16px 0;
                    font-size:16px;
                    line-height:1.7;
                    color:#374151;
                }

                .panel-info ul {
                    margin:0;
                    padding-left:20px;
                    color:#374151;
                }

                .panel-info li {
                    margin-bottom:12px;
                    font-size:15px;
                    line-height:1.6;
                }

                .nota {
                    margin-top:18px;
                    background:#fff9e8;
                    border-left:5px solid #f4c542;
                    border-radius:14px;
                    padding:14px 15px;
                    color:#5b4a17;
                    font-size:14px;
                    line-height:1.6;
                }

                .panel-datos {
                    background: linear-gradient(180deg,#ffffff,#fffdf7);
                    border: 1px solid #f3e6b2;
                    border-radius: 20px;
                    padding: 24px;
                    box-shadow: inset 0 1px 0 rgba(255,255,255,0.75);
                }

                .panel-datos h2 {
                    margin-top:0;
                    margin-bottom:18px;
                    font-size:28px;
                    color:#111827;
                }

                .datos-grid {
                    display:grid;
                    grid-template-columns: 1fr 1fr;
                    gap:14px;
                    margin-bottom:18px;
                }

                .dato {
                    background:#f8fafc;
                    border:1px solid #e5e7eb;
                    border-radius:16px;
                    padding:16px;
                }

                .dato-titulo {
                    font-size:13px;
                    color:#6b7280;
                    font-weight:bold;
                    text-transform:uppercase;
                    letter-spacing:0.4px;
                    margin-bottom:8px;
                }

                .dato-valor {
                    font-size:17px;
                    color:#111827;
                    font-weight:bold;
                    line-height:1.5;
                    word-break:break-word;
                }

                .acciones {
                    display:flex;
                    gap:12px;
                    flex-wrap:wrap;
                    margin-top:8px;
                }

                @media (max-width: 980px) {
                    .grid-principal {
                        grid-template-columns:1fr;
                    }
                }

                @media (max-width: 680px) {
                    .datos-grid {
                        grid-template-columns:1fr;
                    }
                }

                @media (max-width: 560px) {
                    .panel-info h2,
                    .panel-datos h2 {
                        font-size:24px;
                    }

                    .acciones {
                        flex-direction:column;
                    }

                    .acciones .btn {
                        width:100%%;
                        text-align:center;
                    }
                }
            </style>

            <div class="grid-principal">
                <div class="panel-info">
                    <h2>Mi cuenta de participante</h2>

                    <p>
                        Desde esta sección puedes consultar la información principal de tu sesión activa y verificar que tu cuenta se encuentre correctamente disponible dentro del sistema.
                    </p>

                    <ul>
                        <li><strong>Rol:</strong> participante del sistema académico.</li>
                        <li><strong>Acceso:</strong> eventos publicados, inscripciones y código QR individual.</li>
                        <li><strong>Uso principal:</strong> consultar eventos, inscribirse y mostrar QR para asistencia.</li>
                    </ul>

                    <div class="nota">
                        Recomendación: utiliza la sección de <strong>Mis inscripciones</strong> para mostrar tu QR cuando necesites validar tu asistencia ante el organizador o administrador.
                    </div>
                </div>

                <div class="panel-datos">
                    <h2>Datos de sesión</h2>

                    <div class="datos-grid">
                        <div class="dato">
                            <div class="dato-titulo">ID</div>
                            <div class="dato-valor">%d</div>
                        </div>

                        <div class="dato">
                            <div class="dato-titulo">Estado</div>
                            <div class="dato-valor">%s</div>
                        </div>

                        <div class="dato">
                            <div class="dato-titulo">Nombre</div>
                            <div class="dato-valor">%s</div>
                        </div>

                        <div class="dato">
                            <div class="dato-titulo">Rol</div>
                            <div class="dato-valor"><span class="badge badge-programado">PARTICIPANTE</span></div>
                        </div>

                        <div class="dato" style="grid-column: 1 / -1;">
                            <div class="dato-titulo">Correo</div>
                            <div class="dato-valor">%s</div>
                        </div>
                    </div>

                    <div class="acciones">
                        <a href="/participante/dashboard" class="btn btn-principal">Ir al dashboard</a>
                        <a href="/participante/mis-inscripciones" class="btn btn-secundario">Ver mis inscripciones</a>
                        <a href="/logout" class="btn btn-alerta">Cerrar sesión</a>
                    </div>
                </div>
            </div>
            """.formatted(
                usuario.getId(),
                usuario.isBloqueado()
                        ? "<span class='badge badge-cancelado'>Bloqueado</span>"
                        : "<span class='badge badge-activo'>Activo</span>",
                usuario.getNombre(),
                usuario.getCorreo()
        );

        ctx.html(LayoutUtil.layoutParticipante(
                "Mi sesión",
                "Consulta la información del usuario autenticado y verifica los datos principales de tu cuenta de participante.",
                contenido
        ));
    }


    public void logout(Context ctx) {
        SessionUtil.cerrarSesion(ctx);
        ctx.redirect("/login");
    }

    private String construirHtmlLogin(String mensajeExito, String mensajeError) {
        String html = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Login - Eventos Academicos Pucmm</title>
                <style>
                    %ESTILOS%
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

                        <h1 class="titulo-principal">Bienvenidos a Eventos Academicos Pucmm</h1>
                        <p class="subtitulo">Sistema de gestión y control de eventos académicos para administradores, organizadores y participantes.</p>
                        <div class="linea-dorada"></div>
                    </div>

                    <div class="caja-formulario">
                        <h2 class="titulo-formulario">Iniciar sesión</h2>

                        %MENSAJE_EXITO%
                        %MENSAJE_ERROR%

                        <form method="post" action="/login">
                            <label for="correo">Correo</label>
                            <input id="correo" type="email" name="correo" required>

                            <label for="password">Contraseña</label>
                            <input id="password" type="password" name="password" required>

                            <button type="submit">Entrar</button>
                        </form>

                        <div class="acciones-extra">
                            <span>¿No tienes cuenta?</span>
                            <a href="/registro">Regístrate aquí</a>
                        </div>

                        <div class="info">
                            <strong>Admin por defecto:</strong><br>
                            correo: admin@admin.com<br>
                            clave: admin123
                        </div>

                        <div class="mini-detalles">
                            <span></span>
                            <span></span>
                            <span></span>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """;

        return html
                .replace("%ESTILOS%", obtenerEstilosAuth())
                .replace("%MENSAJE_EXITO%", bloqueMensajeExito(mensajeExito))
                .replace("%MENSAJE_ERROR%", bloqueMensajeError(mensajeError));
    }

    private String construirHtmlRegistro(String nombre, String correo, String mensajeError) {
        String html = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Registro - Eventos Academicos Pucmm</title>
                <style>
                    %ESTILOS%
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

                        <h1 class="titulo-principal">Crear cuenta de participante</h1>
                        <p class="subtitulo">Completa el formulario para registrarte en el sistema y poder inscribirte en eventos académicos.</p>
                        <div class="linea-dorada"></div>
                    </div>

                    <div class="caja-formulario">
                        <h2 class="titulo-formulario">Registro público</h2>

                        <div class="nota-publica">
                            El registro público crea usuarios con rol <strong>PARTICIPANTE</strong>.
                        </div>

                        %MENSAJE_ERROR%

                        <form method="post" action="/registro">
                            <label for="nombre">Nombre</label>
                            <input id="nombre" type="text" name="nombre" value="%NOMBRE%" required>

                            <label for="correo">Correo</label>
                            <input id="correo" type="email" name="correo" value="%CORREO%" required>

                            <label for="password">Contraseña</label>
                            <input id="password" type="password" name="password" required>

                            <label for="confirmarPassword">Confirmar contraseña</label>
                            <input id="confirmarPassword" type="password" name="confirmarPassword" required>

                            <button type="submit">Registrarme</button>
                        </form>

                        <div class="acciones-extra">
                            <span>¿Ya tienes cuenta?</span>
                            <a href="/login">Volver al login</a>
                        </div>

                        <div class="mini-detalles">
                            <span></span>
                            <span></span>
                            <span></span>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """;

        return html
                .replace("%ESTILOS%", obtenerEstilosAuth())
                .replace("%MENSAJE_ERROR%", bloqueMensajeError(mensajeError))
                .replace("%NOMBRE%", escaparHtml(nombre))
                .replace("%CORREO%", escaparHtml(correo));
    }

    private String obtenerEstilosAuth() {
        return """
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            min-height: 100vh;
            font-family: Arial, sans-serif;
            background:
                radial-gradient(circle at 10% 10%, rgba(255, 214, 77, 0.25), transparent 18%),
                radial-gradient(circle at 90% 85%, rgba(255, 214, 77, 0.18), transparent 20%),
                linear-gradient(135deg, #0a3d91, #0d47a1 55%, #1565c0);
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 34px 20px 40px;
        }

        .contenedor {
            position: relative;
            width: 100%;
            max-width: 640px;
            background: #ffffff;
            border-radius: 24px;
            box-shadow: 0 24px 50px rgba(0, 0, 0, 0.24);
            padding: 34px 32px 28px;
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
            width: 90px;
            height: 90px;
            border-radius: 50%;
            background: rgba(244, 197, 66, 0.12);
            top: -20px;
            right: -20px;
        }

        .decoracion-circulo-2 {
            position: absolute;
            width: 55px;
            height: 55px;
            border-radius: 50%;
            background: rgba(244, 197, 66, 0.18);
            top: 28px;
            right: 55px;
        }

        .encabezado {
            position: relative;
            z-index: 1;
            margin-bottom: 24px;
        }

        .logo-bloque {
            width: 92px;
            height: 92px;
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
            text-align: center;
        }

        .logo-bloque img {
            width: 68px;
            height: 68px;
            object-fit: contain;
            display: block;
        }

        .titulo-principal {
            margin: 0;
            font-size: 38px;
            line-height: 1.15;
            color: #0a3d91;
            font-weight: bold;
        }

        .subtitulo {
            margin: 12px 0 0 0;
            font-size: 18px;
            color: #4b5563;
            line-height: 1.6;
            max-width: 520px;
        }

        .linea-dorada {
            width: 130px;
            height: 6px;
            border-radius: 999px;
            background: linear-gradient(90deg, #f4c542, #ffd95a);
            margin-top: 18px;
        }

        .caja-formulario {
            position: relative;
            z-index: 1;
            background: linear-gradient(180deg, #ffffff, #fffdf7);
            border: 1px solid #f3e6b2;
            border-radius: 20px;
            padding: 28px 24px 22px;
            box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.75);
        }

        .titulo-formulario {
            margin: 0 0 22px 0;
            font-size: 29px;
            text-align: center;
            color: #111827;
        }

        label {
            display: block;
            margin-bottom: 8px;
            font-size: 15px;
            font-weight: bold;
            color: #1f2937;
        }

        input {
            width: 100%;
            padding: 15px 16px;
            margin-bottom: 18px;
            border: 1px solid #d7dde7;
            border-radius: 12px;
            font-size: 16px;
            outline: none;
            background: #ffffff;
            transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
        }

        input:focus {
            border-color: #f4c542;
            box-shadow: 0 0 0 4px rgba(244, 197, 66, 0.18);
            transform: translateY(-1px);
        }

        button {
            width: 100%;
            padding: 15px;
            border: none;
            border-radius: 12px;
            background: linear-gradient(90deg, #0a3d91, #174ea6);
            color: white;
            font-size: 17px;
            font-weight: bold;
            cursor: pointer;
            box-shadow: 0 10px 22px rgba(10, 61, 145, 0.22);
            transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
        }

        button:hover {
            transform: translateY(-1px);
            opacity: 0.97;
        }

        .info {
            margin-top: 20px;
            background: linear-gradient(90deg, #fff9e8, #fffdf7);
            border-left: 5px solid #f4c542;
            border-radius: 14px;
            padding: 15px 16px;
            color: #374151;
            font-size: 15px;
            line-height: 1.6;
        }

        .info strong {
            color: #111827;
        }

        .nota-publica {
            margin-bottom: 18px;
            background: linear-gradient(90deg, #fff9e8, #fffdf7);
            border-left: 5px solid #f4c542;
            border-radius: 14px;
            padding: 15px 16px;
            color: #374151;
            font-size: 15px;
            line-height: 1.6;
        }

        .mensaje-exito {
            margin-bottom: 18px;
            background: #ecfdf3;
            border-left: 5px solid #22c55e;
            color: #166534;
            border-radius: 14px;
            padding: 15px 16px;
            font-size: 15px;
            line-height: 1.6;
        }

        .mensaje-error {
            margin-bottom: 18px;
            background: #fef2f2;
            border-left: 5px solid #ef4444;
            color: #991b1b;
            border-radius: 14px;
            padding: 15px 16px;
            font-size: 15px;
            line-height: 1.6;
        }

        .acciones-extra {
            margin-top: 18px;
            text-align: center;
            font-size: 15px;
            color: #374151;
        }

        .acciones-extra a {
            color: #0a3d91;
            font-weight: bold;
            text-decoration: none;
            margin-left: 6px;
        }

        .acciones-extra a:hover {
            text-decoration: underline;
        }

        .mini-detalles {
            display: flex;
            gap: 8px;
            margin-top: 18px;
            justify-content: center;
        }

        .mini-detalles span {
            width: 10px;
            height: 10px;
            border-radius: 50%;
            background: #f4c542;
            opacity: 0.9;
        }

        @media (max-width: 720px) {
            .contenedor {
                max-width: 580px;
            }

            .titulo-principal {
                font-size: 32px;
            }

            .subtitulo {
                font-size: 16px;
            }

            .titulo-formulario {
                font-size: 25px;
            }
        }

        @media (max-width: 560px) {
            .contenedor {
                padding: 24px 18px 20px;
            }

            .logo-bloque {
                width: 82px;
                height: 82px;
            }

            .logo-bloque img {
                width: 60px;
                height: 60px;
            }

            .titulo-principal {
                font-size: 28px;
            }

            .subtitulo {
                font-size: 15px;
            }

            .titulo-formulario {
                font-size: 23px;
            }

            input,
            button {
                font-size: 15px;
            }

            .acciones-extra,
            .info,
            .nota-publica,
            .mensaje-exito,
            .mensaje-error {
                font-size: 14px;
            }
        }
        """;
    }

    private String bloqueMensajeExito(String mensaje) {
        if (mensaje == null || mensaje.isBlank()) {
            return "";
        }
        return "<div class='mensaje-exito'>" + escaparHtml(mensaje) + "</div>";
    }

    private String bloqueMensajeError(String mensaje) {
        if (mensaje == null || mensaje.isBlank()) {
            return "";
        }
        return "<div class='mensaje-error'>" + escaparHtml(mensaje) + "</div>";
    }

    private String escaparHtml(String texto) {
        if (texto == null) {
            return "";
        }

        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String valorSeguro(String texto) {
        return texto == null ? "" : texto.trim();
    }
}