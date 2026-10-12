package com.mycompany.perfumeriayvi.seguridad;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un binder (o un método) con el módulo que protege.
 * El {@link AutorizacionInterceptor} lo verifica antes de ejecutar la acción.
 *
 * Requisito: RF03 (CU03).
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiereAcceso {

    Modulo value();
}
