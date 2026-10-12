package com.mycompany.perfumeriayvi.model.service;

import com.mycompany.perfumeriayvi.model.entity.Producto;
import com.mycompany.perfumeriayvi.model.repository.ProductoRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.mycompany.perfumeriayvi.model.service.Validaciones.*;

/**
 * Reglas de negocio de productos e inventario.
 *
 * Requisitos: RF06 (CU06, HU05) registrar, consultar, actualizar y eliminar productos,
 * con alerta de stock bajo (stock &lt;= stock mínimo).
 * RNF-F2: validaciones. RNF-F3: no se elimina un producto que ya fue vendido.
 */
@Service
public class ProductoService {

    private final ProductoRepository repositorio;

    public ProductoService(ProductoRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** RF06: inventario completo (también alimenta el selector de RF07). */
    public List<Producto> listar() {
        return repositorio.listar();
    }

    /** RF06: alerta de stock bajo. */
    public long contarStockBajo(List<Producto> productos) {
        return productos.stream().filter(ProductoService::tieneStockBajo).count();
    }

    /** RF06: un producto tiene stock bajo cuando sus unidades no superan el mínimo. */
    public static boolean tieneStockBajo(Producto p) {
        return p.getStock() <= p.getStockMinimo();
    }

    /** RF06: consulta de un producto. */
    public Producto buscarPorId(int id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new ReglaNegocioException("El producto solicitado no existe."));
    }

    /** RF06: registrar producto. */
    public void registrar(Producto p, String usuario) {
        validar(p);
        repositorio.registrar(p, usuario);
    }

    /** RF06: actualizar producto. */
    public void actualizar(Producto p, String usuario) {
        exigir(p.getId() > 0, "No se indicó el producto a actualizar.");
        validar(p);
        repositorio.actualizar(p, usuario);
    }

    /** RF06 + RNF-F3: eliminar producto. */
    public void eliminar(int id, String usuario) {
        try {
            repositorio.eliminar(id, usuario);
        } catch (DataIntegrityViolationException e) {
            throw new ReglaNegocioException(
                    "No se puede eliminar el producto porque ya tiene ventas registradas.");
        }
    }

    /** RF06 / RNF-F2: reglas de validación del producto. */
    void validar(Producto p) {
        p.setNombre(limpiar(p.getNombre()));
        p.setMarca(limpiar(p.getMarca()));
        p.setTipo(limpiar(p.getTipo()));
        p.setCategoria(limpiar(p.getCategoria()));
        p.setDescripcion(limpiar(p.getDescripcion()));

        exigir(p.getNombre().length() >= 3, "El nombre del producto debe tener mínimo 3 caracteres.");
        exigir(p.getMarca().length() >= 2, "La marca es obligatoria.");
        exigir(!p.getTipo().isEmpty(), "El tipo de producto es obligatorio.");
        exigir(!p.getCategoria().isEmpty(), "La categoría es obligatoria.");
        exigir(p.getDescripcion().length() >= 5, "La descripción debe tener al menos 5 caracteres.");
        exigir(p.getPrecioCompra() > 0, "El precio de compra debe ser mayor a cero.");
        exigir(p.getPrecioVenta() > 0, "El precio de venta debe ser mayor a cero.");
        exigir(p.getPrecioVenta() >= p.getPrecioCompra(),
                "El precio de venta no puede ser menor al precio de compra.");
        exigir(p.getStock() >= 0, "El stock no puede ser negativo.");
        exigir(p.getStockMinimo() >= 0, "El stock mínimo no puede ser negativo.");
        exigir(p.getIdProveedor() > 0, "Debe seleccionar un proveedor.");
    }
}
