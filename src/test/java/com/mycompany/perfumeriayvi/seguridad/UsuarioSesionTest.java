package com.mycompany.perfumeriayvi.seguridad;

import com.mycompany.perfumeriayvi.model.entity.Usuario;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas de RF03 (CU03) y RF10: matriz de autorización por rol y permisos. */
public class UsuarioSesionTest {

    public static Usuario usuario(String rol, boolean clientes, boolean proveedores, boolean productos, boolean ventas) {
        Usuario u = new Usuario();
        u.setId(1);
        u.setUsername("u");
        u.setRole(rol);
        u.setActive(true);
        u.setAccesoClientes(clientes);
        u.setAccesoProveedores(proveedores);
        u.setAccesoProductos(productos);
        u.setAccesoVentas(ventas);
        return u;
    }

    @Test
    void elAdministradorAccedeATodo() {
        UsuarioSesion admin = UsuarioSesion.de(usuario(Usuario.ROL_ADMIN, false, false, false, false));
        for (Modulo m : Modulo.values()) {
            assertTrue(admin.puedeAcceder(m), m.name());
        }
    }

    @Test
    void elVendedorSoloAccedeASusModulos() {
        UsuarioSesion v = UsuarioSesion.de(usuario(Usuario.ROL_VENDEDOR, true, false, false, true));
        assertTrue(v.puedeAcceder(Modulo.CLIENTES));
        assertTrue(v.puedeAcceder(Modulo.VENTAS));
        assertFalse(v.puedeAcceder(Modulo.PROVEEDORES));
        assertFalse(v.puedeAcceder(Modulo.PRODUCTOS));
    }

    @Test
    void elVendedorNuncaAccedeAUsuariosNiAuditoria() {
        UsuarioSesion v = UsuarioSesion.de(usuario(Usuario.ROL_VENDEDOR, true, true, true, true));
        assertFalse(v.puedeAcceder(Modulo.USUARIOS));
        assertFalse(v.puedeAcceder(Modulo.AUDITORIA));
    }
}
