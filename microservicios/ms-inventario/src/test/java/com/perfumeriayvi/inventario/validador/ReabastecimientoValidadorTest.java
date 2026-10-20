package com.perfumeriayvi.inventario.validador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.perfumeriayvi.inventario.modelo.SolicitudReabastecimiento;
import org.junit.jupiter.api.Test;

/** Pruebas del VALIDADOR de MS-01 Inventario. */
class ReabastecimientoValidadorTest {

    private final ReabastecimientoValidador validador = new ReabastecimientoValidador();

    private SolicitudReabastecimiento valida() {
        return new SolicitudReabastecimiento(1, "Aqua Brisa 100 ml", 10, 5, 2.0, 3, 30);
    }

    @Test
    void aceptaUnaSolicitudCorrecta() {
        assertThatCode(() -> validador.validar(valida())).doesNotThrowAnyException();
    }

    @Test
    void aceptaQueNoSeEnvienDiasDePlanificacion() {
        var s = new SolicitudReabastecimiento(1, null, 10, 5, 2.0, 3, null);
        assertThatCode(() -> validador.validar(s)).doesNotThrowAnyException();
    }

    @Test
    void rechazaCamposObligatoriosVaciosYLosReportaTodosJuntos() {
        var s = new SolicitudReabastecimiento(null, null, null, null, null, null, null);
        assertThatThrownBy(() -> validador.validar(s))
                .isInstanceOfSatisfying(ValidacionException.class, e -> assertThat(e.getErrores())
                        .hasSize(5)
                        .contains("El idProducto es obligatorio.",
                                "El campo stockActual es obligatorio.",
                                "El campo ventasDiarias es obligatorio."));
    }

    @Test
    void rechazaValoresNegativosYFueraDeRango() {
        var s = new SolicitudReabastecimiento(0, null, -1, -5, -2.0, 0, 120);
        assertThatThrownBy(() -> validador.validar(s))
                .isInstanceOfSatisfying(ValidacionException.class, e -> assertThat(e.getErrores())
                        .hasSize(6)
                        .contains("El idProducto debe ser mayor que 0.",
                                "El campo stockActual debe estar entre 0 y 100000.",
                                "El campo diasEntregaProveedor debe estar entre 1 y 60.",
                                "El campo diasPlanificacion debe estar entre 1 y 90."));
    }

    @Test
    void rechazaNombreDemasiadoLargo() {
        var s = new SolicitudReabastecimiento(1, "x".repeat(101), 10, 5, 2.0, 3, 30);
        assertThatThrownBy(() -> validador.validar(s))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("nombreProducto");
    }

    @Test
    void rechazaSolicitudNula() {
        assertThatThrownBy(() -> validador.validar(null)).isInstanceOf(ValidacionException.class);
    }
}
