package com.mycompany.perfumeriayvi.model.repository;

import com.mycompany.perfumeriayvi.model.entity.Producto;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC. Requisitos: RF06 (CU06, HU05) gestionar productos e inventario.
 * RNF-S5: todas las consultas usan parámetros (?) para evitar inyección SQL.
 * RNF-S7: registrarUsuarioAuditoria() deja el usuario en @app_user para los triggers.
 */
@Repository
public class JdbcProductoRepository extends RepositorioBase implements ProductoRepository {

    private static final RowMapper<Producto> MAPPER = (rs, i) -> {
        Producto p = new Producto();
        p.setId(rs.getInt("id_producto"));
        p.setNombre(rs.getString("nombre"));
        p.setMarca(rs.getString("marca"));
        p.setTipo(rs.getString("tipo"));
        p.setCategoria(rs.getString("categoria"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPrecioCompra(rs.getDouble("precio_compra"));
        p.setPrecioVenta(rs.getDouble("precio_venta"));
        p.setStock(rs.getInt("stock"));
        p.setStockMinimo(rs.getInt("stock_minimo"));
        p.setIdProveedor(rs.getInt("id_proveedor"));
        p.setNombreProveedor(rs.getString("nombre_proveedor"));
        return p;
    };

    private static final String SELECT_BASE = """
            SELECT p.*, pr.nombre AS nombre_proveedor
            FROM producto p
            LEFT JOIN proveedor pr ON p.id_proveedor = pr.id_proveedor
            """;

    public JdbcProductoRepository(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public List<Producto> listar() {
        return jdbc.query(SELECT_BASE + " ORDER BY p.id_producto", MAPPER);
    }

    @Override
    public Optional<Producto> buscarPorId(int id) {
        return jdbc.query(SELECT_BASE + " WHERE p.id_producto = ?", MAPPER, id)
                .stream().findFirst();
    }

    @Override
    @Transactional
    public void registrar(Producto p, String usuario) {
        registrarUsuarioAuditoria(usuario);
        jdbc.update("""
                INSERT INTO producto
                (nombre, marca, tipo, categoria, descripcion, precio_compra,
                 precio_venta, stock, stock_minimo, id_proveedor)
                VALUES (?,?,?,?,?,?,?,?,?,?)
                """,
                p.getNombre(), p.getMarca(), p.getTipo(), p.getCategoria(), p.getDescripcion(),
                p.getPrecioCompra(), p.getPrecioVenta(), p.getStock(), p.getStockMinimo(),
                p.getIdProveedor());
    }

    @Override
    @Transactional
    public void actualizar(Producto p, String usuario) {
        registrarUsuarioAuditoria(usuario);
        jdbc.update("""
                UPDATE producto
                SET nombre=?, marca=?, tipo=?, categoria=?, descripcion=?, precio_compra=?,
                    precio_venta=?, stock=?, stock_minimo=?, id_proveedor=?
                WHERE id_producto=?
                """,
                p.getNombre(), p.getMarca(), p.getTipo(), p.getCategoria(), p.getDescripcion(),
                p.getPrecioCompra(), p.getPrecioVenta(), p.getStock(), p.getStockMinimo(),
                p.getIdProveedor(), p.getId());
    }

    @Override
    @Transactional
    public void eliminar(int id, String usuario) {
        registrarUsuarioAuditoria(usuario);
        jdbc.update("DELETE FROM producto WHERE id_producto = ?", id);
    }
}
