package com.perfumeriayvi.inventario.endpoint;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.perfumeriayvi.inventario.microservicio.ReabastecimientoMicroservicio;
import com.perfumeriayvi.inventario.validador.ReabastecimientoValidador;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Pruebas del ENDPOINT de MS-01 Inventario: petición HTTP completa con el validador y la lógica reales. */
@WebMvcTest(ReabastecimientoEndpoint.class)
@Import({ReabastecimientoValidador.class, ReabastecimientoMicroservicio.class})
class ReabastecimientoEndpointTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void postConDatosCorrectosResponde200ConElCalculo() throws Exception {
        String json = """
                {"idProducto":1,"nombreProducto":"Aqua Brisa 100 ml","stockActual":4,
                 "stockMinimo":5,"ventasDiarias":2,"diasEntregaProveedor":3}
                """;
        mvc.perform(post("/api/v1/reabastecimiento").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivel").value("CRITICO"))
                .andExpect(jsonPath("$.necesitaPedido").value(true))
                .andExpect(jsonPath("$.cantidadSugerida").value(67));
    }

    @Test
    void getDesdeElNavegadorCalculaIgual() throws Exception {
        mvc.perform(get("/api/v1/reabastecimiento")
                        .param("idProducto", "5").param("stockActual", "36").param("stockMinimo", "5")
                        .param("ventasDiarias", "2").param("diasEntregaProveedor", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivel").value("NORMAL"))
                .andExpect(jsonPath("$.cantidadSugerida").value(0));
    }

    @Test
    void datosInvalidosResponden400ConLaListaDeErrores() throws Exception {
        mvc.perform(post("/api/v1/reabastecimiento").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idProducto\":-3,\"stockActual\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.errores.length()").value(5));
    }

    @Test
    void jsonMalEscritoResponde400() throws Exception {
        mvc.perform(post("/api/v1/reabastecimiento").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idProducto\": \"uno\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El formato de la petición no es válido."));
    }

    @Test
    void letrasEnUnNumeroDeLaUrlResponden400() throws Exception {
        mvc.perform(get("/api/v1/reabastecimiento").param("idProducto", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void infoDescribeElServicio() throws Exception {
        mvc.perform(get("/api/v1/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servicio").value("ms-inventario"));
    }
}
