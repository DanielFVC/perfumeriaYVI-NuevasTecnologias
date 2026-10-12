package com.mycompany.perfumeriayvi.model.service;

import com.mycompany.perfumeriayvi.model.entity.DetalleVenta;
import com.mycompany.perfumeriayvi.model.entity.Venta;
import com.mycompany.perfumeriayvi.model.repository.VentaRepository;
import com.mycompany.perfumeriayvi.monitoreo.MetricasPerfumeria;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.mycompany.perfumeriayvi.model.service.Validaciones.exigir;

/**
 * Reglas de negocio de ventas.
 *
 * Requisitos:
 * - RF07 (CU07, HU06): registrar una venta con varios productos, calcular subtotales y total
 *   con el precio vigente y descontar el stock (no se vende más de lo disponible).
 * - RF08 (CU08, HU07): consultar, actualizar y eliminar ventas devolviendo el stock.
 * - RNF-F1: cada operación es una transacción; si algo falla se revierte completa.
 */
@Service
public class VentaService {

    private final VentaRepository repositorio;
    private final MetricasPerfumeria metricas;

    public VentaService(VentaRepository repositorio, MetricasPerfumeria metricas) {
        this.repositorio = repositorio;
        this.metricas = metricas;
    }

    /** RF08: reporte de ventas. */
    public List<Venta> listar() {
        return repositorio.listar();
    }

    /** RF08: venta con sus líneas para editarla. */
    public Venta buscar(int id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new ReglaNegocioException("La venta solicitada no existe."));
    }

    /** RF07 + RNF-F1: registrar venta y descontar inventario en una sola transacción. */
    @Transactional
    public int registrar(String fecha, int idCliente, List<Integer> productos, List<Integer> cantidades,
                         int idUsuario, String usuario) {
        Venta venta = construirVenta(fecha, idCliente, productos, cantidades);
        venta.setIdUser(idUsuario);
        descontarInventario(venta.getDetalles());
        int id = repositorio.registrar(venta, usuario);
        metricas.ventaRegistrada(venta.getTotal());
        return id;
    }

    /** RF08 + RNF-F1: actualizar venta: devuelve el stock anterior y descuenta el nuevo. */
    @Transactional
    public void actualizar(int idVenta, String fecha, int idCliente, List<Integer> productos,
                           List<Integer> cantidades, String usuario) {
        Venta anterior = buscar(idVenta);
        Venta venta = construirVenta(fecha, idCliente, productos, cantidades);
        venta.setId(idVenta);
        venta.setIdUser(anterior.getIdUser());

        devolverInventario(anterior.getDetalles());
        descontarInventario(venta.getDetalles());
        repositorio.actualizar(venta, usuario);
        metricas.ventaCorregida();
    }

    /** RF08 + RNF-F1: eliminar (anular) venta devolviendo las unidades al inventario. */
    @Transactional
    public void eliminar(int idVenta, String usuario) {
        Venta venta = buscar(idVenta);
        devolverInventario(venta.getDetalles());
        repositorio.eliminar(idVenta, usuario);
        metricas.ventaAnulada();
    }

    /** RF07: valida los datos, agrupa productos repetidos y calcula subtotales y total. */
    Venta construirVenta(String fecha, int idCliente, List<Integer> productos, List<Integer> cantidades) {
        exigir(fecha != null && !fecha.isBlank(), "La fecha de la venta es obligatoria.");
        try {
            LocalDate.parse(fecha);
        } catch (DateTimeParseException e) {
            throw new ReglaNegocioException("La fecha de la venta no es válida.");
        }
        exigir(idCliente > 0, "Debe seleccionar un cliente.");
        exigir(productos != null && !productos.isEmpty(), "La venta debe tener al menos un producto.");
        exigir(cantidades != null && cantidades.size() == productos.size(),
                "Cada producto debe tener una cantidad.");

        // Si el mismo producto se agrega dos veces, se suman las cantidades
        Map<Integer, Integer> agrupado = new LinkedHashMap<>();
        for (int i = 0; i < productos.size(); i++) {
            Integer idProducto = productos.get(i);
            Integer cantidad = cantidades.get(i);
            exigir(idProducto != null && idProducto > 0, "Debe seleccionar el producto de cada fila.");
            exigir(cantidad != null && cantidad > 0, "Las cantidades deben ser mayores a cero.");
            agrupado.merge(idProducto, cantidad, Integer::sum);
        }

        Venta venta = new Venta();
        venta.setFecha(fecha);
        venta.setIdCliente(idCliente);

        List<DetalleVenta> detalles = new ArrayList<>();
        double total = 0;
        for (Map.Entry<Integer, Integer> linea : agrupado.entrySet()) {
            double precio = repositorio.precioVenta(linea.getKey())
                    .orElseThrow(() -> new ReglaNegocioException("Uno de los productos seleccionados no existe."));
            double subtotal = precio * linea.getValue();
            detalles.add(new DetalleVenta(linea.getKey(), null, linea.getValue(), subtotal));
            total += subtotal;
        }
        venta.setDetalles(detalles);
        venta.setTotal(total);
        return venta;
    }

    /** RF07: descuenta el stock; si un producto no alcanza, se cancela toda la venta. */
    private void descontarInventario(List<DetalleVenta> detalles) {
        for (DetalleVenta d : detalles) {
            if (!repositorio.descontarStock(d.getIdProducto(), d.getCantidad())) {
                int disponible = repositorio.stockDisponible(d.getIdProducto());
                throw new ReglaNegocioException("Stock insuficiente para el producto " + d.getIdProducto()
                        + ": se pidieron " + d.getCantidad() + " y hay " + disponible + " disponibles.");
            }
        }
    }

    /** RF08: devuelve al inventario las unidades de una venta que se edita o elimina. */
    private void devolverInventario(List<DetalleVenta> detalles) {
        for (DetalleVenta d : detalles) {
            repositorio.devolverStock(d.getIdProducto(), d.getCantidad());
        }
    }
}
