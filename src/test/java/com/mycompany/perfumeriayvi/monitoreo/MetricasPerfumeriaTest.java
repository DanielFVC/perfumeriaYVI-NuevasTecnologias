package com.mycompany.perfumeriayvi.monitoreo;

import com.mycompany.perfumeriayvi.model.entity.Producto;
import com.mycompany.perfumeriayvi.model.service.ProductoService;
import com.mycompany.perfumeriayvi.seguridad.EventosSeguridad;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Etapa MONITOR: los eventos de seguridad y el inventario quedan medidos para Prometheus. */
class MetricasPerfumeriaTest {

    @Test
    void lasMetricasExistenEnCeroDesdeElInicio() {
        SimpleMeterRegistry registro = new SimpleMeterRegistry();
        new MetricasPerfumeria(registro);

        assertEquals(0.0, registro.get("perfumeria.login").tag("resultado", "exitoso").counter().count());
        assertEquals(0.0, registro.get("perfumeria.login").tag("resultado", "fallido").counter().count());
        assertEquals(0.0, registro.get("perfumeria.cuentas.bloqueadas").counter().count());
        assertEquals(0.0, registro.get("perfumeria.ventas").tag("operacion", "corregida").counter().count());
    }

    @Test
    void losEventosDeSeguridadSumanEnLasMetricas() {
        SimpleMeterRegistry registro = new SimpleMeterRegistry();
        EventosSeguridad eventos = new EventosSeguridad(new MetricasPerfumeria(registro));

        eventos.loginExitoso("admin", "127.0.0.1");
        eventos.loginFallido("ana", "127.0.0.1", "contraseña incorrecta");
        eventos.loginFallido("ana", "127.0.0.1", "contraseña incorrecta");
        eventos.cuentaBloqueada("ana", "127.0.0.1");
        eventos.accesoDenegado("carlos", "/usuarios");
        eventos.csrfInvalido("/clientes/eliminar", "127.0.0.1");
        eventos.logout("admin");

        assertEquals(1.0, registro.get("perfumeria.login").tag("resultado", "exitoso").counter().count());
        assertEquals(2.0, registro.get("perfumeria.login").tag("resultado", "fallido").counter().count());
        assertEquals(1.0, registro.get("perfumeria.cuentas.bloqueadas").counter().count());
        assertEquals(1.0, registro.get("perfumeria.accesos.denegados").counter().count());
        assertEquals(1.0, registro.get("perfumeria.csrf.rechazados").counter().count());
    }

    @Test
    void indicadorDeStockBajoUsaLaReglaDeRF06() {
        ProductoService productos = mock(ProductoService.class);
        Producto bajo = new Producto();
        bajo.setStock(2);
        bajo.setStockMinimo(5);
        Producto normal = new Producto();
        normal.setStock(20);
        normal.setStockMinimo(5);
        List<Producto> lista = List.of(bajo, normal);
        when(productos.listar()).thenReturn(lista);
        when(productos.contarStockBajo(lista)).thenReturn(1L);

        SimpleMeterRegistry registro = new SimpleMeterRegistry();
        new MonitoreoConfig().metricasInventario(productos).bindTo(registro);

        assertEquals(1.0, registro.get("perfumeria.productos.stock_bajo").gauge().value());
    }

    @Test
    void siLaBaseDeDatosFallaElIndicadorQuedaSinDato() {
        ProductoService productos = mock(ProductoService.class);
        when(productos.listar()).thenThrow(new RuntimeException("BD caída"));

        assertTrue(Double.isNaN(MonitoreoConfig.contarStockBajo(productos)));
    }
}
