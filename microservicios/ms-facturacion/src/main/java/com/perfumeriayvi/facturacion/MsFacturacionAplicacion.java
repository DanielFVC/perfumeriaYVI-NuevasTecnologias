package com.perfumeriayvi.facturacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de arranque del microservicio MS-02 Facturación.
 *
 * Programa independiente de la aplicación web: tiene su propio proceso, su propio
 * puerto (8092) y se despliega como software con "java -jar ms-facturacion.jar" (sin Docker).
 *
 * Capas del microservicio:
 *   endpoint/       -> FacturaEndpoint      (recibe las peticiones HTTP)
 *   validador/      -> FacturaValidador     (revisa cliente, documento y productos)
 *   microservicio/  -> FacturaMicroservicio (calcula subtotal, descuento, IVA y total)
 *   modelo/         -> datos de entrada y de salida
 */
@SpringBootApplication
public class MsFacturacionAplicacion {

    public static void main(String[] args) {
        SpringApplication.run(MsFacturacionAplicacion.class, args);
    }
}
