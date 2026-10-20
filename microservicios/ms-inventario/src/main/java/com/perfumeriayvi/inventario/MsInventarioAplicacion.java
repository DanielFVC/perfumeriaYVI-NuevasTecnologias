package com.perfumeriayvi.inventario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de arranque del microservicio MS-01 Inventario (reabastecimiento).
 *
 * Es un programa independiente de la aplicación web: tiene su propio proceso, su propio
 * puerto (8091) y se despliega como software con "java -jar ms-inventario.jar" (sin Docker).
 *
 * Capas del microservicio:
 *   endpoint/       -> ReabastecimientoEndpoint      (recibe las peticiones HTTP)
 *   validador/      -> ReabastecimientoValidador     (revisa que los datos sean correctos)
 *   microservicio/  -> ReabastecimientoMicroservicio (hace el cálculo de negocio)
 *   modelo/         -> datos de entrada y de salida
 */
@SpringBootApplication
public class MsInventarioAplicacion {

    public static void main(String[] args) {
        SpringApplication.run(MsInventarioAplicacion.class, args);
    }
}
