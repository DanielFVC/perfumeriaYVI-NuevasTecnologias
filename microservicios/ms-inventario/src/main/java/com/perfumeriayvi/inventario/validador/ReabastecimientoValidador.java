package com.perfumeriayvi.inventario.validador;

import com.perfumeriayvi.inventario.modelo.SolicitudReabastecimiento;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * VALIDADOR del microservicio MS-01 Inventario.
 *
 * Revisa los datos ANTES de calcular. Si algo está mal no se hace ningún cálculo y se
 * responden todos los errores juntos (HTTP 400), así el cliente los corrige de una vez.
 *
 * Reglas (las mismas de la aplicación principal: stock y stock mínimo no negativos, RF05/RF06):
 *  - idProducto obligatorio y mayor que 0.
 *  - stockActual y stockMinimo obligatorios, entre 0 y 100.000.
 *  - ventasDiarias obligatorio, entre 0 y 10.000.
 *  - diasEntregaProveedor obligatorio, entre 1 y 60 días.
 *  - diasPlanificacion opcional, entre 1 y 90 días.
 *  - nombreProducto opcional, máximo 100 caracteres (igual que la columna producto.nombre).
 */
@Component
public class ReabastecimientoValidador {

    static final int STOCK_MAXIMO = 100_000;
    static final double VENTAS_DIARIAS_MAXIMAS = 10_000;
    static final int DIAS_ENTREGA_MAXIMOS = 60;
    static final int DIAS_PLANIFICACION_MAXIMOS = 90;
    static final int LONGITUD_NOMBRE_MAXIMA = 100;

    /**
     * Valida la solicitud completa.
     *
     * @throws ValidacionException con la lista de errores si hay al menos uno
     */
    public void validar(SolicitudReabastecimiento s) {
        List<String> errores = new ArrayList<>();

        if (s == null) {
            throw new ValidacionException(List.of("Debe enviar los datos del producto."));
        }

        if (s.idProducto() == null) {
            errores.add("El idProducto es obligatorio.");
        } else if (s.idProducto() <= 0) {
            errores.add("El idProducto debe ser mayor que 0.");
        }

        if (s.nombreProducto() != null && s.nombreProducto().length() > LONGITUD_NOMBRE_MAXIMA) {
            errores.add("El nombreProducto no puede tener más de " + LONGITUD_NOMBRE_MAXIMA + " caracteres.");
        }

        validarRango(errores, "stockActual", s.stockActual(), 0, STOCK_MAXIMO);
        validarRango(errores, "stockMinimo", s.stockMinimo(), 0, STOCK_MAXIMO);

        if (s.ventasDiarias() == null) {
            errores.add("El campo ventasDiarias es obligatorio.");
        } else if (s.ventasDiarias().isNaN() || s.ventasDiarias() < 0
                || s.ventasDiarias() > VENTAS_DIARIAS_MAXIMAS) {
            errores.add("El campo ventasDiarias debe estar entre 0 y " + (int) VENTAS_DIARIAS_MAXIMAS + ".");
        }

        validarRango(errores, "diasEntregaProveedor", s.diasEntregaProveedor(), 1, DIAS_ENTREGA_MAXIMOS);

        if (s.diasPlanificacion() != null) {
            validarRango(errores, "diasPlanificacion", s.diasPlanificacion(), 1, DIAS_PLANIFICACION_MAXIMOS);
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
    }

    private void validarRango(List<String> errores, String campo, Integer valor, int minimo, int maximo) {
        if (valor == null) {
            errores.add("El campo " + campo + " es obligatorio.");
        } else if (valor < minimo || valor > maximo) {
            errores.add("El campo " + campo + " debe estar entre " + minimo + " y " + maximo + ".");
        }
    }
}
