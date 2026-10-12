package com.mycompany.perfumeriayvi.model.repository;

import com.mycompany.perfumeriayvi.model.entity.Proveedor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC. Requisitos: RF05 (CU05, HU04) gestionar proveedores.
 * RNF-S5: todas las consultas usan parámetros (?) para evitar inyección SQL.
 * RNF-S7: registrarUsuarioAuditoria() deja el usuario en @app_user para los triggers.
 */
@Repository
public class JdbcProveedorRepository extends RepositorioBase implements ProveedorRepository {

    private static final RowMapper<Proveedor> MAPPER = (rs, i) -> {
        Proveedor p = new Proveedor();
        p.setIdProveedor(rs.getInt("id_proveedor"));
        p.setNombre(rs.getString("nombre"));
        p.setNit(rs.getString("nit"));
        p.setCiudad(rs.getString("ciudad"));
        p.setBarrio(rs.getString("barrio"));
        p.setTipoVia(rs.getString("tipo_via"));
        p.setNumeroVia(rs.getString("numero_via"));
        p.setComplemento(rs.getString("complemento"));
        p.setTipoProveedor(rs.getString("tipo_proveedor"));
        p.setTelefono(rs.getString("telefono"));
        p.setEmail(rs.getString("email"));
        return p;
    };

    public JdbcProveedorRepository(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public List<Proveedor> listar() {
        return jdbc.query("SELECT * FROM proveedor ORDER BY nombre", MAPPER);
    }

    @Override
    public Optional<Proveedor> buscarPorId(int id) {
        return jdbc.query("SELECT * FROM proveedor WHERE id_proveedor = ?", MAPPER, id)
                .stream().findFirst();
    }

    @Override
    public boolean existeNit(String nit, int excluirId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM proveedor WHERE nit = ? AND id_proveedor <> ?",
                Integer.class, nit, excluirId);
        return n != null && n > 0;
    }

    @Override
    @Transactional
    public void registrar(Proveedor p, String usuario) {
        registrarUsuarioAuditoria(usuario);
        jdbc.update("""
                INSERT INTO proveedor
                (nombre, nit, ciudad, barrio, tipo_via, numero_via, complemento,
                 tipo_proveedor, telefono, email)
                VALUES (?,?,?,?,?,?,?,?,?,?)
                """,
                p.getNombre(), p.getNit(), p.getCiudad(), p.getBarrio(),
                p.getTipoVia(), p.getNumeroVia(), p.getComplemento(),
                p.getTipoProveedor(), p.getTelefono(), p.getEmail());
    }

    @Override
    @Transactional
    public void actualizar(Proveedor p, String usuario) {
        registrarUsuarioAuditoria(usuario);
        jdbc.update("""
                UPDATE proveedor
                SET nombre=?, nit=?, ciudad=?, barrio=?, tipo_via=?, numero_via=?,
                    complemento=?, tipo_proveedor=?, telefono=?, email=?
                WHERE id_proveedor=?
                """,
                p.getNombre(), p.getNit(), p.getCiudad(), p.getBarrio(),
                p.getTipoVia(), p.getNumeroVia(), p.getComplemento(),
                p.getTipoProveedor(), p.getTelefono(), p.getEmail(),
                p.getIdProveedor());
    }

    @Override
    @Transactional
    public void eliminar(int id, String usuario) {
        registrarUsuarioAuditoria(usuario);
        jdbc.update("DELETE FROM proveedor WHERE id_proveedor = ?", id);
    }
}
