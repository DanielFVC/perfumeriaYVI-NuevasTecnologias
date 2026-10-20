package com.perfumeriayvi.inventario.modelo;

/**
 * Datos que recibe el microservicio para calcular el reabastecimiento de un producto.
 *
 * Los campos numéricos son de tipo objeto (Integer, Double) para poder detectar
 * cuando no se envían y avisarlo en el validador.
 *
 * @param idProducto            identificador del producto en Perfumería YVI (tabla producto)
 * @param nombreProducto        nombre del producto (opcional, solo para el mensaje)
 * @param stockActual           unidades disponibles hoy
 * @param stockMinimo           unidades mínimas de seguridad (columna stock_minimo)
 * @param ventasDiarias         promedio de unidades vendidas por día
 * @param diasEntregaProveedor  días que tarda el proveedor en entregar un pedido
 * @param diasPlanificacion     días de venta que debe cubrir el pedido (por defecto 30)
 */
public record SolicitudReabastecimiento(
        Integer idProducto,
        String nombreProducto,
        Integer stockActual,
        Integer stockMinimo,
        Double ventasDiarias,
        Integer diasEntregaProveedor,
        Integer diasPlanificacion) {

    public static final int DIAS_PLANIFICACION_POR_DEFECTO = 30;

    /** Devuelve los días de planificación o el valor por defecto si no se enviaron. */
    public int diasPlanificacionEfectivos() {
        return diasPlanificacion == null ? DIAS_PLANIFICACION_POR_DEFECTO : diasPlanificacion;
    }
}
