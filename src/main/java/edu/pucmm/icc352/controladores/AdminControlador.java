package edu.pucmm.icc352.controladores;

import edu.pucmm.icc352.modelos.Evento;
import edu.pucmm.icc352.modelos.Rol;
import edu.pucmm.icc352.modelos.Usuario;
import edu.pucmm.icc352.servicios.EventoServicio;
import edu.pucmm.icc352.servicios.UsuarioServicio;
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

        String html = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <title>Dashboard Admin</title>
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
                    <h1>Dashboard del Administrador</h1>
                    <p>Bienvenido, administrador.</p>

                    <a href="/admin/usuarios">Gestionar usuarios</a>
                    <a href="/admin/usuarios/nuevo">Registrar usuario</a>
                    <a href="/admin/eventos">Gestionar eventos</a>
                    <a href="/asistencia/registrar">Registrar asistencia</a>
                    <a href="/me" class="secundario">Ver mi sesión</a>
                    <a href="/logout" class="secundario">Cerrar sesión</a>
                </div>
            </body>
            </html>
            """;

        ctx.html(html);
    }

    public void listarUsuarios(Context ctx) {
        if (!validarAdmin(ctx)) {
            return;
        }

        List<Usuario> usuarios = usuarioServicio.listarTodos();

        StringBuilder filas = new StringBuilder();

        for (Usuario usuario : usuarios) {
            String accion;

            if (usuario.isBloqueado()) {
                accion = "<a href='/admin/usuarios/desbloquear/" + usuario.getId() + "'>Desbloquear</a>";
            } else {
                accion = "<a href='/admin/usuarios/bloquear/" + usuario.getId() + "'>Bloquear</a>";
            }

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
                    usuario.getRol().name(),
                    usuario.isBloqueado() ? "Sí" : "No",
                    accion
            ));
        }

        String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>Usuarios</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            background: #f4f4f4;
                            margin: 0;
                            padding: 40px;
                        }
                        .contenedor {
                            max-width: 1000px;
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
                        <h1>Gestión de usuarios</h1>
                        <a href="/admin/usuarios/nuevo">Registrar usuario</a>

                        <table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Nombre</th>
                                    <th>Correo</th>
                                    <th>Rol</th>
                                    <th>Bloqueado</th>
                                    <th>Acción</th>
                                </tr>
                            </thead>
                            <tbody>
                                %s
                            </tbody>
                        </table>

                        <a href="/admin/dashboard" class="volver">Volver al dashboard</a>
                    </div>
                </body>
                </html>
                """.formatted(filas);

        ctx.html(html);
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
                        body {
                            font-family: Arial, sans-serif;
                            background: #f4f4f4;
                            margin: 0;
                            padding: 40px;
                        }
                        .contenedor {
                            max-width: 500px;
                            margin: auto;
                            background: white;
                            padding: 24px;
                            border-radius: 10px;
                            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        }
                        input, select {
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
                        <h1>Registrar usuario</h1>
                        <form method="post" action="/admin/usuarios/nuevo">
                            <label>Nombre</label>
                            <input type="text" name="nombre" required>

                            <label>Correo</label>
                            <input type="email" name="correo" required>

                            <label>Contraseña</label>
                            <input type="password" name="password" required>

                            <label>Rol</label>
                            <select name="rol" required>
                                <option value="ORGANIZADOR">ORGANIZADOR</option>
                                <option value="PARTICIPANTE">PARTICIPANTE</option>
                                <option value="ADMIN">ADMIN</option>
                            </select>

                            <button type="submit">Guardar usuario</button>
                            <a href="/admin/usuarios" class="secundario">Volver</a>
                        </form>
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
            ctx.status(400).html("""
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <title>Error</title>
                    </head>
                    <body style="font-family: Arial, sans-serif; padding: 40px;">
                        <h2>Error al registrar usuario</h2>
                        <p>%s</p>
                        <a href="/admin/usuarios/nuevo">Volver</a>
                    </body>
                    </html>
                    """.formatted(e.getMessage()));
        }
    }

    public void listarEventos(Context ctx) {
        if (!validarAdmin(ctx)) {
            return;
        }

        List<Evento> eventos = eventoServicio.listarTodos();
        StringBuilder filas = new StringBuilder();

        for (Evento evento : eventos) {
            String organizador = evento.getOrganizador() != null ? evento.getOrganizador().getNombre() : "Sin organizador";

            filas.append("""
                    <tr>
                        <td>%d</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td><a href='/admin/eventos/eliminar/%d'>Eliminar</a></td>
                    </tr>
                    """.formatted(
                    evento.getId(),
                    evento.getTitulo(),
                    evento.getFecha(),
                    evento.getHora(),
                    organizador,
                    evento.isPublicado() ? "Sí" : "No",
                    evento.isCancelado() ? "Sí" : "No",
                    evento.getId()
            ));
        }

        String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>Eventos</title>
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
                        }
                        .volver {
                            margin-top: 20px;
                            background: #6c757d;
                        }
                    </style>
                </head>
                <body>
                    <div class="contenedor">
                        <h1>Gestión de eventos</h1>

                        <table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Título</th>
                                    <th>Fecha</th>
                                    <th>Hora</th>
                                    <th>Organizador</th>
                                    <th>Publicado</th>
                                    <th>Cancelado</th>
                                    <th>Acción</th>
                                </tr>
                            </thead>
                            <tbody>
                                %s
                            </tbody>
                        </table>

                        <a href="/admin/dashboard" class="volver">Volver al dashboard</a>
                    </div>
                </body>
                </html>
                """.formatted(filas);

        ctx.html(html);
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