package com.mycompany.perfumeriayvi.model.repository;

import com.mycompany.perfumeriayvi.model.entity.Cliente;

import java.util.List;
import java.util.Optional;

/**
 * Persistencia de clientes.
 * Requisitos: RF04 (CU04, HU03) gestionar clientes.
 * RNF-F3: numero_documento es único en la BD; un cliente con ventas no se puede eliminar (FK).
 */
public interface ClienteRepository {

    List<Cliente> listar();

    Optional<Cliente> buscarPorId(int id);

    boolean existeDocumento(String numeroDocumento, int excluirId);

    void registrar(Cliente cliente, String usuario);

    void actualizar(Cliente cliente, String usuario);

    void eliminar(int id, String usuario);
}
