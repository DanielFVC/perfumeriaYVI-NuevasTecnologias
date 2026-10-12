package com.mycompany.perfumeriayvi.model.service;

import com.mycompany.perfumeriayvi.model.entity.Auditoria;
import com.mycompany.perfumeriayvi.model.repository.AuditoriaRepository;
import com.mycompany.perfumeriayvi.model.repository.AuditoriaRepository.FiltroAuditoria;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Consulta del historial de auditoría.
 *
 * Requisitos: RF11 (CU11, HU09) consultar con filtros, paginación y resumen por operación;
 * RF12 (CU12, HU09) obtener los registros que se exportan; RNF-R2 paginación.
 */
@Service
public class AuditoriaService {

    /** RF12: límite de filas por exportación para no sobrecargar el servidor (RNF-R1). */
    public static final int MAXIMO_EXPORTACION = 5000;

    private final AuditoriaRepository repositorio;

    public AuditoriaService(AuditoriaRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** RF11 + RNF-R2: una página de resultados. */
    public PaginaAuditoria consultar(FiltroAuditoria filtro, int pagina, int tamano) {
        int total = repositorio.contar(filtro);
        int totalPaginas = Math.max(1, (int) Math.ceil((double) total / tamano));
        int paginaValida = Math.min(Math.max(1, pagina), totalPaginas);

        List<Auditoria> registros = repositorio.buscar(filtro, tamano, (paginaValida - 1) * tamano);
        Map<String, Integer> conteo = repositorio.contarPorOperacion(filtro);
        return new PaginaAuditoria(registros, paginaValida, totalPaginas, total, conteo);
    }

    /** RF12: registros filtrados para exportar a Excel o PDF. */
    public List<Auditoria> paraExportar(FiltroAuditoria filtro) {
        return repositorio.buscar(filtro, MAXIMO_EXPORTACION, 0);
    }

    /** Resultado paginado de la consulta (RF11). */
    public record PaginaAuditoria(List<Auditoria> registros, int pagina, int totalPaginas,
                                  int totalRegistros, Map<String, Integer> conteoPorOperacion) {
    }
}
