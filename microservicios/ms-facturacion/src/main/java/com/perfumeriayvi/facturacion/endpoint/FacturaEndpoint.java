package com.perfumeriayvi.facturacion.endpoint;

import com.perfumeriayvi.facturacion.microservicio.FacturaMicroservicio;
import com.perfumeriayvi.facturacion.modelo.RespuestaFactura;
import com.perfumeriayvi.facturacion.modelo.SolicitudFactura;
import com.perfumeriayvi.facturacion.validador.FacturaValidador;
import com.perfumeriayvi.facturacion.validador.ValidacionException;
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

/**
 * ENDPOINT REST del microservicio MS-02 Facturación.
 *
 * Es la "puerta de entrada": recibe la petición HTTP, la pasa al validador y, si es válida,
 * al microservicio que hace el cálculo. Devuelve siempre JSON.
 *
 *   POST http://localhost:8092/api/v1/facturas                               (cuerpo JSON)
 *   GET  http://localhost:8092/api/v1/documentos/validar?tipo=NIT&numero=890903938-8
 *   GET  http://localhost:8092/api/v1/info
 *   GET  http://localhost:8092/actuator/health                               (salud, para OPERATE)
 *
 * Respuestas: 200 con la factura · 400 con la lista de errores del validador.
 */
@RestController
@RequestMapping("/api/v1")
public class FacturaEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(FacturaEndpoint.class);

    private final FacturaValidador validador;
    private final FacturaMicroservicio microservicio;

    public FacturaEndpoint(FacturaValidador validador, FacturaMicroservicio microservicio) {
        this.validador = validador;
        this.microservicio = microservicio;
    }

    /** Calcula una factura (subtotal, descuento, IVA y total). */
    @PostMapping("/facturas")
    public RespuestaFactura calcular(@RequestBody SolicitudFactura solicitud) {
        validador.validar(solicitud);                                 // 1. validar
        RespuestaFactura factura = microservicio.calcular(solicitud);  // 2. calcular
        LOG.info("FACTURA cliente={} productos={} unidades={} total={}",
                factura.cliente(), factura.lineas().size(), factura.unidades(), factura.total());
        return factura;                                               // 3. responder
    }

    /** Valida solo un documento de cliente; útil para probar el validador desde el navegador. */
    @GetMapping("/documentos/validar")
    public Map<String, Object> validarDocumento(@RequestParam(required = false) String tipo,
                                                @RequestParam(required = false) String numero) {
        List<String> errores = validador.validarDocumento(tipo, numero);
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("tipo", tipo);
        cuerpo.put("numero", numero);
        cuerpo.put("valido", errores.isEmpty());
        cuerpo.put("errores", errores);
        return cuerpo;
    }

    /** Información del servicio. */
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("servicio", "ms-facturacion");
        info.put("version", "1.0.0");
        info.put("descripcion", "Calcula subtotal, descuento, IVA (19 %) y total de una venta (apoya RF07 y RF08).");
        info.put("endpoints", List.of(
                "POST /api/v1/facturas",
                "GET  /api/v1/documentos/validar?tipo=NIT&numero=890903938-8",
                "GET  /actuator/health"));
        return info;
    }

    // ------------------------------------------------------------------ errores -> HTTP 400

    @ExceptionHandler(ValidacionException.class)
    public ResponseEntity<Map<String, Object>> datosInvalidos(ValidacionException e) {
        LOG.warn("VALIDACION_FALLIDA errores={}", e.getErrores().size());
        return error("Los datos enviados no son válidos.", e.getErrores());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
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
