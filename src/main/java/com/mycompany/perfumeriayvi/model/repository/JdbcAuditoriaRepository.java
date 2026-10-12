package com.mycompany.perfumeriayvi.model.repository;

import com.mycompany.perfumeriayvi.model.entity.Auditoria;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Requisitos: RF11 (CU11), RF12 (CU12), RNF-R2 (paginación).
 * Las consultas usan parámetros (?) para evitar inyección SQL (RNF-S5).
 */
@Repository
public class JdbcAuditoriaRepository extends RepositorioBase implements AuditoriaRepository {

    public JdbcAuditoriaRepository(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public List<Auditoria> buscar(FiltroAuditoria filtro, int limite, int desplazamiento) {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT id_auditoria, fecha, tabla_afectada, operacion, id_registro, usuario, detalle "
                + "FROM auditoria_ventas WHERE 1=1 " + where(filtro, params)
                + "ORDER BY fecha DESC, id_auditoria DESC LIMIT ? OFFSET ?";
        params.add(limite);
        params.add(desplazamiento);

        return jdbc.query(sql, (rs, i) -> new Auditoria(
                rs.getInt("id_auditoria"),
                rs.getTimestamp("fecha"),
                rs.getString("tabla_afectada"),
                rs.getString("operacion"),
                rs.getInt("id_registro"),
                rs.getString("usuario"),
                rs.getString("detalle")), params.toArray());
    }

    @Override
    public int contar(FiltroAuditoria filtro) {
        List<Object> params = new ArrayList<>();
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM auditoria_ventas WHERE 1=1 " + where(filtro, params),
                Integer.class, params.toArray());
        return total == null ? 0 : total;
    }

    @Override
    public Map<String, Integer> contarPorOperacion(FiltroAuditoria filtro) {
        List<Object> params = new ArrayList<>();
        Map<String, Integer> conteo = new LinkedHashMap<>();
        jdbc.query("SELECT operacion, COUNT(*) total FROM auditoria_ventas WHERE 1=1 "
                        + where(filtro, params) + "GROUP BY operacion ORDER BY operacion",
                rs -> {
                    conteo.put(rs.getString("operacion"), rs.getInt("total"));
                }, params.toArray());
        return conteo;
    }

    private String where(FiltroAuditoria f, List<Object> params) {
        StringBuilder sql = new StringBuilder();
        if (tieneValor(f.tabla())) {
            sql.append("AND tabla_afectada = ? ");
            params.add(f.tabla());
        }
        if (tieneValor(f.usuario())) {
            sql.append("AND usuario LIKE ? ");
            params.add("%" + f.usuario().trim() + "%");
        }
        if (tieneValor(f.operacion())) {
            sql.append("AND operacion = ? ");
            params.add(f.operacion());
        }
        return sql.toString();
    }

    private boolean tieneValor(String s) {
        return s != null && !s.isBlank();
    }
}
