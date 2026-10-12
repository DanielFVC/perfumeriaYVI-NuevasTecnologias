package com.mycompany.perfumeriayvi.monitoreo;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.stereotype.Component;

/**
 * Métricas propias de la Perfumería YVI para la etapa MONITOR de DevOps.
 *
 * Prometheus las lee en /actuator/prometheus y Grafana las muestra en el tablero
 * "Perfumería YVI - Monitoreo". Sirven para comprobar en vivo los requisitos no funcionales:
 * - RNF-S2: intentos fallidos y cuentas bloqueadas.
 * - RNF-S4: peticiones rechazadas por token CSRF inválido.
 * - RNF-S7: eventos de seguridad (inicios de sesión, accesos denegados).
 * - RF07 / RF08: ventas registradas, corregidas y anuladas, y montos vendidos.
 *
 * Los tiempos de respuesta (RNF-R1) y las conexiones a la BD (RNF-R3) los mide
 * Spring Boot automáticamente (http_server_requests_seconds y hikaricp_connections).
 *
 * Nombres en Prometheus:
 *   perfumeria_login_total{resultado="exitoso|fallido"}
 *   perfumeria_cuentas_bloqueadas_total
 *   perfumeria_accesos_denegados_total
 *   perfumeria_csrf_rechazados_total
 *   perfumeria_ventas_total{operacion="registrada|corregida|anulada"}
 *   perfumeria_ventas_monto_pesos_sum / _count
 */
@Component
public class MetricasPerfumeria {

    private final Counter loginExitoso;
    private final Counter loginFallido;
    private final Counter cuentasBloqueadas;
    private final Counter accesosDenegados;
    private final Counter csrfRechazados;
    private final Counter ventasRegistradas;
    private final Counter ventasCorregidas;
    private final Counter ventasAnuladas;
    private final DistributionSummary montoVentas;

    public MetricasPerfumeria(MeterRegistry registro) {
        // Se crean todas al iniciar para que aparezcan en Grafana con valor 0 desde el principio
        loginExitoso = contador(registro, "perfumeria.login", "Inicios de sesión (RF01, RNF-S7)", "resultado", "exitoso");
        loginFallido = contador(registro, "perfumeria.login", "Inicios de sesión (RF01, RNF-S7)", "resultado", "fallido");
        cuentasBloqueadas = contador(registro, "perfumeria.cuentas.bloqueadas",
                "Cuentas bloqueadas por 5 intentos fallidos (RNF-S2)", null, null);
        accesosDenegados = contador(registro, "perfumeria.accesos.denegados",
                "Peticiones a módulos sin permiso (RF03, RNF-S7)", null, null);
        csrfRechazados = contador(registro, "perfumeria.csrf.rechazados",
                "Peticiones POST rechazadas por token CSRF inválido (RNF-S4)", null, null);
        ventasRegistradas = contador(registro, "perfumeria.ventas", "Operaciones de ventas (RF07, RF08)", "operacion", "registrada");
        ventasCorregidas = contador(registro, "perfumeria.ventas", "Operaciones de ventas (RF07, RF08)", "operacion", "corregida");
        ventasAnuladas = contador(registro, "perfumeria.ventas", "Operaciones de ventas (RF07, RF08)", "operacion", "anulada");
        montoVentas = DistributionSummary.builder("perfumeria.ventas.monto")
                .description("Total en pesos de cada venta registrada (RF07)")
                .baseUnit("pesos")
                .register(registro);
    }

    private static Counter contador(MeterRegistry registro, String nombre, String descripcion,
                                    String etiqueta, String valor) {
        Counter.Builder builder = Counter.builder(nombre).description(descripcion);
        if (etiqueta != null) {
            builder = builder.tag(etiqueta, valor);
        }
        return builder.register(registro);
    }

    public void loginExitoso() {
        loginExitoso.increment();
    }

    public void loginFallido() {
        loginFallido.increment();
    }

    public void cuentaBloqueada() {
        cuentasBloqueadas.increment();
    }

    public void accesoDenegado() {
        accesosDenegados.increment();
    }

    public void csrfRechazado() {
        csrfRechazados.increment();
    }

    public void ventaRegistrada(double total) {
        ventasRegistradas.increment();
        montoVentas.record(total);
    }

    public void ventaCorregida() {
        ventasCorregidas.increment();
    }

    public void ventaAnulada() {
        ventasAnuladas.increment();
    }
}
