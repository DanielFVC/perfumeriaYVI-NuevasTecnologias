package com.mycompany.perfumeriayvi.model.repository;

import com.mycompany.perfumeriayvi.model.entity.DetalleVenta;
import com.mycompany.perfumeriayvi.model.entity.Venta;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Persistencia de ventas.
 * Requisitos: RF07 (CU07, HU06) registrar venta con control de inventario;
 * RF08 (CU08, HU07) consultar, actualizar y eliminar ventas.
 */
public interface VentaRepository {

    List<Venta> listar();

    Optional<Venta> buscarPorId(int id);

    /** RF07: precio de venta vigente; vacío si el producto no existe. */
    OptionalDouble precioVenta(int idProducto);

    int registrar(Venta venta, String usuario);

    void actualizar(Venta venta, String usuario);

    void eliminar(int idVenta, String usuario);

    List<DetalleVenta> detalles(int idVenta);

    /**
     * RF07: descuenta unidades solo si hay stock suficiente.
     * @return true si se descontó; false si no alcanzaba el stock.
     */
    boolean descontarStock(int idProducto, int cantidad);

    /** RF08: devuelve unidades al inventario al editar o eliminar una venta. */
    void devolverStock(int idProducto, int cantidad);

    int stockDisponible(int idProducto);
}
