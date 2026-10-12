package com.mycompany.perfumeriayvi.model.service;

/**
 * Error de validación o de regla de negocio con un mensaje apto para mostrar al usuario.
 * Requisito: RNF-F2 (mensajes claros, sin páginas de error técnicas).
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
