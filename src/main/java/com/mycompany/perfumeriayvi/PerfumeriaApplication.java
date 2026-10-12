package com.mycompany.perfumeriayvi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Punto de entrada de Perfumería YVI.
 *
 * Arquitectura MVVM (RNF-M1):
 * <ul>
 *   <li><b>Model</b> ({@code model.entity}, {@code model.repository}, {@code model.service}):
 *       datos, acceso a MySQL y reglas de negocio.</li>
 *   <li><b>ViewModel</b> ({@code viewmodel}): estado y comandos de cada pantalla.
 *       No conoce HTTP ni JSP, por eso se prueba con pruebas unitarias.</li>
 *   <li><b>View</b> ({@code view.binder} + JSP en WEB-INF/views): las JSP se enlazan a las
 *       propiedades del ViewModel con expresiones ${vm.propiedad}; los binders solo conectan
 *       la petición HTTP con el comando del ViewModel.</li>
 * </ul>
 *
 * Se puede desplegar como WAR en Tomcat (NetBeans) o ejecutar con "java -jar perfumeriaYVI.war".
 */
@SpringBootApplication
public class PerfumeriaApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(PerfumeriaApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(PerfumeriaApplication.class, args);
    }
}
