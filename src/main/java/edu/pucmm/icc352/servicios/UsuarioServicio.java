package edu.pucmm.icc352.servicios;

import edu.pucmm.icc352.modelos.Rol;
import edu.pucmm.icc352.modelos.Usuario;
import edu.pucmm.icc352.repositorios.UsuarioRepositorio;
import edu.pucmm.icc352.utilidades.PasswordUtil;

import java.util.List;
import java.util.Optional;

public class UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio = new UsuarioRepositorio();

    public Usuario registrarUsuario(String nombre, String correo, String password, Rol rol) {
        validarDatosUsuario(nombre, correo, password, rol);

        Optional<Usuario> usuarioExistente = usuarioRepositorio.buscarPorCorreo(correo);
        if (usuarioExistente.isPresent()) {
            throw new RuntimeException("Ya existe un usuario con ese correo.");
        }

        Usuario usuario = new Usuario(
                nombre.trim(),
                correo.trim().toLowerCase(),
                PasswordUtil.hashPassword(password),
                rol
        );

        return usuarioRepositorio.guardar(usuario);
    }

    public Usuario autenticar(String correo, String password) {
        if (correo == null || correo.isBlank() || password == null || password.isBlank()) {
            throw new RuntimeException("Correo y password son obligatorios.");
        }

        Usuario usuario = usuarioRepositorio.buscarPorCorreo(correo.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas."));

        if (usuario.isBloqueado()) {
            throw new RuntimeException("El usuario está bloqueado.");
        }

        if (!PasswordUtil.verificarPassword(password, usuario.getPassword())) {
            throw new RuntimeException("Credenciales inválidas.");
        }

        return usuario;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepositorio.listarTodos();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepositorio.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
    }

    public Usuario buscarPorCorreo(String correo) {
        return usuarioRepositorio.buscarPorCorreo(correo.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
    }

    public Usuario bloquearUsuario(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setBloqueado(true);
        return usuarioRepositorio.actualizar(usuario);
    }

    public Usuario desbloquearUsuario(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setBloqueado(false);
        return usuarioRepositorio.actualizar(usuario);
    }

    private void validarDatosUsuario(String nombre, String correo, String password, Rol rol) {
        if (nombre == null || nombre.isBlank()) {
            throw new RuntimeException("El nombre es obligatorio.");
        }

        if (correo == null || correo.isBlank()) {
            throw new RuntimeException("El correo es obligatorio.");
        }

        if (password == null || password.isBlank()) {
            throw new RuntimeException("La contraseña es obligatoria.");
        }

        if (password.length() < 4) {
            throw new RuntimeException("La contraseña debe tener al menos 4 caracteres.");
        }

        if (rol == null) {
            throw new RuntimeException("El rol es obligatorio.");
        }
    }
}