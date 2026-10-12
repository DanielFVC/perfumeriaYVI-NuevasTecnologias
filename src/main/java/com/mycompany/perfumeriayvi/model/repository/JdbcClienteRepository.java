package com.mycompany.perfumeriayvi.model.repository;

import com.mycompany.perfumeriayvi.model.entity.Cliente;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC. Requisitos: RF04 (CU04, HU03) gestionar clientes.
 * RNF-S5: todas las consultas usan parámetros (?) para evitar inyección SQL.
 * RNF-S7: registrarUsuarioAuditoria() deja el usuario en @app_user para los triggers.
 */
@Repository
public class JdbcClienteRepository extends RepositorioBase implements ClienteRepository {

    private static final RowMapper<Cliente> MAPPER = (rs, i) -> {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setNombre(rs.getString("nombre"));
        c.setTelefono(rs.getString("telefono"));
        c.setCiudad(rs.getString("ciudad"));
        c.setTipoDocumento(rs.getString("tipo_documento"));
        c.setNumeroDocumento(rs.getString("numero_documento"));
        c.setEmail(rs.getString("email"));
        c.setBarrio(rs.getString("barrio"));
        c.setTipoVia(rs.getString("tipo_via"));
        c.setNumeroVia(rs.getString("numero_via"));
        c.setComplemento(rs.getString("complemento"));
        return c;
    };

    public JdbcClienteRepository(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public List<Cliente> listar() {
        return jdbc.query("SELECT * FROM cliente ORDER BY nombre", MAPPER);
    }

    @Override
    public Optional<Cliente> buscarPorId(int id) {
        return jdbc.query("SELECT * FROM cliente WHERE id_cliente = ?", MAPPER, id)
                .stream().findFirst();
    }

    @Override
    public boolean existeDocumento(String numeroDocumento, int excluirId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM cliente WHERE numero_documento = ? AND id_cliente <> ?",
                Integer.class, numeroDocumento, excluirId);
        return n != null && n > 0;
    }

    @Override
    @Transactional
    public void registrar(Cliente c, String usuario) {
        registrarUsuarioAuditoria(usuario);
        jdbc.update("""
                INSERT INTO cliente
                (nombre, telefono, ciudad, tipo_documento, numero_documento, email,
                 barrio, tipo_via, numero_via, complemento)
                VALUES (?,?,?,?,?,?,?,?,?,?)
                """,
                c.getNombre(), c.getTelefono(), c.getCiudad(),
                c.getTipoDocumento(), c.getNumeroDocumento(), c.getEmail(),
                c.getBarrio(), c.getTipoVia(), c.getNumeroVia(), c.getComplemento());
    }

    @Override
    @Transactional
    public void actualizar(Cliente c, String usuario) {
        registrarUsuarioAuditoria(usuario);
        jdbc.update("""
                UPDATE cliente SET
                nombre=?, telefono=?, ciudad=?, tipo_documento=?, numero_documento=?, email=?,
                barrio=?, tipo_via=?, numero_via=?, complemento=?
                WHERE id_cliente=?
                """,
                c.getNombre(), c.getTelefono(), c.getCiudad(),
                c.getTipoDocumento(), c.getNumeroDocumento(), c.getEmail(),
                c.getBarrio(), c.getTipoVia(), c.getNumeroVia(), c.getComplemento(),
                c.getIdCliente());
    }

    @Override
    @Transactional
    public void eliminar(int id, String usuario) {
        registrarUsuarioAuditoria(usuario);
        jdbc.update("DELETE FROM cliente WHERE id_cliente = ?", id);
    }
}
