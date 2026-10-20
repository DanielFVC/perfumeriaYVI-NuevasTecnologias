package com.perfumeriayvi.inventario.endpoint;

import com.perfumeriayvi.inventario.microservicio.ReabastecimientoMicroservicio;
import com.perfumeriayvi.inventario.modelo.RespuestaReabastecimiento;
import com.perfumeriayvi.inventario.modelo.SolicitudReabastecimiento;
import com.perfumeriayvi.inventario.validador.ReabastecimientoValidador;
import com.perfumeriayvi.inventario.validador.ValidacionException;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * ENDPOINT REST del microservicio MS-01 Inventario.
 *
 * Es la "puerta de entrada": recibe la petición HTTP, la pasa al validador y, si es válida,
 * al microservicio que hace el cálculo. Devuelve siempre JSON.
 *
 *   POST http://localhost:8091/api/v1/reabastecimiento        (cuerpo JSON)
 *   GET  http://localhost:8091/api/v1/reabastecimiento?...    (los mismos datos en la URL, para el navegador)
 *   GET  http://localhost:8091/api/v1/info                    (qué hace el servicio)
 *   GET  http://localhost:8091/actuator/health                (salud, para OPERATE)
 *
 * Respuestas: 200 con el cálculo · 400 con la lista de errores del validador.
 */
@RestController
@RequestMapping("/api/v1")
public class ReabastecimientoEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(ReabastecimientoEndpoint.class);

    private final ReabastecimientoValidador validador;
    private final ReabastecimientoMicroservicio microservicio;

    public ReabastecimientoEndpoint(ReabastecimientoValidador validador,
                                    ReabastecimientoMicroservicio microservicio) {
        this.validador = validador;
        this.microservicio = microservicio;
    }

    /** Cálculo con los datos en JSON (forma normal de consumir el microservicio). */
    @PostMapping("/reabastecimiento")
    public RespuestaReabastecimiento calcular(@RequestBody SolicitudReabastecimiento solicitud) {
        validador.validar(solicitud);                                  // 1. validar
        RespuestaReabastecimiento respuesta = microservicio.calcular(solicitud);  // 2. calcular
        LOG.info("REABASTECIMIENTO producto={} stock={} nivel={} pedir={}",
                respuesta.idProducto(), solicitud.stockActual(), respuesta.nivel(), respuesta.cantidadSugerida());
        return respuesta;                                              // 3. responder
    }

    /** El mismo cálculo con los datos en la URL, para probarlo desde el navegador. */
    @GetMapping("/reabastecimiento")
    public RespuestaReabastecimiento calcularDesdeUrl(
            @RequestParam(required = false) Integer idProducto,
            @RequestParam(required = false) String nombreProducto,
            @RequestParam(required = false) Integer stockActual,
            @RequestParam(required = false) Integer stockMinimo,
            @RequestParam(required = false) Double ventasDiarias,
            @RequestParam(required = false) Integer diasEntregaProveedor,
            @RequestParam(required = false) Integer diasPlanificacion) {
        return calcular(new SolicitudReabastecimiento(idProducto, nombreProducto, stockActual,
                stockMinimo, ventasDiarias, diasEntregaProveedor, diasPlanificacion));
    }

    /** Información del servicio. */
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("servicio", "ms-inventario");
        info.put("version", "1.0.0");
        info.put("descripcion", "Calcula si un producto necesita reabastecerse y cuántas unidades pedir (apoya RF06).");
        info.put("endpoints", List.of(
                "POST /api/v1/reabastecimiento",
                "GET  /api/v1/reabastecimiento?idProducto=1&stockActual=4&stockMinimo=5&ventasDiarias=2&diasEntregaProveedor=3",
                "GET  /actuator/health"));
        return info;
    }

    // ------------------------------------------------------------------ errores -> HTTP 400

    @ExceptionHandler(ValidacionException.class)
    public ResponseEntity<Map<String, Object>> datosInvalidos(ValidacionException e) {
        LOG.warn("VALIDACION_FALLIDA errores={}", e.getErrores().size());
        return error("Los datos enviados no son válidos.", e.getErrores());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> formatoInvalido(Exception e) {
        LOG.warn("FORMATO_INVALIDO {}", e.getClass().getSimpleName());
        return error("El formato de la petición no es válido.",
                List.of("Revise que el JSON esté bien escrito y que los números no tengan letras."));
    }

    private ResponseEntity<Map<String, Object>> error(String mensaje, List<String> errores) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("estado", 400);
        cuerpo.put("mensaje", mensaje);
        cuerpo.put("errores", errores);
        cuerpo.put("fecha", OffsetDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(cuerpo);
    }
}
