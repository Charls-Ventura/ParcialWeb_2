package edu.pucmm.icc352.utilidades;

import edu.pucmm.icc352.modelos.Inscripcion;

import java.util.UUID;

public class QRUtil {

    public static String generarTokenUnico() {
        return UUID.randomUUID().toString();
    }

    public static String generarContenidoQR(Inscripcion inscripcion) {
        if (inscripcion == null || inscripcion.getEvento() == null || inscripcion.getUsuario() == null) {
            throw new RuntimeException("No se puede generar el contenido del QR.");
        }

        return "idEvento=" + inscripcion.getEvento().getId()
                + ";idUsuario=" + inscripcion.getUsuario().getId()
                + ";token=" + inscripcion.getTokenQr();
    }

    public static String generarContenidoQR(Long idEvento, Long idUsuario, String token) {
        return "idEvento=" + idEvento + ";idUsuario=" + idUsuario + ";token=" + token;
    }
}