package com.mycompany.perfumeriayvi.seguridad;

/**
 * Módulos del sistema sobre los que se aplica la autorización.
 *
 * Requisito: RF03 (CU03, HU02). El ADMIN accede a todos; el VENDEDOR solo a
 * CLIENTES, PROVEEDORES, PRODUCTOS y VENTAS si tiene el permiso habilitado (RF10).
 * USUARIOS y AUDITORIA son exclusivos del ADMIN.
 */
public enum Modulo {
    CLIENTES,
    PROVEEDORES,
    PRODUCTOS,
    VENTAS,
    USUARIOS,
    AUDITORIA
}
