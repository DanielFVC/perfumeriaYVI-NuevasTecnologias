package com.mycompany.perfumeriayvi.model.repository;

import com.mycompany.perfumeriayvi.model.entity.Auditoria;

import java.util.List;
import java.util.Map;

/**
 * Consulta del historial de auditoría (tabla auditoria_ventas).
 * Requisitos: RF11 (CU11, HU09) consultar auditoría; RF12 (CU12, HU09) exportar.
 */
public interface AuditoriaRepository {

    List<Auditoria> buscar(FiltroAuditoria filtro, int limite, int desplazamiento);

    int contar(FiltroAuditoria filtro);

    Map<String, Integer> contarPorOperacion(FiltroAuditoria filtro);

    /** Filtros opcionales de la consulta (null o vacío = sin filtro). */
    record FiltroAuditoria(String tabla, String usuario, String operacion) {

        public static FiltroAuditoria vacio() {
            return new FiltroAuditoria(null, null, null);
        }
    }
}
