package com.mycompany.perfumeriayvi.model.service;

import com.mycompany.perfumeriayvi.model.entity.Usuario;
import com.mycompany.perfumeriayvi.model.repository.UsuarioRepository;
import com.mycompany.perfumeriayvi.seguridad.CifradoContrasena;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

import static com.mycompany.perfumeriayvi.model.service.Validaciones.exigir;
import static com.mycompany.perfumeriayvi.model.service.Validaciones.limpiar;

/**
 * Gestión de vendedores (solo administrador).
 *
 * Requisitos:
 * - RF09 (CU09, HU08): crear, bloquear y activar vendedores.
 * - RF10 (CU10, HU02): asignar permisos por módulo.
 * - RNF-S1: la contraseña se guarda como hash BCrypt.
 * - RNF-S6: política de contraseñas.
 */
@Service
public class UsuarioService {

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9._-]{4,30}$");

    private final UsuarioRepository repositorio;
    private final CifradoContrasena cifrado;

    public UsuarioService(UsuarioRepository repositorio, CifradoContrasena cifrado) {
        this.repositorio = repositorio;
        this.cifrado = cifrado;
    }

    /** RF09: listado de vendedores. */
    public List<Usuario> listarVendedores() {
        return repositorio.listarVendedores();
    }

    /** RF09: crear vendedor con contraseña cifrada y todos los permisos desactivados. */
    public void crearVendedor(String nombreCompleto, String username, String contrasena, String actor) {
        String nombre = limpiar(nombreCompleto);
        String usuario = limpiar(username);

        exigir(nombre.length() >= 3, "El nombre completo debe tener al menos 3 caracteres.");
        exigir(USERNAME.matcher(usuario).matches(),
                "El usuario debe tener de 4 a 30 caracteres: letras, números, punto, guion o guion bajo.");
        validarContrasena(contrasena);
        exigir(!repositorio.existeUsername(usuario), "El usuario " + usuario + " ya existe.");

        Usuario u = new Usuario();
        u.setFullName(nombre);
        u.setUsername(usuario);
        u.setPasswordHash(cifrado.cifrar(contrasena));
        u.setRole(Usuario.ROL_VENDEDOR);
        u.setActive(true);
        repositorio.registrar(u, actor);
    }

    /** RF09: bloquear (activo = false) o activar (activo = true) un vendedor. */
    public void cambiarEstado(int id, boolean activo, String actor) {
        exigir(id > 0, "No se indicó el vendedor.");
        repositorio.cambiarEstado(id, activo, actor);
    }

    /** RF10: asignar permisos por módulo a un vendedor. */
    public void actualizarPermisos(int id, boolean clientes, boolean productos,
                                   boolean proveedores, boolean ventas, String actor) {
        exigir(id > 0, "No se indicó el vendedor.");
        repositorio.actualizarPermisos(id, clientes, productos, proveedores, ventas, actor);
    }

    /** RNF-S6: mínimo 8 caracteres con mayúscula, minúscula y número. */
    public void validarContrasena(String contrasena) {
        exigir(contrasena != null && contrasena.length() >= 8 && contrasena.length() <= 72,
                "La contraseña debe tener entre 8 y 72 caracteres.");
        exigir(contrasena.chars().anyMatch(Character::isUpperCase), "La contraseña debe incluir una letra mayúscula.");
        exigir(contrasena.chars().anyMatch(Character::isLowerCase), "La contraseña debe incluir una letra minúscula.");
        exigir(contrasena.chars().anyMatch(Character::isDigit), "La contraseña debe incluir un número.");
    }
}
