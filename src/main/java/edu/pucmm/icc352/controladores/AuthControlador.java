package edu.pucmm.icc352.controladores;

import edu.pucmm.icc352.modelos.Rol;
import edu.pucmm.icc352.modelos.Usuario;
import edu.pucmm.icc352.servicios.UsuarioServicio;
import edu.pucmm.icc352.utilidades.SessionUtil;
import io.javalin.http.Context;

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

        String enlacePanel = "";
        if ("ADMIN".equals(usuario.getRol().name())) {
            enlacePanel = "<a href='/admin/dashboard'>Ir al panel admin</a>";
        } else if ("ORGANIZADOR".equals(usuario.getRol().name())) {
            enlacePanel = "<a href='/organizador/dashboard'>Ir al panel organizador</a>";
        } else if ("PARTICIPANTE".equals(usuario.getRol().name())) {
            enlacePanel = "<a href='/participante/dashboard'>Ir al panel participante</a>";
        }

        String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>Sesión actual</title>
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
                        a {
                            display: inline-block;
                            margin-top: 12px;
                            margin-right: 10px;
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
                        <h1>Usuario autenticado</h1>
                        <p><strong>ID:</strong> %d</p>
                        <p><strong>Nombre:</strong> %s</p>
                        <p><strong>Correo:</strong> %s</p>
                        <p><strong>Rol:</strong> %s</p>
                        <p><strong>Bloqueado:</strong> %s</p>

                        %s
                        <a href="/logout" class="secundario">Cerrar sesión</a>
                        <a href="/">Ir al inicio</a>
                    </div>
                </body>
                </html>
                """.formatted(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol().name(),
                usuario.isBloqueado() ? "Sí" : "No",
                enlacePanel
        );

        ctx.html(html);
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
                padding: 30px 20px;
            }

            .contenedor {
                position: relative;
                width: 100%;
                max-width: 510px;
                background: #ffffff;
                border-radius: 22px;
                box-shadow: 0 24px 50px rgba(0, 0, 0, 0.24);
                padding: 30px 28px 24px;
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
                margin-bottom: 22px;
            }

            .logo-bloque {
                width: 86px;
                height: 86px;
                border-radius: 18px;
                background: linear-gradient(180deg, #fff8e1, #fffdf5);
                border: 2px solid #f4d56a;
                display: flex;
                align-items: center;
                justify-content: center;
                margin-bottom: 14px;
                box-shadow: 0 8px 18px rgba(244, 197, 66, 0.22);
                color: #0a3d91;
                font-weight: bold;
                font-size: 18px;
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
                font-size: 31px;
                line-height: 1.18;
                color: #0a3d91;
                font-weight: bold;
            }

            .subtitulo {
                margin: 10px 0 0 0;
                font-size: 15px;
                color: #4b5563;
                line-height: 1.5;
                max-width: 430px;
            }

            .linea-dorada {
                width: 110px;
                height: 5px;
                border-radius: 999px;
                background: linear-gradient(90deg, #f4c542, #ffd95a);
                margin-top: 18px;
            }

            .caja-formulario {
                position: relative;
                z-index: 1;
                background: linear-gradient(180deg, #ffffff, #fffdf7);
                border: 1px solid #f3e6b2;
                border-radius: 18px;
                padding: 24px 20px 20px;
                box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.75);
            }

            .titulo-formulario {
                margin: 0 0 20px 0;
                font-size: 24px;
                text-align: center;
                color: #111827;
            }

            label {
                display: block;
                margin-bottom: 7px;
                font-size: 14px;
                font-weight: bold;
                color: #1f2937;
            }

            input {
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

            input:focus {
                border-color: #f4c542;
                box-shadow: 0 0 0 4px rgba(244, 197, 66, 0.18);
                transform: translateY(-1px);
            }

            button {
                width: 100%;
                padding: 14px;
                border: none;
                border-radius: 12px;
                background: linear-gradient(90deg, #0a3d91, #174ea6);
                color: white;
                font-size: 16px;
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
                margin-top: 18px;
                background: linear-gradient(90deg, #fff9e8, #fffdf7);
                border-left: 5px solid #f4c542;
                border-radius: 14px;
                padding: 14px 15px;
                color: #374151;
                font-size: 14px;
            }

            .info strong {
                color: #111827;
            }

            .nota-publica {
                margin-bottom: 18px;
                background: linear-gradient(90deg, #fff9e8, #fffdf7);
                border-left: 5px solid #f4c542;
                border-radius: 14px;
                padding: 14px 15px;
                color: #374151;
                font-size: 14px;
            }

            .mensaje-exito {
                margin-bottom: 18px;
                background: #ecfdf3;
                border-left: 5px solid #22c55e;
                color: #166534;
                border-radius: 14px;
                padding: 14px 15px;
                font-size: 14px;
            }

            .mensaje-error {
                margin-bottom: 18px;
                background: #fef2f2;
                border-left: 5px solid #ef4444;
                color: #991b1b;
                border-radius: 14px;
                padding: 14px 15px;
                font-size: 14px;
            }

            .acciones-extra {
                margin-top: 16px;
                text-align: center;
                font-size: 14px;
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
                margin-top: 16px;
                justify-content: center;
            }

            .mini-detalles span {
                width: 10px;
                height: 10px;
                border-radius: 50%;
                background: #f4c542;
                opacity: 0.9;
            }

            @media (max-width: 560px) {
                .contenedor {
                    padding: 26px 18px 20px;
                }

                .logo-bloque {
                    width: 76px;
                    height: 76px;
                }

                .logo-bloque img {
                    width: 56px;
                    height: 56px;
                }

                .titulo-principal {
                    font-size: 26px;
                }

                .subtitulo {
                    font-size: 14px;
                }

                .caja-formulario {
                    padding: 20px 16px 18px;
                }

                .titulo-formulario {
                    font-size: 22px;
                }
            }

            @media (max-width: 400px) {
                .titulo-principal {
                    font-size: 23px;
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