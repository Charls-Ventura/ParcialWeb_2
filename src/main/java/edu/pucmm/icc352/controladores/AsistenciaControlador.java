package edu.pucmm.icc352.controladores;

import edu.pucmm.icc352.modelos.Evento;
import edu.pucmm.icc352.servicios.AsistenciaServicio;
import edu.pucmm.icc352.servicios.EventoServicio;
import edu.pucmm.icc352.servicios.InscripcionServicio;
import edu.pucmm.icc352.utilidades.LayoutUtil;
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

    private String aplicarLayoutSegunRol(Context ctx, String titulo, String subtitulo, String contenido) {
        if (SessionUtil.esAdmin(ctx)) {
            return LayoutUtil.layoutAdmin(titulo, subtitulo, contenido);
        }

        return LayoutUtil.layoutOrganizador(titulo, subtitulo, contenido);
    }


    public void mostrarFormularioEscaneo(Context ctx) {
        if (!validarOrganizadorOAdmin(ctx)) {
            return;
        }

        String rutaVolver = SessionUtil.esAdmin(ctx)
                ? "/admin/dashboard"
                : "/organizador/dashboard";

        String rutaEventos = SessionUtil.esAdmin(ctx)
                ? "/admin/eventos"
                : "/organizador/eventos";

        String contenido = """
            <style>
                .contenido-grid {
                    display: grid;
                    grid-template-columns: 0.95fr 1.05fr;
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

                input {
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

                input:focus {
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

                .acceso-alternativo {
                    margin-bottom: 18px;
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

                    .acciones .btn,
                    .acceso-alternativo .btn {
                        width: 100%%;
                        text-align: center;
                    }
                }
            </style>

            <div class="contenido-grid">
                <div class="panel-info">
                    <h2>Control de asistencia</h2>

                    <p>
                        Desde esta sección puedes validar la entrada o presencia de un participante utilizando el token asociado a su inscripción.
                    </p>

                    <ul>
                        <li><strong>Token QR:</strong> identifica de forma única la inscripción del participante.</li>
                        <li><strong>Validación automática:</strong> el sistema comprueba estado del evento, inscripción y fecha.</li>
                        <li><strong>Doble asistencia:</strong> se evita registrar dos veces a la misma persona.</li>
                    </ul>

                    <div class="nota">
                        También puedes registrar asistencia desde la lista de inscritos de un evento, sin necesidad de escribir el token manualmente.
                    </div>
                </div>

                <div class="panel-formulario">
                    <h2 class="titulo-formulario">Registrar asistencia</h2>

                    <div class="acceso-alternativo">
                        <a href="%s" class="btn btn-secundario">Ir a eventos y registrar desde inscritos</a>
                    </div>

                    <form method="post" action="/asistencia/registrar">
                        <label for="tokenQr">Token QR</label>
                        <input id="tokenQr" type="text" name="tokenQr" required>

                        <div class="acciones">
                                <button type="submit" class="btn btn-principal">Registrar asistencia</button>
                                <a href="/asistencia/escanear" class="btn btn-principal">Escanear con cámara</a>
                                <a href="%s" class="btn btn-secundario">Volver</a>
                            </div>
                    </form>
                </div>
            </div>
            """.formatted(rutaEventos, rutaVolver);

        ctx.html(aplicarLayoutSegunRol(
                ctx,
                "Registrar asistencia",
                "Valida participantes mediante token QR o accede al registro desde la lista de inscritos de cada evento.",
                contenido
        ));
    }

    public void mostrarEscanerQr(Context ctx) {
        if (!validarOrganizadorOAdmin(ctx)) {
            return;
        }

        String rutaVolver = SessionUtil.esAdmin(ctx)
                ? "/admin/dashboard"
                : "/organizador/dashboard";

        String contenido = """
            <style>
                .contenido-grid {
                    display: grid;
                    grid-template-columns: 0.95fr 1.05fr;
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

                .panel-escaner {
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

                #reader {
                    width: 100%%;
                    border-radius: 16px;
                    overflow: hidden;
                    border: 1px solid #d7dde7;
                    background: #f8fafc;
                    min-height: 280px;
                }

                .acciones {
                    display: flex;
                    gap: 12px;
                    flex-wrap: wrap;
                    margin-top: 18px;
                }

                .estado-lector {
                    margin-top: 16px;
                    font-size: 14px;
                    color: #4b5563;
                    line-height: 1.6;
                    background: #f8fafc;
                    border-radius: 12px;
                    padding: 12px 14px;
                }

                .token-box {
                    margin-top: 16px;
                    background: #fff9e8;
                    border: 1px solid #f3e6b2;
                    border-radius: 12px;
                    padding: 14px;
                }

                .token-box strong {
                    color: #8a6200;
                }

                #tokenDetectado {
                    margin-top: 8px;
                    font-family: monospace;
                    font-size: 13px;
                    word-break: break-word;
                    color: #374151;
                }

                .btn-escaneo {
                    display: inline-block;
                    border: none;
                    text-decoration: none;
                    padding: 13px 18px;
                    border-radius: 10px;
                    font-weight: bold;
                    font-size: 15px;
                    cursor: pointer;
                }

                .btn-iniciar {
                    background: linear-gradient(90deg,#0a3d91,#174ea6);
                    color: white;
                }

                .btn-detener {
                    background: #6c757d;
                    color: white;
                }

                .btn-volver {
                    background: #ef4444;
                    color: white;
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

                    .acciones .btn-escaneo {
                        width: 100%%;
                        text-align: center;
                    }
                }
            </style>

            <div class="contenido-grid">
                <div class="panel-info">
                    <h2>Escaneo con cámara</h2>

                    <p>
                        Utiliza la cámara del dispositivo para leer el código QR del participante y registrar su asistencia automáticamente.
                    </p>

                    <ul>
                        <li><strong>Permiso de cámara:</strong> el navegador solicitará acceso cuando inicies el lector.</li>
                        <li><strong>Lectura automática:</strong> al detectar un QR válido, el sistema enviará el contenido al backend.</li>
                        <li><strong>Registro directo:</strong> se reutiliza la misma lógica de asistencia del sistema.</li>
                    </ul>

                    <div class="nota">
                        Instrucción para demostración: presiona <strong>Iniciar cámara</strong>, muestra el QR del participante y espera la lectura automática. Si el código es válido, se registrará la asistencia y se mostrará la confirmación.
                    </div>
                </div>

                <div class="panel-escaner">
                    <h2 class="titulo-formulario">Escanear QR</h2>

                    <div id="reader"></div>

                    <div class="estado-lector" id="estadoLector">
                        El lector está listo. Presiona <strong>Iniciar cámara</strong> para comenzar.
                    </div>

                    <div class="token-box">
                        <strong>Último contenido detectado:</strong>
                        <div id="tokenDetectado">Aún no se ha detectado ningún QR.</div>
                    </div>

                    <div class="acciones">
                        <button type="button" class="btn-escaneo btn-iniciar" onclick="iniciarEscaneo()">Iniciar cámara</button>
                        <button type="button" class="btn-escaneo btn-detener" onclick="detenerEscaneo()">Detener cámara</button>
                        <a href="%s" class="btn-escaneo btn-volver">Volver</a>
                    </div>

                    <form id="formEscaneo" method="post" action="/asistencia/registrar" style="display:none;">
                        <input type="hidden" name="tokenQr" id="tokenQrInput">
                    </form>
                </div>
            </div>

            <script src="https://unpkg.com/html5-qrcode" type="text/javascript"></script>
            <script>
                let html5QrCode = null;
                let lectorActivo = false;

                function actualizarEstado(texto) {
                    document.getElementById("estadoLector").innerHTML = texto;
                }

                function iniciarEscaneo() {
                    if (lectorActivo) {
                        actualizarEstado("La cámara ya está activa.");
                        return;
                    }

                    html5QrCode = new Html5Qrcode("reader");

                    Html5Qrcode.getCameras().then(cameras => {
                        if (!cameras || cameras.length === 0) {
                            actualizarEstado("No se encontró ninguna cámara disponible.");
                            return;
                        }

                        const camara = cameras[0].id;

                        html5QrCode.start(
                            camara,
                            {
                                fps: 10,
                                qrbox: { width: 220, height: 220 }
                            },
                            (decodedText, decodedResult) => {
                                document.getElementById("tokenDetectado").textContent = decodedText;
                                document.getElementById("tokenQrInput").value = decodedText;
                                actualizarEstado("QR detectado correctamente. Enviando para registrar asistencia...");

                                detenerEscaneo(true).then(() => {
                                    document.getElementById("formEscaneo").submit();
                                });
                            },
                            (errorMessage) => {
                            }
                        ).then(() => {
                            lectorActivo = true;
                            actualizarEstado("Cámara activa. Muestra el QR frente al lector para registrar asistencia.");
                        }).catch(err => {
                            actualizarEstado("No fue posible iniciar la cámara: " + err);
                        });

                    }).catch(err => {
                        actualizarEstado("Error obteniendo cámaras: " + err);
                    });
                }

                function detenerEscaneo(interno = false) {
                    if (!html5QrCode || !lectorActivo) {
                        if (!interno) {
                            actualizarEstado("La cámara no está activa.");
                        }
                        return Promise.resolve();
                    }

                    return html5QrCode.stop()
                        .then(() => {
                            lectorActivo = false;
                            if (!interno) {
                                actualizarEstado("La cámara fue detenida correctamente.");
                            }
                        })
                        .catch(err => {
                            if (!interno) {
                                actualizarEstado("No fue posible detener la cámara: " + err);
                            }
                        });
                }
            </script>
            """.formatted(rutaVolver);

        ctx.html(aplicarLayoutSegunRol(
                ctx,
                "Escanear QR",
                "Utiliza la cámara del dispositivo para leer códigos QR y registrar asistencia de participantes.",
                contenido
        ));
    }



    public void registrarAsistencia(Context ctx) {
        if (!validarOrganizadorOAdmin(ctx)) {
            return;
        }

        try {
            String tokenQr = ctx.formParam("tokenQr");
            var asistencia = asistenciaServicio.registrarAsistenciaPorTokenQr(tokenQr);

            String contenido = """
                <style>
                    .panel-exito {
                        background: linear-gradient(180deg,#ffffff,#fffdf7);
                        border: 1px solid #f3e6b2;
                        border-radius: 20px;
                        padding: 28px 24px;
                        box-shadow: inset 0 1px 0 rgba(255,255,255,0.75);
                    }

                    .titulo-exito {
                        margin: 0 0 14px 0;
                        font-size: 30px;
                        color: #166534;
                    }

                    .subtexto {
                        font-size: 16px;
                        color: #4b5563;
                        line-height: 1.6;
                        margin-bottom: 20px;
                    }

                    .datos {
                        background: #f9fafb;
                        border-radius: 14px;
                        padding: 18px;
                        border-left: 4px solid #f4c542;
                        line-height: 1.8;
                        margin-bottom: 22px;
                    }

                    .datos strong {
                        color: #111827;
                    }

                    .acciones {
                        display: flex;
                        gap: 12px;
                        flex-wrap: wrap;
                    }
                </style>

                <div class="panel-exito">
                    <h2 class="titulo-exito">Asistencia registrada</h2>

                    <div class="subtexto">
                        El participante fue validado correctamente y su asistencia ya quedó registrada en el evento correspondiente.
                    </div>

                    <div class="datos">
                        <p><strong>Usuario:</strong> %s</p>
                        <p><strong>Evento:</strong> %s</p>
                        <p><strong>Fecha de registro:</strong> %s</p>
                    </div>

                    <div class="acciones">
                        <a href="/asistencia/registrar" class="btn btn-principal">Registrar otra asistencia</a>
                        <a href="/eventos/resumen/%d" class="btn btn-secundario">Ver resumen del evento</a>
                    </div>
                </div>
                """.formatted(
                    asistencia.getUsuario().getNombre(),
                    asistencia.getEvento().getTitulo(),
                    asistencia.getFechaRegistro(),
                    asistencia.getEvento().getId()
            );

            ctx.html(aplicarLayoutSegunRol(
                    ctx,
                    "Asistencia registrada",
                    "La validación del participante fue completada correctamente.",
                    contenido
            ));

        } catch (Exception e) {
            String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>Error</title>
                </head>

                <body style="font-family:Arial;padding:40px">
                    <h2>Error al registrar asistencia</h2>
                    <p>%s</p>
                    <a href="/asistencia/registrar">Volver</a>
                </body>
                </html>
                """.formatted(e.getMessage());

            ctx.status(400).html(html);
        }
    }


    public void verInscritosEvento(Context ctx) {
        if (!validarOrganizadorOAdmin(ctx)) {
            return;
        }

        try {
            Long eventoId = Long.parseLong(ctx.pathParam("id"));
            Evento evento = eventoServicio.buscarPorId(eventoId);

            var inscripciones = inscripcionServicio.listarPorEvento(evento);

            StringBuilder filas = new StringBuilder();

            for (var inscripcion : inscripciones) {
                if (inscripcion.isCancelada()) {
                    continue;
                }

                boolean yaAsistio = asistenciaServicio.yaAsistio(
                        inscripcion.getUsuario(),
                        inscripcion.getEvento()
                );

                String estadoAsistencia = yaAsistio
                        ? "<span class='badge badge-asistio'>Asistencia registrada</span>"
                        : "<span class='badge badge-pendiente'>Pendiente</span>";

                String accion;

                if (yaAsistio) {
                    accion = "<span class='btn-deshabilitado'>Registrada</span>";
                } else if (SessionUtil.esAdmin(ctx)) {
                    accion = "<a class='btn-accion' href='/asistencia/registrar-inscripcion/" + inscripcion.getId() + "'>Registrar asistencia</a>";
                } else {
                    accion = "<a class='btn-accion' href='/asistencia/validar-inscripcion-qr/" + inscripcion.getId() + "'>Validar con QR</a>";
                }

                filas.append("""
                    <tr>
                        <td>%d</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td><span class="badge badge-inscripcion">Activa</span></td>
                        <td>%s</td>
                        <td>%s</td>
                    </tr>
                    """.formatted(
                        inscripcion.getId(),
                        inscripcion.getUsuario().getNombre(),
                        inscripcion.getUsuario().getCorreo(),
                        estadoAsistencia,
                        accion
                ));
            }

            String rutaVolver = SessionUtil.esAdmin(ctx)
                    ? "/admin/eventos"
                    : "/organizador/eventos";

            String contenido = """
                <style>
                    .info-evento {
                        background: linear-gradient(180deg,#ffffff,#fffdf7);
                        border: 1px solid #f3e6b2;
                        border-radius: 20px;
                        padding: 24px;
                        margin-bottom: 24px;
                    }

                    .info-grid {
                        display: grid;
                        grid-template-columns: repeat(4, 1fr);
                        gap: 16px;
                        margin-top: 16px;
                    }

                    .dato {
                        background: #f8fafc;
                        border-radius: 16px;
                        padding: 18px;
                        border: 1px solid #e5e7eb;
                    }

                    .dato-titulo {
                        font-size: 13px;
                        color: #6b7280;
                        margin-bottom: 8px;
                        font-weight: bold;
                        text-transform: uppercase;
                        letter-spacing: 0.4px;
                    }

                    .dato-valor {
                        font-size: 18px;
                        color: #111827;
                        font-weight: bold;
                        line-height: 1.45;
                        word-break: break-word;
                    }

                    .barra-superior {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        gap: 14px;
                        flex-wrap: wrap;
                        margin-bottom: 20px;
                    }

                    .texto-apoyo {
                        color: #4b5563;
                        font-size: 16px;
                    }

                    .btn-volver {
                        display: inline-block;
                        text-decoration: none;
                        padding: 13px 18px;
                        border-radius: 10px;
                        font-weight: bold;
                        font-size: 15px;
                        background: #6c757d;
                        color: white;
                        transition: transform 0.15s ease, opacity 0.2s ease;
                    }

                    .btn-volver:hover {
                        transform: translateY(-1px);
                        opacity: 0.96;
                    }

                    .badge-inscripcion {
                        background: #dbeafe;
                        color: #1d4ed8;
                    }

                    .badge-pendiente {
                        background: #fff7ed;
                        color: #c2410c;
                    }

                    .badge-asistio {
                        background: #dcfce7;
                        color: #166534;
                    }

                    .btn-accion {
                        display: inline-block;
                        text-decoration: none;
                        padding: 10px 14px;
                        border-radius: 8px;
                        background: linear-gradient(90deg,#0a3d91,#174ea6);
                        color: white;
                        font-size: 14px;
                        font-weight: bold;
                    }

                    .btn-deshabilitado {
                        display: inline-block;
                        padding: 10px 14px;
                        border-radius: 8px;
                        background: #d1d5db;
                        color: #374151;
                        font-size: 14px;
                        font-weight: bold;
                    }

                    @media (max-width: 1000px) {
                        .info-grid {
                            grid-template-columns: 1fr 1fr;
                        }
                    }

                    @media (max-width: 560px) {
                        .info-grid {
                            grid-template-columns: 1fr;
                        }

                        .barra-superior {
                            flex-direction: column;
                            align-items: flex-start;
                        }

                        th, td {
                            font-size: 15px;
                        }
                    }
                </style>

                <div class="info-evento">
                    <h2 style="margin-top:0; color:#0a3d91; font-size:28px;">Datos del evento</h2>

                    <div class="info-grid">
                        <div class="dato">
                            <div class="dato-titulo">ID</div>
                            <div class="dato-valor">%d</div>
                        </div>

                        <div class="dato">
                            <div class="dato-titulo">Título</div>
                            <div class="dato-valor">%s</div>
                        </div>

                        <div class="dato">
                            <div class="dato-titulo">Fecha</div>
                            <div class="dato-valor">%s</div>
                        </div>

                        <div class="dato">
                            <div class="dato-titulo">Hora</div>
                            <div class="dato-valor">%s</div>
                        </div>
                    </div>
                </div>

                <div class="barra-superior">
                    <div class="texto-apoyo">
                        Selecciona un participante para registrar su asistencia de forma segura.
                    </div>

                    <a href="%s" class="btn-volver">Volver a eventos</a>
                </div>

                <div class="panel">
                    <div class="panel-top"></div>

                    <div class="tabla-wrap">
                        <table>
                            <thead>
                                <tr>
                                    <th>ID inscripción</th>
                                    <th>Participante</th>
                                    <th>Correo</th>
                                    <th>Estado inscripción</th>
                                    <th>Estado asistencia</th>
                                    <th>Acción</th>
                                </tr>
                            </thead>
                            <tbody>
                                %s
                            </tbody>
                        </table>
                    </div>
                </div>
                """.formatted(
                    evento.getId(),
                    evento.getTitulo(),
                    evento.getFecha(),
                    evento.getHora(),
                    rutaVolver,
                    filas.toString()
            );

            ctx.html(aplicarLayoutSegunRol(
                    ctx,
                    "Inscritos del evento",
                    "Consulta los participantes inscritos y registra su asistencia directamente desde la lista del evento seleccionado.",
                    contenido
            ));

        } catch (Exception e) {
            String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>Error al mostrar inscritos</title>
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
                            line-height: 1.15;
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
                            line-height: 1.6;
                        }

                        .acciones {
                            margin-top: 24px;
                            display: flex;
                            gap: 12px;
                            flex-wrap: wrap;
                        }

                        a {
                            display: inline-block;
                            text-decoration: none;
                            padding: 13px 18px;
                            border-radius: 10px;
                            background: linear-gradient(90deg,#0a3d91,#174ea6);
                            color: white;
                            font-weight: bold;
                        }

                        .secundario {
                            background: #6c757d;
                        }
                    </style>
                </head>
                <body>
                    <div class="contenedor">
                        <div class="decoracion"></div>

                        <h2>Error al mostrar inscritos</h2>

                        <p>No fue posible cargar la lista de inscritos del evento solicitado.</p>

                        <div class="mensaje">%s</div>

                        <div class="acciones">
                            <a href="/asistencia/registrar">Ir a asistencia</a>
                            <a href="%s" class="secundario">Volver</a>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                    e.getMessage(),
                    SessionUtil.esAdmin(ctx) ? "/admin/eventos" : "/organizador/eventos"
            );

            ctx.status(400).html(html);
        }
    }

    public void registrarAsistenciaPorInscripcion(Context ctx) {
        if (!validarOrganizadorOAdmin(ctx)) {
            return;
        }

        try {
            Long inscripcionId = Long.parseLong(ctx.pathParam("id"));

            var inscripcion = inscripcionServicio.buscarPorId(inscripcionId);

            var asistencia = asistenciaServicio.registrarAsistenciaPorTokenQr(
                    inscripcion.getTokenQr()
            );

            ctx.redirect("/eventos/resumen/" + asistencia.getEvento().getId());

        } catch (Exception e) {
            String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>Error al registrar asistencia</title>
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
                        <h2>Error al registrar asistencia</h2>
                        <p>No fue posible registrar la asistencia desde la inscripción seleccionada.</p>
                        <div class="mensaje">%s</div>
                        <a href="/organizador/eventos">Volver</a>
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

            String rutaVolver = SessionUtil.esAdmin(ctx)
                    ? "/admin/eventos"
                    : "/organizador/eventos";

            String contenido = """
                <style>
                    .datos-evento {
                        background: linear-gradient(180deg,#ffffff,#fffdf7);
                        border: 1px solid #f3e6b2;
                        border-radius: 20px;
                        padding: 24px;
                        margin-bottom: 24px;
                        box-shadow: inset 0 1px 0 rgba(255,255,255,0.75);
                    }

                    .datos-grid {
                        display: grid;
                        grid-template-columns: repeat(4, 1fr);
                        gap: 16px;
                        margin-top: 16px;
                    }

                    .dato {
                        background: #f8fafc;
                        border-radius: 16px;
                        padding: 18px;
                        border: 1px solid #e5e7eb;
                    }

                    .dato-titulo {
                        font-size: 13px;
                        color: #6b7280;
                        margin-bottom: 8px;
                        font-weight: bold;
                        text-transform: uppercase;
                        letter-spacing: 0.4px;
                    }

                    .dato-valor {
                        font-size: 20px;
                        color: #111827;
                        font-weight: bold;
                        line-height: 1.4;
                        word-break: break-word;
                    }

                    .metricas {
                        display: grid;
                        grid-template-columns: repeat(3, 1fr);
                        gap: 18px;
                        margin-top: 10px;
                    }

                    .card-metrica {
                        border-radius: 20px;
                        padding: 24px;
                        box-shadow: 0 12px 24px rgba(0,0,0,0.07);
                    }

                    .card-metrica h3 {
                        margin: 0 0 10px 0;
                        font-size: 18px;
                    }

                    .card-metrica .valor {
                        font-size: 36px;
                        font-weight: bold;
                        line-height: 1.1;
                    }

                    .card-metrica .texto {
                        margin-top: 10px;
                        font-size: 15px;
                        line-height: 1.6;
                    }

                    .inscritos {
                        background: linear-gradient(180deg,#eef5ff,#f8fbff);
                        border: 1px solid #dbe7ff;
                        color: #0a3d91;
                    }

                    .asistentes {
                        background: linear-gradient(180deg,#ecfdf3,#f4fff8);
                        border: 1px solid #bbf7d0;
                        color: #166534;
                    }

                    .porcentaje {
                        background: linear-gradient(180deg,#fff9e8,#fffdf7);
                        border: 1px solid #f3e6b2;
                        color: #8a6200;
                    }

                    .barra-panel {
                        margin-top: 24px;
                        background: linear-gradient(180deg,#ffffff,#fffdf7);
                        border: 1px solid #f3e6b2;
                        border-radius: 20px;
                        padding: 24px;
                    }

                    .barra-panel h3 {
                        margin: 0 0 14px 0;
                        font-size: 24px;
                        color: #0a3d91;
                    }

                    .barra-fondo {
                        width: 100%%;
                        height: 22px;
                        background: #e5e7eb;
                        border-radius: 999px;
                        overflow: hidden;
                    }

                    .barra-valor {
                        height: 100%%;
                        width: %.2f%%;
                        background: linear-gradient(90deg,#0a3d91,#f4c542);
                        border-radius: 999px;
                    }

                    .barra-texto {
                        margin-top: 12px;
                        font-size: 16px;
                        color: #4b5563;
                        line-height: 1.6;
                    }

                    .acciones {
                        margin-top: 24px;
                        display: flex;
                        gap: 12px;
                        flex-wrap: wrap;
                    }

                    @media (max-width: 1000px) {
                        .datos-grid {
                            grid-template-columns: 1fr 1fr;
                        }

                        .metricas {
                            grid-template-columns: 1fr;
                        }
                    }

                    @media (max-width: 560px) {
                        .datos-grid {
                            grid-template-columns: 1fr;
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

                <div class="datos-evento">
                    <h2 style="margin-top:0; color:#0a3d91; font-size:28px;">Información general</h2>

                    <div class="datos-grid">
                        <div class="dato">
                            <div class="dato-titulo">ID</div>
                            <div class="dato-valor">%d</div>
                        </div>

                        <div class="dato">
                            <div class="dato-titulo">Título</div>
                            <div class="dato-valor">%s</div>
                        </div>

                        <div class="dato">
                            <div class="dato-titulo">Fecha</div>
                            <div class="dato-valor">%s</div>
                        </div>

                        <div class="dato">
                            <div class="dato-titulo">Hora</div>
                            <div class="dato-valor">%s</div>
                        </div>
                    </div>
                </div>

                <div class="metricas">
                    <div class="card-metrica inscritos">
                        <h3>Inscritos activos</h3>
                        <div class="valor">%d</div>
                        <div class="texto">Cantidad de participantes con inscripción válida al evento.</div>
                    </div>

                    <div class="card-metrica asistentes">
                        <h3>Asistentes registrados</h3>
                        <div class="valor">%d</div>
                        <div class="texto">Participantes cuya asistencia fue confirmada correctamente.</div>
                    </div>

                    <div class="card-metrica porcentaje">
                        <h3>Porcentaje de asistencia</h3>
                        <div class="valor">%.2f%%</div>
                        <div class="texto">Relación entre inscritos activos y asistentes registrados.</div>
                    </div>
                </div>

                <div class="barra-panel">
                    <h3>Indicador visual de asistencia</h3>

                    <div class="barra-fondo">
                        <div class="barra-valor"></div>
                    </div>

                    <div class="barra-texto">
                        El evento presenta un <strong>%.2f%%</strong> de asistencia en relación con los inscritos activos.
                    </div>
                </div>

                <div class="acciones">
                    <a href="%s" class="btn btn-principal">Volver a eventos</a>
                    <a href="/asistencia/registrar" class="btn btn-secundario">Registrar asistencia</a>
                </div>
                """.formatted(
                    porcentaje,
                    evento.getId(),
                    evento.getTitulo(),
                    evento.getFecha(),
                    evento.getHora(),
                    inscritos,
                    asistentes,
                    porcentaje,
                    porcentaje,
                    rutaVolver
            );

            ctx.html(aplicarLayoutSegunRol(
                    ctx,
                    "Resumen del evento",
                    "Consulta los indicadores principales de participación y asistencia del evento seleccionado.",
                    contenido
            ));

        } catch (Exception e) {
            String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>Error</title>
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
                        <h2>Error al mostrar resumen</h2>
                        <p>No fue posible construir el resumen del evento solicitado.</p>
                        <div class="mensaje">%s</div>
                        <a href="/asistencia/registrar">Volver</a>
                    </div>
                </body>
                </html>
                """.formatted(e.getMessage());

            ctx.status(400).html(html);
        }
    }
    public void mostrarValidadorQrInscripcion(Context ctx) {
        if (!validarOrganizadorOAdmin(ctx)) {
            return;
        }

        if (SessionUtil.esAdmin(ctx)) {
            ctx.redirect("/admin/eventos");
            return;
        }

        try {
            Long inscripcionId = Long.parseLong(ctx.pathParam("id"));
            var inscripcion = inscripcionServicio.buscarPorId(inscripcionId);

            String contenido = """
                <style>
                    .panel-validador {
                        background: linear-gradient(180deg,#ffffff,#fffdf7);
                        border: 1px solid #f3e6b2;
                        border-radius: 20px;
                        padding: 24px;
                    }

                    .info-box {
                        background: #f8fafc;
                        border: 1px solid #e5e7eb;
                        border-radius: 16px;
                        padding: 16px;
                        margin-bottom: 20px;
                        line-height: 1.8;
                    }

                    #readerInscripcion {
                        width: 100%%;
                        min-height: 280px;
                        border: 1px solid #d1d5db;
                        border-radius: 16px;
                        overflow: hidden;
                        background: #f8fafc;
                    }

                    .acciones {
                        margin-top: 18px;
                        display: flex;
                        gap: 12px;
                        flex-wrap: wrap;
                    }

                    .estado {
                        margin-top: 16px;
                        background: #fff9e8;
                        border-left: 5px solid #f4c542;
                        border-radius: 14px;
                        padding: 14px 15px;
                        color: #5b4a17;
                        line-height: 1.6;
                    }
                </style>

                <div class="panel-validador">
                    <h2 style="margin-top:0; color:#0a3d91;">Validar inscripción con QR</h2>

                    <div class="info-box">
                        <div><strong>Participante:</strong> %s</div>
                        <div><strong>Correo:</strong> %s</div>
                        <div><strong>Evento:</strong> %s</div>
                    </div>

                    <div id="readerInscripcion"></div>

                    <div class="estado">
                        Muestra el QR del participante frente a la cámara. El sistema validará que el código escaneado corresponda exactamente a esta inscripción.
                    </div>

                    <div class="acciones">
                        <button type="button" class="btn btn-principal" onclick="iniciarEscaneoInscripcion()">Iniciar cámara</button>
                        <button type="button" class="btn btn-secundario" onclick="detenerEscaneoInscripcion()">Detener cámara</button>
                        <a href="/asistencia/evento/%d" class="btn btn-secundario">Volver</a>
                    </div>

                    <form id="formValidarInscripcionQr" method="post" action="/asistencia/validar-inscripcion-qr/%d" style="display:none;">
                        <input type="hidden" name="qrContenido" id="qrContenidoInscripcion">
                    </form>
                </div>

                <script src="https://unpkg.com/html5-qrcode" type="text/javascript"></script>
                <script>
                    let html5QrCodeInscripcion = null;
                    let lectorInscripcionActivo = false;

                    function iniciarEscaneoInscripcion() {
                        if (lectorInscripcionActivo) return;

                        html5QrCodeInscripcion = new Html5Qrcode("readerInscripcion");

                        Html5Qrcode.getCameras().then(cameras => {
                            if (!cameras || cameras.length === 0) {
                                alert("No se encontró ninguna cámara disponible.");
                                return;
                            }

                            const camara = cameras[0].id;

                            html5QrCodeInscripcion.start(
                                camara,
                                { fps: 10, qrbox: { width: 220, height: 220 } },
                                (decodedText, decodedResult) => {
                                    document.getElementById("qrContenidoInscripcion").value = decodedText;

                                    detenerEscaneoInscripcion().then(() => {
                                        document.getElementById("formValidarInscripcionQr").submit();
                                    });
                                },
                                (errorMessage) => {}
                            ).then(() => {
                                lectorInscripcionActivo = true;
                            }).catch(err => {
                                alert("No fue posible iniciar la cámara: " + err);
                            });

                        }).catch(err => {
                            alert("Error obteniendo cámaras: " + err);
                        });
                    }

                    function detenerEscaneoInscripcion() {
                        if (!html5QrCodeInscripcion || !lectorInscripcionActivo) {
                            return Promise.resolve();
                        }

                        return html5QrCodeInscripcion.stop()
                            .then(() => {
                                lectorInscripcionActivo = false;
                            })
                            .catch(err => {});
                    }
                </script>
                """.formatted(
                    inscripcion.getUsuario().getNombre(),
                    inscripcion.getUsuario().getCorreo(),
                    inscripcion.getEvento().getTitulo(),
                    inscripcion.getEvento().getId(),
                    inscripcion.getId()
            );

            ctx.html(aplicarLayoutSegunRol(
                    ctx,
                    "Validar inscripción con QR",
                    "El organizador debe escanear el QR correcto del participante para registrar su asistencia.",
                    contenido
            ));

        } catch (Exception e) {
            ctx.status(400).result(e.getMessage());
        }
    }
    public void validarQrContraInscripcion(Context ctx) {
        if (!validarOrganizadorOAdmin(ctx)) {
            return;
        }

        if (SessionUtil.esAdmin(ctx)) {
            ctx.redirect("/admin/eventos");
            return;
        }

        try {
            Long inscripcionId = Long.parseLong(ctx.pathParam("id"));
            String qrContenido = ctx.formParam("qrContenido");

            var inscripcion = inscripcionServicio.buscarPorId(inscripcionId);
            String tokenEsperado = inscripcion.getTokenQr();

            String tokenEscaneado = extraerTokenDesdeContenidoQr(qrContenido);

            if (!tokenEsperado.equals(tokenEscaneado)) {
                throw new RuntimeException("El QR escaneado no corresponde a la inscripción seleccionada.");
            }

            asistenciaServicio.registrarAsistenciaPorTokenQr(tokenEscaneado);

            ctx.redirect("/eventos/resumen/" + inscripcion.getEvento().getId());

        } catch (Exception e) {
            String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>Error de validación</title>
                </head>
                <body style="font-family:Arial; padding:40px;">
                    <h2>Error al validar QR</h2>
                    <p>%s</p>
                    <a href="/organizador/eventos">Volver</a>
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

}