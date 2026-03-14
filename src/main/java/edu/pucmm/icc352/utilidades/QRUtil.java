package edu.pucmm.icc352.utilidades;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import edu.pucmm.icc352.modelos.Inscripcion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

public class QRUtil {

    public static String generarTokenUnico() {
        return UUID.randomUUID().toString();
    }

    public static String generarContenidoQR(Inscripcion inscripcion) {
        if (inscripcion == null) {
            throw new RuntimeException("La inscripción es obligatoria.");
        }

        if (inscripcion.getEvento() == null || inscripcion.getUsuario() == null) {
            throw new RuntimeException("La inscripción no tiene datos suficientes para generar el QR.");
        }

        return "idEvento=" + inscripcion.getEvento().getId()
                + ";idUsuario=" + inscripcion.getUsuario().getId()
                + ";token=" + inscripcion.getTokenQr();
    }

    public static String generarQrBase64(Inscripcion inscripcion) {
        try {
            String contenido = generarContenidoQR(inscripcion);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(contenido, BarcodeFormat.QR_CODE, 260, 260);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            byte[] imagenBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(imagenBytes);

        } catch (WriterException | IOException e) {
            throw new RuntimeException("Error generando la imagen QR.", e);
        }
    }
}