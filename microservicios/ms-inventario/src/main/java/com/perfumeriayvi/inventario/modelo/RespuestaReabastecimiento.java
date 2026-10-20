package com.perfumeriayvi.inventario.modelo;

/**
 * Resultado del cálculo de reabastecimiento que devuelve el microservicio en JSON.
 *
 * @param idProducto        producto evaluado
 * @param nombreProducto    nombre del producto (si se envió)
 * @param nivel             AGOTADO, CRITICO, REORDEN o NORMAL
 * @param necesitaPedido    true si hay que hacer un pedido al proveedor
 * @param puntoReorden      unidades a partir de las cuales se debe pedir
 * @param cantidadSugerida  unidades que conviene pedir ahora (0 si no hace falta)
 * @param diasCobertura     días que alcanza el stock actual (null si no hay ventas)
 * @param mensaje           explicación en lenguaje natural
 */
public record RespuestaReabastecimiento(
        Integer idProducto,
        String nombreProducto,
        Nivel nivel,
        boolean necesitaPedido,
        int puntoReorden,
        int cantidadSugerida,
        Double diasCobertura,
        String mensaje) {

    /** Nivel de urgencia del producto. */
    public enum Nivel {
        AGOTADO, CRITICO, REORDEN, NORMAL
    }
}
