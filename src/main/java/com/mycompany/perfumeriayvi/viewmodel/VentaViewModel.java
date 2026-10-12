package com.mycompany.perfumeriayvi.viewmodel;

import com.mycompany.perfumeriayvi.model.entity.Cliente;
import com.mycompany.perfumeriayvi.model.entity.DetalleVenta;
import com.mycompany.perfumeriayvi.model.entity.Producto;
import com.mycompany.perfumeriayvi.model.entity.Venta;
import com.mycompany.perfumeriayvi.model.service.ClienteService;
import com.mycompany.perfumeriayvi.model.service.ProductoService;
import com.mycompany.perfumeriayvi.model.service.VentaService;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel del módulo de ventas (nueva_venta.jsp, reportes.jsp, actualizar_reportes.jsp).
 *
 * Requisitos: RF07 (CU07, HU06) registrar venta con control de stock;
 * RF08 (CU08, HU07) consultar, actualizar y eliminar ventas; RNF-U3 conserva las líneas si hay error.
 */
@Component
@Scope("prototype")
public class VentaViewModel extends ViewModelBase {

    private final VentaService servicio;
    private final ClienteService clienteService;
    private final ProductoService productoService;

    private List<Venta> ventas = List.of();
    private List<Cliente> clientes = List.of();
    private List<Producto> productos = List.of();
    private Venta venta = ventaVacia();
    private double totalVendido;

    public VentaViewModel(VentaService servicio, ClienteService clienteService, ProductoService productoService) {
        this.servicio = servicio;
        this.clienteService = clienteService;
        this.productoService = productoService;
    }

    /** RF08: reporte de ventas con el total vendido. */
    public void cargarReporte() {
        ejecutar(() -> {
            ventas = servicio.listar();
            totalVendido = ventas.stream().mapToDouble(Venta::getTotal).sum();
        }, null);
    }

    /** RF07: formulario de nueva venta con la fecha de hoy. */
    public void prepararNueva() {
        venta = ventaVacia();
        cargarCatalogos();
    }

    /** RF08: venta existente para editar. */
    public boolean cargarParaEditar(int id) {
        cargarCatalogos();
        return ejecutar(() -> venta = servicio.buscar(id), null);
    }

    /** RF07: comando registrar venta. */
    public boolean registrar(String fecha, int idCliente, List<Integer> idProductos, List<Integer> cantidades,
                             int idUsuario, String usuario) {
        boolean ok = ejecutar(() -> servicio.registrar(fecha, idCliente, idProductos, cantidades, idUsuario, usuario),
                "Venta registrada correctamente. El inventario fue actualizado.");
        if (!ok) {
            conservarFormulario(0, fecha, idCliente, idProductos, cantidades);
        }
        return ok;
    }

    /** RF08: comando actualizar venta. */
    public boolean actualizar(int idVenta, String fecha, int idCliente, List<Integer> idProductos,
                              List<Integer> cantidades, String usuario) {
        boolean ok = ejecutar(() -> servicio.actualizar(idVenta, fecha, idCliente, idProductos, cantidades, usuario),
                "Venta actualizada correctamente.");
        if (!ok) {
            conservarFormulario(idVenta, fecha, idCliente, idProductos, cantidades);
        }
        return ok;
    }

    /** RF08: comando eliminar (anular) venta. */
    public boolean eliminar(int idVenta, String usuario) {
        return ejecutar(() -> servicio.eliminar(idVenta, usuario),
                "Venta eliminada. Las unidades se devolvieron al inventario.");
    }

    /** RNF-U3: vuelve a mostrar lo que el usuario había escrito. */
    private void conservarFormulario(int idVenta, String fecha, int idCliente,
                                     List<Integer> idProductos, List<Integer> cantidades) {
        Venta v = new Venta();
        v.setId(idVenta);
        v.setFecha(fecha);
        v.setIdCliente(idCliente);
        List<DetalleVenta> lineas = new ArrayList<>();
        if (idProductos != null) {
            for (int i = 0; i < idProductos.size(); i++) {
                int cantidad = cantidades != null && i < cantidades.size() && cantidades.get(i) != null
                        ? cantidades.get(i) : 1;
                Integer idProducto = idProductos.get(i);
                lineas.add(new DetalleVenta(idProducto == null ? 0 : idProducto, null, cantidad, 0));
            }
        }
        if (lineas.isEmpty()) {
            lineas.add(new DetalleVenta(0, null, 1, 0));
        }
        v.setDetalles(lineas);
        venta = v;
        cargarCatalogos();
    }

    private void cargarCatalogos() {
        ejecutar(() -> {
            clientes = clienteService.listar();
            productos = productoService.listar();
        }, null);
    }

    private static Venta ventaVacia() {
        Venta v = new Venta();
        v.setFecha(LocalDate.now().toString());
        v.setDetalles(new ArrayList<>(List.of(new DetalleVenta(0, null, 1, 0))));
        return v;
    }

    public List<Venta> getVentas() { return ventas; }
    public List<Cliente> getClientes() { return clientes; }
    public List<Producto> getProductos() { return productos; }
    public Venta getVenta() { return venta; }
    public double getTotalVendido() { return totalVendido; }
}
