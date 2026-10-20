package com.perfumeriayvi.facturacion.validador;

import com.perfumeriayvi.facturacion.modelo.SolicitudFactura;
import com.perfumeriayvi.facturacion.modelo.SolicitudFactura.Item;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * VALIDADOR del microservicio MS-02 Facturación.
 *
 * Revisa los datos ANTES de calcular. Si algo está mal no se calcula nada y se responden
 * todos los errores juntos (HTTP 400).
 *
 * Reglas del cliente (las mismas de la aplicación principal, RF04):
 *  - tipoDocumento: CC, TI, CE, PAS o NIT.
 *  - CC, TI, CE y PAS: solo números, de 5 a 10 dígitos.
 *  - NIT: 9 dígitos, guion y dígito de verificación (ej. 890903938-8). El dígito se comprueba
 *    con el algoritmo oficial de la DIAN (módulo 11), así se detectan NIT mal digitados.
 *  - nombreCliente obligatorio, máximo 100 caracteres.
 *
 * Reglas de la venta (RF07):
 *  - porcentajeDescuento opcional, entre 0 y 50.
 *  - entre 1 y 50 productos, sin repetir el mismo idProducto.
 *  - idProducto mayor que 0, cantidad entre 1 y 1.000, precioUnitario mayor que 0 y hasta 10.000.000.
 */
@Component
public class FacturaValidador {

    public static final List<String> TIPOS_DOCUMENTO = List.of("CC", "TI", "CE", "PAS", "NIT");

    static final int MAX_ITEMS = 50;
    static final int MAX_CANTIDAD = 1_000;
    static final BigDecimal MAX_PRECIO = new BigDecimal("10000000");
    static final BigDecimal MAX_DESCUENTO = new BigDecimal("50");
    static final int LONGITUD_NOMBRE_MAXIMA = 100;

    private static final Pattern DOCUMENTO_PERSONA = Pattern.compile("^\\d{5,10}$");
    private static final Pattern NIT = Pattern.compile("^(\\d{9})-(\\d)$");

    /** Pesos oficiales de la DIAN para el dígito de verificación, desde el último dígito hacia la izquierda. */
    private static final int[] PESOS_DIAN = {3, 7, 13, 17, 19, 23, 29, 37, 41, 43, 47, 53, 59, 67, 71};

    /**
     * Valida la solicitud completa.
     *
     * @throws ValidacionException con la lista de errores si hay al menos uno
     */
    public void validar(SolicitudFactura s) {
        if (s == null) {
            throw new ValidacionException(List.of("Debe enviar los datos de la factura."));
        }
        List<String> errores = new ArrayList<>(validarDocumento(s.tipoDocumento(), s.numeroDocumento()));

        if (s.nombreCliente() == null || s.nombreCliente().isBlank()) {
            errores.add("El nombreCliente es obligatorio.");
        } else if (s.nombreCliente().length() > LONGITUD_NOMBRE_MAXIMA) {
            errores.add("El nombreCliente no puede tener más de " + LONGITUD_NOMBRE_MAXIMA + " caracteres.");
        }

        BigDecimal descuento = s.descuentoEfectivo();
        if (descuento.signum() < 0 || descuento.compareTo(MAX_DESCUENTO) > 0) {
            errores.add("El porcentajeDescuento debe estar entre 0 y 50.");
        }

        validarItems(s.items(), errores);

        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
    }

    /**
     * Valida solo el documento del cliente. Lo usa también el endpoint GET /api/v1/documentos/validar.
     *
     * @return lista de errores (vacía si el documento es válido)
     */
    public List<String> validarDocumento(String tipo, String numero) {
        List<String> errores = new ArrayList<>();
        if (tipo == null || !TIPOS_DOCUMENTO.contains(tipo)) {
            errores.add("El tipoDocumento debe ser uno de " + TIPOS_DOCUMENTO + ".");
            return errores;
        }
        if (numero == null || numero.isBlank()) {
            errores.add("El numeroDocumento es obligatorio.");
            return errores;
        }
        String limpio = numero.trim();

        if ("NIT".equals(tipo)) {
            var m = NIT.matcher(limpio);
            if (!m.matches()) {
                errores.add("El NIT debe tener 9 dígitos, guion y dígito de verificación (ej. 890903938-8).");
            } else {
                int esperado = digitoVerificacion(m.group(1));
                if (Integer.parseInt(m.group(2)) != esperado) {
                    errores.add("El dígito de verificación del NIT no es correcto (debería ser " + esperado + ").");
                }
            }
        } else if (!DOCUMENTO_PERSONA.matcher(limpio).matches()) {
            errores.add("El numeroDocumento de tipo " + tipo + " debe tener solo números, de 5 a 10 dígitos.");
        }
        return errores;
    }

    /** Dígito de verificación del NIT según la DIAN (módulo 11). */
    public static int digitoVerificacion(String nitSinDigito) {
        int suma = 0;
        int largo = nitSinDigito.length();
        for (int i = 0; i < largo; i++) {
            int digito = nitSinDigito.charAt(largo - 1 - i) - '0';
            suma += digito * PESOS_DIAN[i];
        }
        int residuo = suma % 11;
        return residuo > 1 ? 11 - residuo : residuo;
    }

    private void validarItems(List<Item> items, List<String> errores) {
        if (items == null || items.isEmpty()) {
            errores.add("La factura debe tener al menos un producto.");
            return;
        }
        if (items.size() > MAX_ITEMS) {
            errores.add("La factura no puede tener más de " + MAX_ITEMS + " productos.");
        }
        Set<Integer> vistos = new HashSet<>();
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            String donde = "Producto " + (i + 1) + ": ";
            if (item == null) {
                errores.add(donde + "no puede estar vacío.");
                continue;
            }
            if (item.idProducto() == null || item.idProducto() <= 0) {
                errores.add(donde + "el idProducto debe ser mayor que 0.");
            } else if (!vistos.add(item.idProducto())) {
                errores.add(donde + "el idProducto " + item.idProducto() + " está repetido; sume las cantidades.");
            }
            if (item.descripcion() != null && item.descripcion().length() > LONGITUD_NOMBRE_MAXIMA) {
                errores.add(donde + "la descripción no puede tener más de " + LONGITUD_NOMBRE_MAXIMA + " caracteres.");
            }
            if (item.cantidad() == null || item.cantidad() < 1 || item.cantidad() > MAX_CANTIDAD) {
                errores.add(donde + "la cantidad debe estar entre 1 y " + MAX_CANTIDAD + ".");
            }
            if (item.precioUnitario() == null || item.precioUnitario().signum() <= 0
                    || item.precioUnitario().compareTo(MAX_PRECIO) > 0) {
                errores.add(donde + "el precioUnitario debe ser mayor que 0 y máximo 10.000.000.");
            }
        }
    }
}
