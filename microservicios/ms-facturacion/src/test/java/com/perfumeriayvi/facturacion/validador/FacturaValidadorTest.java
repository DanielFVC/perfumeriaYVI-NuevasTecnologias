package com.perfumeriayvi.facturacion.validador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.perfumeriayvi.facturacion.modelo.SolicitudFactura;
import com.perfumeriayvi.facturacion.modelo.SolicitudFactura.Item;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Pruebas del VALIDADOR de MS-02 Facturación. */
class FacturaValidadorTest {

    private final FacturaValidador validador = new FacturaValidador();

    private static Item item(int id, int cantidad, String precio) {
        return new Item(id, "Producto " + id, cantidad, new BigDecimal(precio));
    }

    private static SolicitudFactura factura(String tipo, String numero, List<Item> items) {
        return new SolicitudFactura(tipo, numero, "Laura Pérez Vargas", BigDecimal.TEN, items);
    }

    @Test
    void aceptaUnaFacturaCorrectaConCedula() {
        var s = factura("CC", "1059202768", List.of(item(1, 2, "45000")));
        assertThatCode(() -> validador.validar(s)).doesNotThrowAnyException();
    }

    @Test
    void aceptaNitConDigitoDeVerificacionCorrecto() {
        // NIT de ejemplo con dígito calculado por el algoritmo de la DIAN
        assertThat(FacturaValidador.digitoVerificacion("890903938")).isEqualTo(8);
        assertThat(validador.validarDocumento("NIT", "890903938-8")).isEmpty();
    }

    @Test
    void rechazaNitConDigitoEquivocado() {
        assertThat(validador.validarDocumento("NIT", "890903938-5"))
                .containsExactly("El dígito de verificación del NIT no es correcto (debería ser 8).");
    }

    @Test
    void rechazaNitSinGuion() {
        assertThat(validador.validarDocumento("NIT", "8909039388")).hasSize(1);
    }

    @Test
    void rechazaTipoDeDocumentoDesconocidoYCedulaConLetras() {
        assertThat(validador.validarDocumento("XX", "123")).hasSize(1);
        assertThat(validador.validarDocumento("CC", "10A92768"))
                .containsExactly("El numeroDocumento de tipo CC debe tener solo números, de 5 a 10 dígitos.");
        assertThat(validador.validarDocumento("CC", "1234")).hasSize(1);
        assertThat(validador.validarDocumento("CC", null)).containsExactly("El numeroDocumento es obligatorio.");
    }

    @Test
    void rechazaFacturaSinProductos() {
        assertThatThrownBy(() -> validador.validar(factura("CC", "1059202768", Collections.emptyList())))
                .isInstanceOfSatisfying(ValidacionException.class, e -> assertThat(e.getErrores())
                        .containsExactly("La factura debe tener al menos un producto."));
    }

    @Test
    void rechazaProductosConDatosMalosYRepetidos() {
        var items = List.of(item(1, 2, "45000"), item(1, 1, "45000"), item(0, 0, "-5"));
        assertThatThrownBy(() -> validador.validar(factura("CC", "1059202768", items)))
                .isInstanceOfSatisfying(ValidacionException.class, e -> assertThat(e.getErrores())
                        .containsExactly(
                                "Producto 2: el idProducto 1 está repetido; sume las cantidades.",
                                "Producto 3: el idProducto debe ser mayor que 0.",
                                "Producto 3: la cantidad debe estar entre 1 y 1000.",
                                "Producto 3: el precioUnitario debe ser mayor que 0 y máximo 10.000.000."));
    }

    @Test
    void rechazaDescuentoMayorAl50YClienteSinNombre() {
        var s = new SolicitudFactura("CC", "1059202768", " ", new BigDecimal("60"), List.of(item(1, 1, "1000")));
        assertThatThrownBy(() -> validador.validar(s))
                .isInstanceOfSatisfying(ValidacionException.class, e -> assertThat(e.getErrores())
                        .containsExactly("El nombreCliente es obligatorio.",
                                "El porcentajeDescuento debe estar entre 0 y 50."));
    }

    @Test
    void rechazaSolicitudNula() {
        assertThatThrownBy(() -> validador.validar(null)).isInstanceOf(ValidacionException.class);
    }
}
