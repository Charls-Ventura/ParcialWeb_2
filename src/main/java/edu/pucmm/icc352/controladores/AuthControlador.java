package edu.pucmm.icc352.controladores;

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

        String html = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <title>Login</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        background: #f4f4f4;
                        margin: 0;
                        padding: 40px;
                    }
                    .contenedor {
                        max-width: 420px;
                        margin: auto;
                        background: white;
                        padding: 24px;
                        border-radius: 10px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    h1 {
                        margin-top: 0;
                    }
                    input {
                        width: 100%;
                        padding: 10px;
                        margin-top: 8px;
                        margin-bottom: 16px;
                        box-sizing: border-box;
                    }
                    button {
                        width: 100%;
                        padding: 10px;
                        border: none;
                        background: #0d6efd;
                        color: white;
                        border-radius: 6px;
                        cursor: pointer;
                    }
                    .info {
                        margin-top: 16px;
                        font-size: 14px;
                        color: #444;
                        background: #f8f9fa;
                        padding: 12px;
                        border-radius: 6px;
                    }
                </style>
            </head>
            <body>
                <div class="contenedor">
                    <h1>Iniciar sesión</h1>
                    <form method="post" action="/login">
                        <label>Correo</label>
                        <input type="email" name="correo" required>

                        <label>Contraseña</label>
                        <input type="password" name="password" required>

                        <button type="submit">Entrar</button>
                    </form>

                    <div class="info">
                        <strong>Admin por defecto:</strong><br>
                        correo: admin@admin.com<br>
                        clave: admin123
                    </div>
                </div>
            </body>
            </html>
            """;

        ctx.html(html);
    }

    public void procesarLogin(Context ctx) {
        try {
            String correo = ctx.formParam("correo");
            String password = ctx.formParam("password");

            Usuario usuario = usuarioServicio.autenticar(correo, password);
            SessionUtil.guardarUsuarioEnSesion(ctx, usuario);

            ctx.redirect("/me");
        } catch (Exception e) {
            String html = """
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <title>Error de login</title>
                    </head>
                    <body style="font-family: Arial, sans-serif; padding: 40px;">
                        <h2>Error al iniciar sesión</h2>
                        <p>%s</p>
                        <a href="/login">Volver al login</a>
                    </body>
                    </html>
                    """.formatted(e.getMessage());

            ctx.status(401).html(html);
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
}