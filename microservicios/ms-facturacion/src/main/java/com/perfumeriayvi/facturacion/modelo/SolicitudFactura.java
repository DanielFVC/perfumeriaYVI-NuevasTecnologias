package com.perfumeriayvi.facturacion.modelo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Datos que recibe el microservicio para calcular una factura.
 *
 * @param tipoDocumento        CC, TI, CE, PAS (personas) o NIT (empresas)
 * @param numeroDocumento      número del documento; el NIT va con dígito de verificación: 890903938-8
 * @param nombreCliente        nombre del cliente
 * @param porcentajeDescuento  descuento de 0 a 50 % (opcional, por defecto 0)
 * @param items                productos vendidos
 */
public record SolicitudFactura(
        String tipoDocumento,
        String numeroDocumento,
        String nombreCliente,
        BigDecimal porcentajeDescuento,
        List<Item> items) {

    /**
     * Un producto de la factura.
     *
     * @param idProducto      identificador del producto en Perfumería YVI
     * @param descripcion     nombre del producto (opcional)
     * @param cantidad        unidades vendidas
     * @param precioUnitario  precio de venta por unidad en pesos (sin IVA)
     */
    public record Item(Integer idProducto, String descripcion, Integer cantidad, BigDecimal precioUnitario) {
    }

    /** Descuento a aplicar, 0 si no se envió. */
    public BigDecimal descuentoEfectivo() {
        return porcentajeDescuento == null ? BigDecimal.ZERO : porcentajeDescuento;
    }
}
