package com.mycompany.perfumeriayvi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Reloj del sistema en la zona horaria de Colombia.
 * Se inyecta donde se calculan fechas (bloqueo temporal RNF-S2, último acceso RF01),
 * así las pruebas pueden usar un reloj fijo.
 */
@Configuration
public class RelojConfig {

    @Bean
    public Clock reloj() {
        return Clock.system(ZoneId.of("America/Bogota"));
    }
}
