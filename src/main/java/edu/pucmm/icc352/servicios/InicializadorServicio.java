package edu.pucmm.icc352.servicios;

import edu.pucmm.icc352.modelos.Rol;
import edu.pucmm.icc352.modelos.Usuario;
import edu.pucmm.icc352.repositorios.UsuarioRepositorio;
import edu.pucmm.icc352.utilidades.PasswordUtil;

public class InicializadorServicio {

    private final UsuarioRepositorio usuarioRepositorio = new UsuarioRepositorio();

    public void crearAdminSiNoExiste() {
        String correoAdmin = "admin@admin.com";

        if (usuarioRepositorio.buscarPorCorreo(correoAdmin).isEmpty()) {
            Usuario admin = new Usuario(
                    "Administrador",
                    correoAdmin,
                    PasswordUtil.hashPassword("admin123"),
                    Rol.ADMIN
            );

            usuarioRepositorio.guardar(admin);


            System.out.println("USUARIO ADMINISTRADOR CREADO");
            System.out.println("Correo: admin@admin.com");
            System.out.println("Clave: admin123");
        } else {
            System.out.println("El administrador por defecto ya existe.");
        }
    }
}