package com.mycompany.perfumeriayvi.seguridad;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca las acciones que no requieren sesión iniciada (pantalla de login).
 * Todo lo demás exige autenticación.
 *
 * Requisito: RF01 (CU01) / RF03 (CU03).
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AccesoPublico {
}
