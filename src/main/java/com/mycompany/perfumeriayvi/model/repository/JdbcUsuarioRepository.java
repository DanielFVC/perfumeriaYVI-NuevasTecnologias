package com.mycompany.perfumeriayvi.model.repository;

import com.mycompany.perfumeriayvi.model.entity.Usuario;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Requisitos: RF01 (CU01), RF09 (CU09), RF10 (CU10).
 * RNF-S1: solo se guarda y se lee el hash BCrypt (columna password_hash), nunca la contraseña.
 */
@Repository
public class JdbcUsuarioRepository extends RepositorioBase implements UsuarioRepository {

    private static final String COLUMNAS = """
            SELECT id, username, full_name, role, active, password_hash,
                   acceso_clientes, acceso_productos, acceso_proveedores, acceso_ventas,
                   intentos_fallidos, bloqueado_hasta, ultimo_acceso
            FROM users
            """;

    private static final RowMapper<Usuario> MAPPER = (rs, i) -> {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setFullName(rs.getString("full_name"));
        u.setRole(rs.getString("role"));
        u.setActive(rs.getBoolean("active"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setAccesoClientes(rs.getBoolean("acceso_clientes"));
        u.setAccesoProductos(rs.getBoolean("acceso_productos"));
        u.setAccesoProveedores(rs.getBoolean("acceso_proveedores"));
        u.setAccesoVentas(rs.getBoolean("acceso_ventas"));
        u.setIntentosFallidos(rs.getInt("intentos_fallidos"));
        u.setBloqueadoHasta(aFecha(rs.getTimestamp("bloqueado_hasta")));
        u.setUltimoAcceso(aFecha(rs.getTimestamp("ultimo_acceso")));
        return u;
    };

    private static LocalDateTime aFecha(Timestamp t) {
        return t == null ? null : t.toLocalDateTime();
    }

    public JdbcUsuarioRepository(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        return jdbc.query(COLUMNAS + " WHERE username = ?", MAPPER, username).stream().findFirst();
    }

    @Override
    public Optional<Usuario> buscarPorId(int id) {
        return jdbc.query(COLUMNAS + " WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    @Override
    @Transactional
    public void registrarIntentoFallido(int id, String username, int intentos, LocalDateTime bloquearHasta) {
        registrarUsuarioAuditoria(username);
        // El trigger trg_update_users registra LOGIN_FAIL o BLOQUEO en la auditoría (RNF-S7)
        jdbc.update("UPDATE users SET intentos_fallidos = ?, bloqueado_hasta = ? WHERE id = ?",
                intentos, bloquearHasta == null ? null : Timestamp.valueOf(bloquearHasta), id);
    }

    @Override
    @Transactional
    public void registrarAccesoExitoso(int id, String username, LocalDateTime fecha) {
        registrarUsuarioAuditoria(username);
        jdbc.update("UPDATE users SET intentos_fallidos = 0, bloqueado_hasta = NULL, ultimo_acceso = ? WHERE id = ?",
                Timestamp.valueOf(fecha), id);
    }

    @Override
    public List<Usuario> listarVendedores() {
        return jdbc.query(COLUMNAS + " WHERE role = 'VENDEDOR' ORDER BY full_name", MAPPER);
    }

    @Override
    public boolean existeUsername(String username) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, username);
        return n != null && n > 0;
    }

    @Override
    @Transactional
    public void registrar(Usuario u, String usuarioSesion) {
        registrarUsuarioAuditoria(usuarioSesion);
        jdbc.update("""
                INSERT INTO users (username, password_hash, full_name, role, active, created_at)
                VALUES (?,?,?,?,?,NOW())
                """,
                u.getUsername(), u.getPasswordHash(), u.getFullName(), u.getRole(), u.isActive());
    }

    @Override
    @Transactional
    public void cambiarEstado(int id, boolean activo, String usuarioSesion) {
        registrarUsuarioAuditoria(usuarioSesion);
        jdbc.update("UPDATE users SET active = ? WHERE id = ? AND role = 'VENDEDOR'", activo, id);
    }

    @Override
    @Transactional
    public void actualizarPermisos(int id, boolean clientes, boolean productos,
                                   boolean proveedores, boolean ventas, String usuarioSesion) {
        registrarUsuarioAuditoria(usuarioSesion);
        jdbc.update("""
                UPDATE users
                SET acceso_clientes=?, acceso_productos=?, acceso_proveedores=?, acceso_ventas=?
                WHERE id = ? AND role = 'VENDEDOR'
                """,
                clientes, productos, proveedores, ventas, id);
    }
}
