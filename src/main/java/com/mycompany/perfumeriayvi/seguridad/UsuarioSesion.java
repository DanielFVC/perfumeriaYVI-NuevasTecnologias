package com.mycompany.perfumeriayvi.seguridad;

import com.mycompany.perfumeriayvi.model.entity.Usuario;

import java.io.Serializable;

/**
 * Datos del usuario autenticado que se guardan en la sesión HTTP.
 * Nunca incluye la contraseña ni su hash (RNF-S1).
 *
 * Requisitos: RF01 (CU01) sesión iniciada; RF03 (CU03) reglas de autorización por rol y permiso.
 */
public final class UsuarioSesion implements Serializable {

    public static final String ATRIBUTO = "usuarioSesion";

    private static final long serialVersionUID = 1L;

    private final int id;
    private final String username;
    private final String nombre;
    private final String rol;
    private final boolean accesoClientes;
    private final boolean accesoProveedores;
    private final boolean accesoProductos;
    private final boolean accesoVentas;

    private UsuarioSesion(Usuario u) {
        this.id = u.getId();
        this.username = u.getUsername();
        this.nombre = u.getFullName();
        this.rol = u.getRole();
        this.accesoClientes = u.isAccesoClientes();
        this.accesoProveedores = u.isAccesoProveedores();
        this.accesoProductos = u.isAccesoProductos();
        this.accesoVentas = u.isAccesoVentas();
    }

    public static UsuarioSesion de(Usuario usuario) {
        return new UsuarioSesion(usuario);
    }

    /** RF03: regla central de autorización. */
    public boolean puedeAcceder(Modulo modulo) {
        if (isAdmin()) {
            return true;
        }
        return switch (modulo) {
            case CLIENTES -> accesoClientes;
            case PROVEEDORES -> accesoProveedores;
            case PRODUCTOS -> accesoProductos;
            case VENTAS -> accesoVentas;
            case USUARIOS, AUDITORIA -> false;
        };
    }

    public boolean isAdmin() {
        return Usuario.ROL_ADMIN.equals(rol);
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getNombre() { return nombre; }
    public String getRol() { return rol; }
}
