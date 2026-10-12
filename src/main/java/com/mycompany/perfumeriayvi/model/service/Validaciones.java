package com.mycompany.perfumeriayvi.model.service;

import java.util.regex.Pattern;

/**
 * Reglas de validación compartidas por los servicios.
 * Requisito: RNF-F2 (validación en el servidor, no solo en el navegador).
 */
final class Validaciones {

    static final Pattern TELEFONO = Pattern.compile("\\d{7,15}");
    static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    static final Pattern SOLO_DIGITOS = Pattern.compile("\\d+");

    private Validaciones() {
    }

    static boolean vacio(String s) {
        return s == null || s.isBlank();
    }

    static String limpiar(String s) {
        return s == null ? "" : s.trim();
    }

    static void exigir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new ReglaNegocioException(mensaje);
        }
    }
}
