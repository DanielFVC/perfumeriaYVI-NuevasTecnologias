package com.mycompany.perfumeriayvi.viewmodel;

import com.mycompany.perfumeriayvi.model.entity.Producto;
import com.mycompany.perfumeriayvi.model.entity.Proveedor;
import com.mycompany.perfumeriayvi.model.service.ProductoService;
import com.mycompany.perfumeriayvi.model.service.ProveedorService;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ViewModel del módulo de productos e inventario (inventario_productos.jsp,
 * registrar_productos.jsp, actualizar_productos.jsp).
 *
 * Requisitos: RF06 (CU06, HU05) con alerta de stock bajo; RNF-U3 conserva el formulario.
 */
@Component
@Scope("prototype")
public class ProductoViewModel extends ViewModelBase {

    private final ProductoService servicio;
    private final ProveedorService proveedorService;

    private List<Producto> productos = List.of();
    private List<Proveedor> proveedores = List.of();
    private Producto producto = new Producto();
    private long cantidadStockBajo;

    public ProductoViewModel(ProductoService servicio, ProveedorService proveedorService) {
        this.servicio = servicio;
        this.proveedorService = proveedorService;
    }

    /** RF06: inventario con conteo de productos en stock bajo. */
    public void cargarInventario() {
        ejecutar(() -> {
            productos = servicio.listar();
            cantidadStockBajo = servicio.contarStockBajo(productos);
        }, null);
    }

    /** RF06: formulario vacío con la lista de proveedores. */
    public void prepararNuevo() {
        producto = new Producto();
        cargarProveedores();
    }

    /** RF06: cargar producto a editar. */
    public boolean cargarParaEditar(int id) {
        cargarProveedores();
        return ejecutar(() -> producto = servicio.buscarPorId(id), null);
    }

    /** RF06: comando registrar. */
    public boolean registrar(Producto datos, String usuario) {
        producto = datos;
        boolean ok = ejecutar(() -> servicio.registrar(datos, usuario), "Producto registrado correctamente.");
        if (!ok) {
            cargarProveedores();
        }
        return ok;
    }

    /** RF06: comando actualizar. */
    public boolean actualizar(Producto datos, String usuario) {
        producto = datos;
        boolean ok = ejecutar(() -> servicio.actualizar(datos, usuario), "Producto actualizado correctamente.");
        if (!ok) {
            cargarProveedores();
        }
        return ok;
    }

    /** RF06 + RNF-F3: comando eliminar. */
    public boolean eliminar(int id, String usuario) {
        return ejecutar(() -> servicio.eliminar(id, usuario), "Producto eliminado correctamente.");
    }

    private void cargarProveedores() {
        ejecutar(() -> proveedores = proveedorService.listar(), null);
    }

    /** RF06: usado por la vista para resaltar la fila. */
    public boolean stockBajo(Producto p) {
        return ProductoService.tieneStockBajo(p);
    }

    public List<Producto> getProductos() { return productos; }
    public List<Proveedor> getProveedores() { return proveedores; }
    public Producto getProducto() { return producto; }
    public long getCantidadStockBajo() { return cantidadStockBajo; }
}
