package com.mycompany.perfumeriayvi.model.repository;

import com.mycompany.perfumeriayvi.model.entity.Proveedor;

import java.util.List;
import java.util.Optional;

/**
 * Persistencia de proveedors.
 * Requisitos: RF05 (CU05, HU04) gestionar proveedores.
 * RNF-F3: el NIT es único; un proveedor con productos no se puede eliminar (FK).
 */
public interface ProveedorRepository {

    List<Proveedor> listar();

    Optional<Proveedor> buscarPorId(int id);

    boolean existeNit(String nit, int excluirId);

    void registrar(Proveedor proveedor, String usuario);

    void actualizar(Proveedor proveedor, String usuario);

    void eliminar(int id, String usuario);
}
