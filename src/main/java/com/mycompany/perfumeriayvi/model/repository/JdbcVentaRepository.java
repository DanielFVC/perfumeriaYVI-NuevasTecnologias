package com.mycompany.perfumeriayvi.model.repository;

import com.mycompany.perfumeriayvi.model.entity.DetalleVenta;
import com.mycompany.perfumeriayvi.model.entity.Venta;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Requisitos: RF07 (CU07, HU06), RF08 (CU08, HU07).
 * RNF-F1: las operaciones de escritura se ejecutan dentro de la transacción
 * abierta por VentaService (@Transactional): si falla una línea se revierte todo.
 */
@Repository
public class JdbcVentaRepository extends RepositorioBase implements VentaRepository {

    private static final RowMapper<Venta> MAPPER = (rs, i) -> {
        Venta v = new Venta();
        v.setId(rs.getInt("id_venta"));
        v.setFecha(rs.getString("fecha"));
        v.setIdCliente(rs.getInt("id_cliente"));
        v.setCliente(rs.getString("cliente"));
        v.setTotal(rs.getDouble("total"));
        v.setIdUser(rs.getInt("id_user"));
        v.setVendedor(rs.getString("vendedor"));
        return v;
    };

    private static final String SELECT_BASE = """
            SELECT v.id_venta, v.fecha, v.id_cliente, v.total, v.id_user,
                   c.nombre AS cliente, u.full_name AS vendedor
            FROM venta v
            JOIN cliente c ON v.id_cliente = c.id_cliente
            JOIN users u ON v.id_user = u.id
            """;

    public JdbcVentaRepository(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public List<Venta> listar() {
        return jdbc.query(SELECT_BASE + " ORDER BY v.fecha DESC, v.id_venta DESC", MAPPER);
    }

    @Override
    public Optional<Venta> buscarPorId(int id) {
        Optional<Venta> venta = jdbc.query(SELECT_BASE + " WHERE v.id_venta = ?", MAPPER, id)
                .stream().findFirst();
        venta.ifPresent(v -> v.setDetalles(detalles(id)));
        return venta;
    }

    @Override
    public List<DetalleVenta> detalles(int idVenta) {
        return jdbc.query("""
                SELECT dv.id_producto, p.nombre AS producto, dv.cantidad, dv.subtotal
                FROM detalle_venta dv
                JOIN producto p ON dv.id_producto = p.id_producto
                WHERE dv.id_venta = ?
                """,
                (rs, i) -> new DetalleVenta(
                        rs.getInt("id_producto"),
                        rs.getString("producto"),
                        rs.getInt("cantidad"),
                        rs.getDouble("subtotal")),
                idVenta);
    }

    @Override
    public OptionalDouble precioVenta(int idProducto) {
        List<Double> precio = jdbc.queryForList(
                "SELECT precio_venta FROM producto WHERE id_producto = ?", Double.class, idProducto);
        return precio.isEmpty() || precio.get(0) == null ? OptionalDouble.empty() : OptionalDouble.of(precio.get(0));
    }

    @Override
    public int registrar(Venta v, String usuario) {
        registrarUsuarioAuditoria(usuario);

        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO venta(fecha, id_cliente, total, id_user) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, v.getFecha());
            ps.setInt(2, v.getIdCliente());
            ps.setDouble(3, v.getTotal());
            ps.setInt(4, v.getIdUser());
            return ps;
        }, keys);

        int idVenta = keys.getKey().intValue();
        insertarDetalles(idVenta, v.getDetalles());
        return idVenta;
    }

    @Override
    public void actualizar(Venta v, String usuario) {
        registrarUsuarioAuditoria(usuario);
        jdbc.update("UPDATE venta SET fecha=?, id_cliente=?, total=? WHERE id_venta=?",
                v.getFecha(), v.getIdCliente(), v.getTotal(), v.getId());
        jdbc.update("DELETE FROM detalle_venta WHERE id_venta = ?", v.getId());
        insertarDetalles(v.getId(), v.getDetalles());
    }

    @Override
    public void eliminar(int idVenta, String usuario) {
        registrarUsuarioAuditoria(usuario);
        jdbc.update("DELETE FROM detalle_venta WHERE id_venta = ?", idVenta);
        jdbc.update("DELETE FROM venta WHERE id_venta = ?", idVenta);
    }

    @Override
    public boolean descontarStock(int idProducto, int cantidad) {
        // La condición "stock >= ?" evita dejar el inventario en negativo
        // aunque dos vendedores vendan el mismo producto al mismo tiempo.
        return jdbc.update("UPDATE producto SET stock = stock - ? WHERE id_producto = ? AND stock >= ?",
                cantidad, idProducto, cantidad) == 1;
    }

    @Override
    public void devolverStock(int idProducto, int cantidad) {
        jdbc.update("UPDATE producto SET stock = stock + ? WHERE id_producto = ?", cantidad, idProducto);
    }

    @Override
    public int stockDisponible(int idProducto) {
        Integer stock = jdbc.queryForObject("SELECT stock FROM producto WHERE id_producto = ?",
                Integer.class, idProducto);
        return stock == null ? 0 : stock;
    }

    private void insertarDetalles(int idVenta, List<DetalleVenta> detalles) {
        for (DetalleVenta d : detalles) {
            jdbc.update("INSERT INTO detalle_venta(id_venta, id_producto, cantidad, subtotal) VALUES (?,?,?,?)",
                    idVenta, d.getIdProducto(), d.getCantidad(), d.getSubtotal());
        }
    }
}
