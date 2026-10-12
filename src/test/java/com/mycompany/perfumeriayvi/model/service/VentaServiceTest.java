package com.mycompany.perfumeriayvi.model.service;

import com.mycompany.perfumeriayvi.model.entity.DetalleVenta;
import com.mycompany.perfumeriayvi.model.entity.Venta;
import com.mycompany.perfumeriayvi.model.repository.VentaRepository;
import com.mycompany.perfumeriayvi.monitoreo.MetricasPerfumeria;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Pruebas de RF07 (CU07) y RF08 (CU08): cálculo de la venta y control de stock. */
class VentaServiceTest {

    private VentaRepository repositorio;
    private VentaService servicio;
    private SimpleMeterRegistry metricas;

    @BeforeEach
    void preparar() {
        repositorio = mock(VentaRepository.class);
        metricas = new SimpleMeterRegistry();
        servicio = new VentaService(repositorio, new MetricasPerfumeria(metricas));
        when(repositorio.precioVenta(1)).thenReturn(OptionalDouble.of(35000));
        when(repositorio.precioVenta(2)).thenReturn(OptionalDouble.of(65000));
        when(repositorio.descontarStock(anyInt(), anyInt())).thenReturn(true);
        when(repositorio.registrar(any(), any())).thenReturn(99);
    }

    @Test
    void calculaTotalYAgrupaProductosRepetidos() {
        int id = servicio.registrar("2026-10-07", 4, List.of(1, 2, 1), List.of(1, 2, 2), 7, "vendedor");

        assertEquals(99, id);
        ArgumentCaptor<Venta> venta = ArgumentCaptor.forClass(Venta.class);
        verify(repositorio).registrar(venta.capture(), eq("vendedor"));
        assertEquals(2, venta.getValue().getDetalles().size());
        assertEquals(3 * 35000 + 2 * 65000, venta.getValue().getTotal(), 0.001);
        assertEquals(7, venta.getValue().getIdUser());
        verify(repositorio).descontarStock(1, 3);
        verify(repositorio).descontarStock(2, 2);
        // Monitoreo: la venta queda contada con su monto
        assertEquals(1.0, metricas.get("perfumeria.ventas").tag("operacion", "registrada").counter().count());
        assertEquals(3 * 35000 + 2 * 65000, metricas.get("perfumeria.ventas.monto").summary().totalAmount(), 0.001);
    }

    @Test
    void noRegistraSiNoHayStockSuficiente() {
        when(repositorio.descontarStock(2, 5)).thenReturn(false);
        when(repositorio.stockDisponible(2)).thenReturn(3);

        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar("2026-10-07", 4, List.of(2), List.of(5), 7, "vendedor"));
        assertTrue(e.getMessage().contains("Stock insuficiente"));
        verify(repositorio, never()).registrar(any(), any());
        assertEquals(0.0, metricas.get("perfumeria.ventas").tag("operacion", "registrada").counter().count());
    }

    @Test
    void rechazaCantidadesNoPositivasYFechaInvalida() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar("2026-10-07", 4, List.of(1), List.of(0), 7, "v"));
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar("07/10/2026", 4, List.of(1), List.of(1), 7, "v"));
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar("2026-10-07", 0, List.of(1), List.of(1), 7, "v"));
    }

    @Test
    void eliminarDevuelveElStock() {
        Venta v = new Venta();
        v.setId(10);
        v.setDetalles(List.of(new DetalleVenta(1, "A", 2, 70000), new DetalleVenta(2, "B", 1, 65000)));
        when(repositorio.buscarPorId(10)).thenReturn(Optional.of(v));

        servicio.eliminar(10, "admin");

        verify(repositorio).devolverStock(1, 2);
        verify(repositorio).devolverStock(2, 1);
        assertEquals(1.0, metricas.get("perfumeria.ventas").tag("operacion", "anulada").counter().count());
        verify(repositorio).eliminar(10, "admin");
    }

    @Test
    void actualizarDevuelveStockAnteriorYDescuentaElNuevo() {
        Venta anterior = new Venta();
        anterior.setId(10);
        anterior.setIdUser(3);
        anterior.setDetalles(List.of(new DetalleVenta(1, "A", 2, 70000)));
        when(repositorio.buscarPorId(10)).thenReturn(Optional.of(anterior));

        servicio.actualizar(10, "2026-10-08", 4, List.of(2), List.of(1), "admin");

        verify(repositorio).devolverStock(1, 2);
        verify(repositorio).descontarStock(2, 1);
        verify(repositorio).actualizar(argThat(v -> v.getId() == 10 && v.getIdUser() == 3), eq("admin"));
    }
}
