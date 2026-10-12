package com.mycompany.perfumeriayvi.config;

import com.mycompany.perfumeriayvi.seguridad.AutorizacionInterceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración web de Spring MVC.
 *
 * Registra el interceptor de autorización para todas las rutas (RF01, RF03),
 * excepto imágenes, la página de error y los endpoints de monitoreo.
 *
 * El prefijo y sufijo de las vistas JSP (capa View) están en application.properties.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AutorizacionInterceptor autorizacion;

    public WebConfig(AutorizacionInterceptor autorizacion) {
        this.autorizacion = autorizacion;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(autorizacion)
                .addPathPatterns("/**")
                .excludePathPatterns("/imagenes/**", "/error", "/actuator/**");
    }
}
