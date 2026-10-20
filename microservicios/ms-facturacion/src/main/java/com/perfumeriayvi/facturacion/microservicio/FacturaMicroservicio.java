package com.perfumeriayvi.facturacion.microservicio;

import com.perfumeriayvi.facturacion.modelo.RespuestaFactura;
import com.perfumeriayvi.facturacion.modelo.RespuestaFactura.Linea;
import com.perfumeriayvi.facturacion.modelo.SolicitudFactura;
import com.perfumeriayvi.facturacion.modelo.SolicitudFactura.Item;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * MICROSERVICIO (lógica de negocio) MS-02 Facturación.
 *
 * Calcula los valores de una venta de Perfumería YVI (apoya RF07 y RF08):
 *
 *   totalLinea   = cantidad × precioUnitario            (por cada producto)
 *   subtotal     = suma de los totalLinea
 *   descuento    = subtotal × porcentajeDescuento ÷ 100
 *   baseGravable = subtotal − descuento
 *   iva          = baseGravable × 19 %                   (tarifa general de IVA en Colombia)
 *   total        = baseGravable + iva
 *
 * Se usa BigDecimal (no double) para que no haya errores de centavos, y cada valor se
 * redondea a pesos enteros con la regla comercial (0,5 hacia arriba).
 */
@Service
public class FacturaMicroservicio {

    public static final BigDecimal PORCENTAJE_IVA = new BigDecimal("19");
    private static final BigDecimal CIEN = new BigDecimal("100");

    /** Calcula la factura. La solicitud ya debe venir validada. */
    public RespuestaFactura calcular(SolicitudFactura s) {
        List<Linea> lineas = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int unidades = 0;

        for (Item item : s.items()) {
            BigDecimal totalLinea = pesos(item.precioUnitario().multiply(BigDecimal.valueOf(item.cantidad())));
            lineas.add(new Linea(item.idProducto(), item.descripcion(), item.cantidad(),
                    pesos(item.precioUnitario()), totalLinea));
            subtotal = subtotal.add(totalLinea);
            unidades += item.cantidad();
        }

        BigDecimal porcentajeDescuento = s.descuentoEfectivo();
        BigDecimal descuento = pesos(subtotal.multiply(porcentajeDescuento).divide(CIEN));
        BigDecimal base = subtotal.subtract(descuento);
        BigDecimal iva = pesos(base.multiply(PORCENTAJE_IVA).divide(CIEN));
        BigDecimal total = base.add(iva);

        return new RespuestaFactura(
                s.tipoDocumento() + " " + s.numeroDocumento().trim(),
                s.nombreCliente().trim(),
                lineas,
                unidades,
                subtotal,
                porcentajeDescuento,
                descuento,
                base,
                PORCENTAJE_IVA,
                iva,
                total);
    }

    /** Redondea a pesos enteros (0,5 hacia arriba). */
    private static BigDecimal pesos(BigDecimal valor) {
        return valor.setScale(0, RoundingMode.HALF_UP);
    }
}
