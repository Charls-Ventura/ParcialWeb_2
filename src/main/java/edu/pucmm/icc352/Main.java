package edu.pucmm.icc352;

import edu.pucmm.icc352.config.HibernateUtil;
import edu.pucmm.icc352.controladores.AdminControlador;
import edu.pucmm.icc352.controladores.AsistenciaControlador;
import edu.pucmm.icc352.controladores.AuthControlador;
import edu.pucmm.icc352.controladores.OrganizadorControlador;
import edu.pucmm.icc352.controladores.ParticipanteControlador;
import edu.pucmm.icc352.servicios.InicializadorServicio;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;


import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {

        HibernateUtil.getSessionFactory();
        new InicializadorServicio().crearAdminSiNoExiste();

        AuthControlador authControlador = new AuthControlador();
        AdminControlador adminControlador = new AdminControlador();
        OrganizadorControlador organizadorControlador = new OrganizadorControlador();
        ParticipanteControlador participanteControlador = new ParticipanteControlador();
        AsistenciaControlador asistenciaControlador = new AsistenciaControlador();

        Javalin app = Javalin.create(config -> {
            config.startup.showJavalinBanner = false;

            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "public";
                staticFiles.location = Location.CLASSPATH;
            });

            config.routes.get("/", ctx ->
                    ctx.redirect("/login"));

            config.routes.get("/ping", ctx ->
                    ctx.result("pong"));

            config.routes.get("/logo-pucmm", ctx -> {
                Path rutaLogo = Path.of("src", "main", "resources", "public", "img", "logo-pucmm.png");

                if (!Files.exists(rutaLogo)) {
                    ctx.status(404).result("Logo no encontrado");
                    return;
                }


                InputStream inputStream = Files.newInputStream(rutaLogo);
                ctx.contentType("image/png");
                ctx.result(inputStream);
            });

            config.routes.get("/login", authControlador::mostrarLogin);
            config.routes.post("/login", authControlador::procesarLogin);

            config.routes.get("/registro", authControlador::mostrarFormularioRegistro);
            config.routes.post("/registro", authControlador::procesarRegistro);

            config.routes.get("/me", authControlador::verSesionActual);
            config.routes.get("/logout", authControlador::logout);

            config.routes.get("/admin/dashboard", adminControlador::dashboard);
            config.routes.get("/admin/usuarios", adminControlador::listarUsuarios);
            config.routes.get("/admin/usuarios/nuevo", adminControlador::mostrarFormularioNuevoUsuario);
            config.routes.post("/admin/usuarios/nuevo", adminControlador::guardarNuevoUsuario);
            config.routes.get("/admin/usuarios/bloquear/{id}", adminControlador::bloquearUsuario);
            config.routes.get("/admin/usuarios/desbloquear/{id}", adminControlador::desbloquearUsuario);
            config.routes.get("/admin/eventos", adminControlador::listarEventos);
            config.routes.get("/admin/eventos/eliminar/{id}", adminControlador::eliminarEvento);

            config.routes.get("/organizador/dashboard", organizadorControlador::dashboard);
            config.routes.get("/organizador/eventos", organizadorControlador::listarMisEventos);
            config.routes.get("/organizador/eventos/nuevo", organizadorControlador::mostrarFormularioNuevoEvento);
            config.routes.post("/organizador/eventos/nuevo", organizadorControlador::guardarNuevoEvento);
            config.routes.get("/organizador/eventos/editar/{id}", organizadorControlador::mostrarFormularioEditarEvento);
            config.routes.post("/organizador/eventos/editar/{id}", organizadorControlador::guardarEdicionEvento);
            config.routes.get("/organizador/eventos/publicar/{id}", organizadorControlador::publicarEvento);
            config.routes.get("/organizador/eventos/despublicar/{id}", organizadorControlador::despublicarEvento);
            config.routes.get("/organizador/eventos/cancelar/{id}", organizadorControlador::cancelarEvento);

            config.routes.get("/participante/dashboard", participanteControlador::dashboard);
            config.routes.get("/participante/eventos", participanteControlador::listarEventosPublicados);
            config.routes.get("/participante/eventos/inscribirse/{id}", participanteControlador::inscribirseEvento);
            config.routes.get("/participante/mis-inscripciones", participanteControlador::listarMisInscripciones);
            config.routes.get("/participante/inscripciones/cancelar/{id}", participanteControlador::cancelarInscripcion);
            config.routes.post("/participante/validar-qr", participanteControlador::validarQrPropio);

            config.routes.get("/asistencia/escanear", asistenciaControlador::mostrarEscanerQr);
            config.routes.get("/asistencia/registrar", asistenciaControlador::mostrarFormularioEscaneo);
            config.routes.post("/asistencia/registrar", asistenciaControlador::registrarAsistencia);
            config.routes.get("/asistencia/evento/{id}", asistenciaControlador::verInscritosEvento);
            config.routes.get("/asistencia/registrar-inscripcion/{id}", asistenciaControlador::registrarAsistenciaPorInscripcion);
            config.routes.get("/eventos/resumen/{id}", asistenciaControlador::verResumenEvento);
            config.routes.get("/asistencia/validar-inscripcion-qr/{id}", asistenciaControlador::mostrarValidadorQrInscripcion);
            config.routes.post("/asistencia/validar-inscripcion-qr/{id}", asistenciaControlador::validarQrContraInscripcion);

        });

        app.start(7000);

        System.out.println("Servidor iniciado en http://localhost:7000");
    }
}