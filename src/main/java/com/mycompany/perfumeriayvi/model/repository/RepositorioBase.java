package com.mycompany.perfumeriayvi.model.repository;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Base común de los repositorios JDBC.
 *
 * Los triggers de auditoría de MySQL leen la variable de sesión @app_user
 * para saber qué usuario de la aplicación hizo el cambio. Como la variable
 * vive en la conexión, los métodos que la usan deben ser @Transactional
 * (así el SET y el INSERT/UPDATE/DELETE usan la misma conexión).
 */
public abstract class RepositorioBase {

    protected final JdbcTemplate jdbc;

    protected RepositorioBase(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    protected void registrarUsuarioAuditoria(String usuario) {
        if (usuario != null && !usuario.isBlank()) {
            jdbc.update("SET @app_user = ?", usuario);
        }
    }
}
