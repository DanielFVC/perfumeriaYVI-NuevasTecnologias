package com.perfumeriayvi.facturacion.modelo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Factura calculada que devuelve el microservicio en JSON. Todos los valores en pesos colombianos.
 *
 * @param cliente              tipo y número de documento del cliente
 * @param nombreCliente        nombre del cliente
 * @param lineas               detalle por producto
 * @param unidades             total de unidades vendidas
 * @param subtotal             suma de las líneas antes de descuento
 * @param porcentajeDescuento  descuento aplicado (%)
 * @param descuento            valor del descuento
 * @param baseGravable         subtotal menos descuento (sobre esto se cobra el IVA)
 * @param porcentajeIva        tarifa de IVA (19 %)
 * @param iva                  valor del IVA
 * @param total                valor a pagar
 */
public record RespuestaFactura(
        String cliente,
        String nombreCliente,
        List<Linea> lineas,
        int unidades,
        BigDecimal subtotal,
        BigDecimal porcentajeDescuento,
        BigDecimal descuento,
        BigDecimal baseGravable,
        BigDecimal porcentajeIva,
        BigDecimal iva,
        BigDecimal total) {

    /** Detalle de un producto: cantidad × precio unitario. */
    public record Linea(Integer idProducto, String descripcion, int cantidad,
                        BigDecimal precioUnitario, BigDecimal totalLinea) {
    }
}
