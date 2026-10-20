package com.perfumeriayvi.inventario.validador;

import java.util.List;

/**
 * Error que lanza el validador cuando los datos no son correctos.
 * Lleva la lista completa de problemas para devolverlos todos de una vez (HTTP 400).
 */
public class ValidacionException extends RuntimeException {

    private final List<String> errores;

    public ValidacionException(List<String> errores) {
        super(String.join(" ", errores));
        this.errores = List.copyOf(errores);
    }

    public List<String> getErrores() {
        return errores;
    }
}
