package com.perfumeriayvi.facturacion.endpoint;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.perfumeriayvi.facturacion.microservicio.FacturaMicroservicio;
import com.perfumeriayvi.facturacion.validador.FacturaValidador;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Pruebas del ENDPOINT de MS-02 Facturación: petición HTTP completa con el validador y la lógica reales. */
@WebMvcTest(FacturaEndpoint.class)
@Import({FacturaValidador.class, FacturaMicroservicio.class})
class FacturaEndpointTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void postConFacturaCorrectaResponde200ConElTotal() throws Exception {
        String json = """
                {"tipoDocumento":"CC","numeroDocumento":"1059202768","nombreCliente":"Andrés Suárez",
                 "porcentajeDescuento":10,
                 "items":[{"idProducto":1,"descripcion":"Aqua Brisa 100 ml","cantidad":2,"precioUnitario":45000},
                          {"idProducto":5,"descripcion":"Cuero Imperial 100 ml","cantidad":1,"precioUnitario":52000}]}
                """;
        mvc.perform(post("/api/v1/facturas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(142000))
                .andExpect(jsonPath("$.iva").value(24282))
                .andExpect(jsonPath("$.total").value(152082));
    }

    @Test
    void facturaInvalidaResponde400ConLosErrores() throws Exception {
        String json = """
                {"tipoDocumento":"NIT","numeroDocumento":"890903938-5","nombreCliente":"Empresa","items":[]}
                """;
        mvc.perform(post("/api/v1/facturas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.length()").value(2))
                .andExpect(jsonPath("$.errores[0]").value("El dígito de verificación del NIT no es correcto (debería ser 8)."));
    }

    @Test
    void jsonMalEscritoResponde400() throws Exception {
        mvc.perform(post("/api/v1/facturas").contentType(MediaType.APPLICATION_JSON).content("{\"items\": [1,"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El formato de la petición no es válido."));
    }

    @Test
    void validarDocumentoDesdeElNavegador() throws Exception {
        mvc.perform(get("/api/v1/documentos/validar").param("tipo", "NIT").param("numero", "890903938-8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true));
        mvc.perform(get("/api/v1/documentos/validar").param("tipo", "CC").param("numero", "12A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false));
    }

    @Test
    void infoDescribeElServicio() throws Exception {
        mvc.perform(get("/api/v1/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servicio").value("ms-facturacion"));
    }
}
