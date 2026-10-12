package com.mycompany.perfumeriayvi.monitoreo;

import com.mycompany.perfumeriayvi.model.service.ProductoService;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Indicadores de inventario para la etapa MONITOR de DevOps.
 *
 * perfumeria_productos_stock_bajo: cantidad de productos cuyo stock es menor o igual
 * al stock mínimo (la misma regla de RF06 / HU05 que muestra la pantalla de productos).
 * Prometheus lo consulta cada pocos segundos; la alerta "StockBajoEnAumento" lo vigila.
 */
@Configuration
public class MonitoreoConfig {

    @Bean
    public MeterBinder metricasInventario(ProductoService productos) {
        return registro -> Gauge.builder("perfumeria.productos.stock_bajo", productos,
                        MonitoreoConfig::contarStockBajo)
                .description("Productos con stock menor o igual al mínimo (RF06)")
                .register(registro);
    }

    /** Si la base de datos no responde se publica NaN (sin dato) en lugar de romper la consulta. */
    static double contarStockBajo(ProductoService productos) {
        try {
            return productos.contarStockBajo(productos.listar());
        } catch (RuntimeException e) {
            return Double.NaN;
        }
    }
}
