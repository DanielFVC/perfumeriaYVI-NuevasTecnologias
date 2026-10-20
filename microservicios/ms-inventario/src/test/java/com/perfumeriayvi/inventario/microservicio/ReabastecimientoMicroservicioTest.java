package com.perfumeriayvi.inventario.microservicio;

import static org.assertj.core.api.Assertions.assertThat;

import com.perfumeriayvi.inventario.modelo.RespuestaReabastecimiento;
import com.perfumeriayvi.inventario.modelo.RespuestaReabastecimiento.Nivel;
import com.perfumeriayvi.inventario.modelo.SolicitudReabastecimiento;
import org.junit.jupiter.api.Test;

/** Pruebas de la lógica de negocio de MS-01 Inventario. */
class ReabastecimientoMicroservicioTest {

    private final ReabastecimientoMicroservicio microservicio = new ReabastecimientoMicroservicio();

    @Test
    void productoAgotadoPideTodoElStockObjetivo() {
        // punto de reorden = 2 × 3 + 5 = 11 ; objetivo = 11 + 2 × 30 = 71
        RespuestaReabastecimiento r = microservicio.calcular(
                new SolicitudReabastecimiento(1, "Aqua Brisa 100 ml", 0, 5, 2.0, 3, 30));
        assertThat(r.nivel()).isEqualTo(Nivel.AGOTADO);
        assertThat(r.necesitaPedido()).isTrue();
        assertThat(r.puntoReorden()).isEqualTo(11);
        assertThat(r.cantidadSugerida()).isEqualTo(71);
        assertThat(r.diasCobertura()).isEqualTo(0.0);
    }

    @Test
    void stockIgualAlMinimoEsCritico() {
        RespuestaReabastecimiento r = microservicio.calcular(
                new SolicitudReabastecimiento(2, null, 5, 5, 2.0, 3, 30));
        assertThat(r.nivel()).isEqualTo(Nivel.CRITICO);
        assertThat(r.cantidadSugerida()).isEqualTo(66);
        assertThat(r.mensaje()).contains("El producto 2", "stock bajo");
    }

    @Test
    void stockQueNoAlcanzaMientrasLlegaElPedidoEsReorden() {
        // stock 10 ≤ punto de reorden 11 -> hay que pedir aunque esté sobre el mínimo
        RespuestaReabastecimiento r = microservicio.calcular(
                new SolicitudReabastecimiento(3, "Madera Salvaje", 10, 5, 2.0, 3, 30));
        assertThat(r.nivel()).isEqualTo(Nivel.REORDEN);
        assertThat(r.necesitaPedido()).isTrue();
        assertThat(r.cantidadSugerida()).isEqualTo(61);
        assertThat(r.diasCobertura()).isEqualTo(5.0);
    }

    @Test
    void stockSuficienteNoPide() {
        RespuestaReabastecimiento r = microservicio.calcular(
                new SolicitudReabastecimiento(5, "Cuero Imperial", 36, 5, 2.0, 3, null));
        assertThat(r.nivel()).isEqualTo(Nivel.NORMAL);
        assertThat(r.necesitaPedido()).isFalse();
        assertThat(r.cantidadSugerida()).isZero();
        assertThat(r.diasCobertura()).isEqualTo(18.0);
    }

    @Test
    void sinVentasLaCoberturaEsNulaYUsaSoloElMinimo() {
        RespuestaReabastecimiento r = microservicio.calcular(
                new SolicitudReabastecimiento(6, null, 3, 5, 0.0, 3, 30));
        assertThat(r.diasCobertura()).isNull();
        assertThat(r.puntoReorden()).isEqualTo(5);
        assertThat(r.cantidadSugerida()).isEqualTo(2);
    }

    @Test
    void redondeaHaciaArribaLasVentasFraccionadas() {
        // 1.5 × 3 = 4.5 -> 5 ; punto de reorden = 5 + 5 = 10
        RespuestaReabastecimiento r = microservicio.calcular(
                new SolicitudReabastecimiento(7, null, 20, 5, 1.5, 3, 30));
        assertThat(r.puntoReorden()).isEqualTo(10);
        assertThat(r.nivel()).isEqualTo(Nivel.NORMAL);
    }
}
