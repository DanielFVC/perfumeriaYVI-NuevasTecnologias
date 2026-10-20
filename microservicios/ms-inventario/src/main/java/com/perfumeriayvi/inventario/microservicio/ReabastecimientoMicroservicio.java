package com.perfumeriayvi.inventario.microservicio;

import com.perfumeriayvi.inventario.modelo.RespuestaReabastecimiento;
import com.perfumeriayvi.inventario.modelo.RespuestaReabastecimiento.Nivel;
import com.perfumeriayvi.inventario.modelo.SolicitudReabastecimiento;
import org.springframework.stereotype.Service;

/**
 * MICROSERVICIO (lógica de negocio) MS-01 Inventario: reabastecimiento.
 *
 * Responde dos preguntas para un producto: ¿hay que pedir ya al proveedor? y ¿cuántas unidades?
 * Apoya el requisito RF06 (alertas de stock bajo) de Perfumería YVI.
 *
 * Fórmulas (modelo clásico de punto de reorden):
 *   puntoReorden     = ventasDiarias × diasEntregaProveedor + stockMinimo
 *                      (lo que se vende mientras llega el pedido, más la reserva de seguridad)
 *   necesitaPedido   = stockActual ≤ puntoReorden
 *   stockObjetivo    = puntoReorden + ventasDiarias × diasPlanificacion
 *   cantidadSugerida = stockObjetivo − stockActual   (solo si necesita pedido)
 *   diasCobertura    = stockActual ÷ ventasDiarias
 *
 * Niveles:
 *   AGOTADO  -> stock en 0
 *   CRITICO  -> stock igual o por debajo del mínimo (lo mismo que "stock bajo" en la app)
 *   REORDEN  -> todavía hay stock, pero no alcanza mientras llega el pedido
 *   NORMAL   -> no hace falta pedir
 */
@Service
public class ReabastecimientoMicroservicio {

    /** Calcula el reabastecimiento. La solicitud ya debe venir validada. */
    public RespuestaReabastecimiento calcular(SolicitudReabastecimiento s) {
        int stock = s.stockActual();
        int minimo = s.stockMinimo();
        double ventas = s.ventasDiarias();

        int puntoReorden = (int) Math.ceil(ventas * s.diasEntregaProveedor()) + minimo;
        boolean necesitaPedido = stock <= puntoReorden;

        int stockObjetivo = puntoReorden + (int) Math.ceil(ventas * s.diasPlanificacionEfectivos());
        int cantidadSugerida = necesitaPedido ? Math.max(0, stockObjetivo - stock) : 0;

        Double diasCobertura = ventas > 0 ? redondear(stock / ventas) : null;

        Nivel nivel;
        if (stock == 0) {
            nivel = Nivel.AGOTADO;
        } else if (stock <= minimo) {
            nivel = Nivel.CRITICO;
        } else if (necesitaPedido) {
            nivel = Nivel.REORDEN;
        } else {
            nivel = Nivel.NORMAL;
        }

        return new RespuestaReabastecimiento(
                s.idProducto(),
                s.nombreProducto(),
                nivel,
                necesitaPedido,
                puntoReorden,
                cantidadSugerida,
                diasCobertura,
                mensaje(nivel, cantidadSugerida, diasCobertura, s));
    }

    private String mensaje(Nivel nivel, int cantidad, Double dias, SolicitudReabastecimiento s) {
        String producto = s.nombreProducto() == null || s.nombreProducto().isBlank()
                ? "El producto " + s.idProducto()
                : s.nombreProducto();
        return switch (nivel) {
            case AGOTADO -> producto + " está agotado. Pida " + cantidad + " unidades de inmediato.";
            case CRITICO -> producto + " está en stock bajo (" + s.stockActual() + " de mínimo "
                    + s.stockMinimo() + "). Pida " + cantidad + " unidades hoy.";
            case REORDEN -> producto + " alcanza para " + formatear(dias) + " días y el proveedor tarda "
                    + s.diasEntregaProveedor() + ". Pida " + cantidad + " unidades.";
            case NORMAL -> producto + " tiene stock suficiente"
                    + (dias == null ? "" : " para " + formatear(dias) + " días") + ". No hace falta pedir.";
        };
    }

    private static Double redondear(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }

    private static String formatear(Double dias) {
        if (dias == null) {
            return "muchos";
        }
        return dias % 1 == 0 ? String.valueOf(dias.intValue()) : String.valueOf(dias);
    }
}
