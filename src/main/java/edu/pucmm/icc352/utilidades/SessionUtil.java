package edu.pucmm.icc352.utilidades;

import edu.pucmm.icc352.modelos.Usuario;
import io.javalin.http.Context;

public class SessionUtil {

    private static final String USUARIO_ID = "usuarioId";
    private static final String USUARIO_NOMBRE = "usuarioNombre";
    private static final String USUARIO_ROL = "usuarioRol";

    public static void guardarUsuarioEnSesion(Context ctx, Usuario usuario) {
        ctx.sessionAttribute(USUARIO_ID, usuario.getId());
        ctx.sessionAttribute(USUARIO_NOMBRE, usuario.getNombre());
        ctx.sessionAttribute(USUARIO_ROL, usuario.getRol().name());
    }

    public static Long obtenerUsuarioId(Context ctx) {
        return ctx.sessionAttribute(USUARIO_ID);
    }

    public static String obtenerUsuarioNombre(Context ctx) {
        return ctx.sessionAttribute(USUARIO_NOMBRE);
    }

    public static String obtenerUsuarioRol(Context ctx) {
        return ctx.sessionAttribute(USUARIO_ROL);
    }

    public static boolean estaLogueado(Context ctx) {
        return obtenerUsuarioId(ctx) != null;
    }

    public static boolean esAdmin(Context ctx) {
        String rol = obtenerUsuarioRol(ctx);
        return "ADMIN".equals(rol);
    }

    public static boolean esOrganizador(Context ctx) {
        String rol = obtenerUsuarioRol(ctx);
        return "ORGANIZADOR".equals(rol);
    }

    public static boolean esParticipante(Context ctx) {
        String rol = obtenerUsuarioRol(ctx);
        return "PARTICIPANTE".equals(rol);
    }

    public static void cerrarSesion(Context ctx) {
        ctx.sessionAttribute(USUARIO_ID, null);
        ctx.sessionAttribute(USUARIO_NOMBRE, null);
        ctx.sessionAttribute(USUARIO_ROL, null);
    }
}