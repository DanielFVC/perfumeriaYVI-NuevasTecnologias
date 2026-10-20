package com.perfumeriayvi.facturacion.microservicio;

import static org.assertj.core.api.Assertions.assertThat;

import com.perfumeriayvi.facturacion.modelo.RespuestaFactura;
import com.perfumeriayvi.facturacion.modelo.SolicitudFactura;
import com.perfumeriayvi.facturacion.modelo.SolicitudFactura.Item;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Pruebas de la lógica de negocio de MS-02 Facturación. */
class FacturaMicroservicioTest {

    private final FacturaMicroservicio microservicio = new FacturaMicroservicio();

    @Test
    void calculaSubtotalDescuentoIvaYTotal() {
        // 2 × 45.000 + 1 × 52.000 = 142.000 ; 10 % = 14.200 ; base 127.800 ; IVA 24.282 ; total 152.082
        var s = new SolicitudFactura("CC", "1059202768", "Andrés Suárez", BigDecimal.TEN, List.of(
                new Item(1, "Aqua Brisa 100 ml", 2, new BigDecimal("45000")),
                new Item(5, "Cuero Imperial 100 ml", 1, new BigDecimal("52000"))));

        RespuestaFactura f = microservicio.calcular(s);

        assertThat(f.cliente()).isEqualTo("CC 1059202768");
        assertThat(f.unidades()).isEqualTo(3);
        assertThat(f.lineas()).hasSize(2);
        assertThat(f.lineas().get(0).totalLinea()).isEqualByComparingTo("90000");
        assertThat(f.subtotal()).isEqualByComparingTo("142000");
        assertThat(f.descuento()).isEqualByComparingTo("14200");
        assertThat(f.baseGravable()).isEqualByComparingTo("127800");
        assertThat(f.iva()).isEqualByComparingTo("24282");
        assertThat(f.total()).isEqualByComparingTo("152082");
    }

    @Test
    void sinDescuentoElIvaEsEl19PorCientoDelSubtotal() {
        var s = new SolicitudFactura("NIT", "890903938-8", "Empresa Ejemplo SAS", null, List.of(
                new Item(6, null, 3, new BigDecimal("28000"))));

        RespuestaFactura f = microservicio.calcular(s);

        assertThat(f.descuento()).isEqualByComparingTo("0");
        assertThat(f.iva()).isEqualByComparingTo("15960");
        assertThat(f.total()).isEqualByComparingTo("99960");
    }

    @Test
    void redondeaAPesosEnterosConLaReglaComercial() {
        // 1 × 999,50 -> 1.000 ; IVA 190 ; total 1.190
        var s = new SolicitudFactura("CC", "12345678", "Cliente", BigDecimal.ZERO, List.of(
                new Item(2, null, 1, new BigDecimal("999.50"))));

        RespuestaFactura f = microservicio.calcular(s);

        assertThat(f.subtotal()).isEqualByComparingTo("1000");
        assertThat(f.iva()).isEqualByComparingTo("190");
        assertThat(f.total()).isEqualByComparingTo("1190");
    }
}
