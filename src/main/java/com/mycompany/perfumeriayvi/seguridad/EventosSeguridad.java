package com.mycompany.perfumeriayvi.seguridad;

import com.mycompany.perfumeriayvi.monitoreo.MetricasPerfumeria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Bitácora de eventos de seguridad en el log de la aplicación (logger "SEGURIDAD").
 *
 * Requisito: RNF-S7. Se registran inicios de sesión, fallos, bloqueos, cierres de sesión,
 * accesos denegados y tokens CSRF inválidos. Nunca se registran contraseñas.
 * Además cada evento suma en las métricas de monitoreo (MetricasPerfumeria) para verlo en Grafana.
 */
@Component
public class EventosSeguridad {

    private static final Logger LOG = LoggerFactory.getLogger("SEGURIDAD");

    private final MetricasPerfumeria metricas;

    public EventosSeguridad(MetricasPerfumeria metricas) {
        this.metricas = metricas;
    }

    /** Evita que un usuario malicioso inserte saltos de línea falsos en el log. */
    static String limpio(String valor) {
        if (valor == null) {
            return "-";
        }
        String s = valor.replaceAll("[\\r\\n\\t]", "_");
        return s.length() > 100 ? s.substring(0, 100) : s;
    }

    public void loginExitoso(String usuario, String ip) {
        metricas.loginExitoso();
        LOG.info("LOGIN_OK usuario={} ip={}", limpio(usuario), limpio(ip));
    }

    public void loginFallido(String usuario, String ip, String motivo) {
        metricas.loginFallido();
        LOG.warn("LOGIN_FAIL usuario={} ip={} motivo={}", limpio(usuario), limpio(ip), limpio(motivo));
    }

    public void cuentaBloqueada(String usuario, String ip) {
        metricas.cuentaBloqueada();
        LOG.warn("BLOQUEO usuario={} ip={}", limpio(usuario), limpio(ip));
    }

    public void logout(String usuario) {
        LOG.info("LOGOUT usuario={}", limpio(usuario));
    }

    public void accesoDenegado(String usuario, String ruta) {
        metricas.accesoDenegado();
        LOG.warn("ACCESO_DENEGADO usuario={} ruta={}", limpio(usuario), limpio(ruta));
    }

    public void csrfInvalido(String ruta, String ip) {
        metricas.csrfRechazado();
        LOG.warn("CSRF_INVALIDO ruta={} ip={}", limpio(ruta), limpio(ip));
    }
}
