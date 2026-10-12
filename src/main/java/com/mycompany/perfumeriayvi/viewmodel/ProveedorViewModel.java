package com.mycompany.perfumeriayvi.viewmodel;

import com.mycompany.perfumeriayvi.model.entity.Proveedor;
import com.mycompany.perfumeriayvi.model.service.ClienteService;
import com.mycompany.perfumeriayvi.model.service.ProveedorService;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ViewModel del módulo de proveedores (ver_proveedores.jsp, registrar_proveedores.jsp,
 * actualizar_proveedores.jsp).
 *
 * Requisitos: RF05 (CU05, HU04). RNF-U3: el formulario conserva los datos si hay error.
 */
@Component
@Scope("prototype")
public class ProveedorViewModel extends ViewModelBase {

    private final ProveedorService servicio;

    private List<Proveedor> proveedores = List.of();
    private Proveedor proveedor = new Proveedor();

    public ProveedorViewModel(ProveedorService servicio) {
        this.servicio = servicio;
    }

    /** RF05: consultar proveedores. */
    public void cargarLista() {
        ejecutar(() -> proveedores = servicio.listar(), null);
    }

    /** RF05: formulario vacío. */
    public void prepararNuevo() {
        proveedor = new Proveedor();
    }

    /** RF05: cargar proveedor a editar. */
    public boolean cargarParaEditar(int id) {
        return ejecutar(() -> proveedor = servicio.buscarPorId(id), null);
    }

    /** RF05: comando registrar. */
    public boolean registrar(Proveedor datos, String usuario) {
        proveedor = datos;
        boolean ok = ejecutar(() -> servicio.registrar(datos, usuario), "Proveedor registrado correctamente.");
        if (ok) {
            proveedor = new Proveedor();
        }
        return ok;
    }

    /** RF05: comando actualizar. */
    public boolean actualizar(Proveedor datos, String usuario) {
        proveedor = datos;
        return ejecutar(() -> servicio.actualizar(datos, usuario), "Proveedor actualizado correctamente.");
    }

    /** RF05 + RNF-F3: comando eliminar. */
    public boolean eliminar(int id, String usuario) {
        return ejecutar(() -> servicio.eliminar(id, usuario), "Proveedor eliminado correctamente.");
    }

    public List<Proveedor> getProveedores() { return proveedores; }
    public Proveedor getProveedor() { return proveedor; }
    public List<String> getTiposProveedor() { return ProveedorService.TIPOS_PROVEEDOR; }
    public List<String> getTiposVia() { return ClienteService.TIPOS_VIA; }
}
