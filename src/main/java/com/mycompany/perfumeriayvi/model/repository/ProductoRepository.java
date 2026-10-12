package com.mycompany.perfumeriayvi.model.repository;

import com.mycompany.perfumeriayvi.model.entity.Producto;

import java.util.List;
import java.util.Optional;

/**
 * Persistencia de productos.
 * Requisitos: RF06 (CU06, HU05) gestionar productos e inventario.
 * RNF-F3: un producto vendido no se puede eliminar (FK con detalle_venta).
 */
public interface ProductoRepository {

    List<Producto> listar();

    Optional<Producto> buscarPorId(int id);

    void registrar(Producto producto, String usuario);

    void actualizar(Producto producto, String usuario);

    void eliminar(int id, String usuario);
}
