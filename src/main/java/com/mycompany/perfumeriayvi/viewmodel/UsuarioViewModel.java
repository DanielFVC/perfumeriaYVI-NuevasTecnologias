package com.mycompany.perfumeriayvi.viewmodel;

import com.mycompany.perfumeriayvi.model.entity.Usuario;
import com.mycompany.perfumeriayvi.model.service.UsuarioService;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ViewModel de gestión de vendedores (usuarios.jsp). Solo lo usa el administrador.
 *
 * Requisitos: RF09 (CU09, HU08) crear, bloquear y activar vendedores;
 * RF10 (CU10, HU02) asignar permisos; RNF-S6 política de contraseñas.
 */
@Component
@Scope("prototype")
public class UsuarioViewModel extends ViewModelBase {

    private final UsuarioService servicio;

    private List<Usuario> vendedores = List.of();
    private String nombreNuevo;
    private String usernameNuevo;

    public UsuarioViewModel(UsuarioService servicio) {
        this.servicio = servicio;
    }

    /** RF09: listado de vendedores. */
    public void cargarVendedores() {
        ejecutar(() -> vendedores = servicio.listarVendedores(), null);
    }

    /** RF09: comando crear vendedor. La contraseña nunca se conserva en el ViewModel. */
    public boolean crear(String nombre, String username, String contrasena, String actor) {
        boolean ok = ejecutar(() -> servicio.crearVendedor(nombre, username, contrasena, actor),
                "Vendedor creado. Asígnele los permisos de los módulos que va a usar.");
        if (!ok) {
            nombreNuevo = nombre;
            usernameNuevo = username;
        }
        return ok;
    }

    /** RF09: comando bloquear o activar. */
    public boolean cambiarEstado(int id, boolean activo, String actor) {
        return ejecutar(() -> servicio.cambiarEstado(id, activo, actor),
                activo ? "Vendedor activado." : "Vendedor bloqueado. Ya no puede iniciar sesión.");
    }

    /** RF10: comando asignar permisos. */
    public boolean actualizarPermisos(int id, boolean clientes, boolean productos, boolean proveedores,
                                      boolean ventas, String actor) {
        return ejecutar(() -> servicio.actualizarPermisos(id, clientes, productos, proveedores, ventas, actor),
                "Permisos actualizados. Se aplican de inmediato.");
    }

    public List<Usuario> getVendedores() { return vendedores; }
    public String getNombreNuevo() { return nombreNuevo; }
    public String getUsernameNuevo() { return usernameNuevo; }
}
