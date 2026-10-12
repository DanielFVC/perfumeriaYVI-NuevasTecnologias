package com.mycompany.perfumeriayvi.viewmodel;

import com.mycompany.perfumeriayvi.model.entity.Auditoria;
import com.mycompany.perfumeriayvi.model.repository.AuditoriaRepository.FiltroAuditoria;
import com.mycompany.perfumeriayvi.model.service.AuditoriaService;
import com.mycompany.perfumeriayvi.model.service.AuditoriaService.PaginaAuditoria;
import com.mycompany.perfumeriayvi.model.service.ExportadorAuditoria;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * ViewModel del historial de auditoría (auditoria.jsp). Solo lo usa el administrador.
 *
 * Requisitos: RF11 (CU11, HU09) filtros, paginación y gráfico; RF12 (CU12, HU09) exportación;
 * RNF-R2 (20 registros por página).
 */
@Component
@Scope("prototype")
public class AuditoriaViewModel extends ViewModelBase {

    /** RNF-R2 */
    public static final int REGISTROS_POR_PAGINA = 20;

    public static final List<String> TABLAS = List.of("venta", "detalle_venta", "cliente", "producto", "proveedor", "users");
    public static final List<String> OPERACIONES = List.of("INSERT", "UPDATE", "DELETE", "LOGIN", "LOGIN_FAIL", "BLOQUEO");

    private final AuditoriaService servicio;
    private final ExportadorAuditoria exportador;

    private FiltroAuditoria filtro = FiltroAuditoria.vacio();
    private List<Auditoria> registros = List.of();
    private Map<String, Integer> conteoPorOperacion = Map.of();
    private int paginaActual = 1;
    private int totalPaginas = 1;
    private int totalRegistros;

    public AuditoriaViewModel(AuditoriaService servicio, ExportadorAuditoria exportador) {
        this.servicio = servicio;
        this.exportador = exportador;
    }

    /** RF11: aplica filtros y página. Solo se aceptan valores de las listas conocidas. */
    public void consultar(String tabla, String usuario, String operacion, int pagina) {
        filtro = new FiltroAuditoria(
                permitido(TABLAS, tabla),
                usuario == null || usuario.isBlank() ? null : usuario.trim(),
                permitido(OPERACIONES, operacion));
        ejecutar(() -> {
            PaginaAuditoria resultado = servicio.consultar(filtro, pagina, REGISTROS_POR_PAGINA);
            registros = resultado.registros();
            paginaActual = resultado.pagina();
            totalPaginas = resultado.totalPaginas();
            totalRegistros = resultado.totalRegistros();
            conteoPorOperacion = resultado.conteoPorOperacion();
        }, null);
    }

    /** RF12: Excel con los registros del filtro actual. */
    public byte[] exportarExcel() {
        return exportador.aExcel(servicio.paraExportar(filtro));
    }

    /** RF12: PDF con los registros del filtro actual. */
    public byte[] exportarPdf() {
        return exportador.aPdf(servicio.paraExportar(filtro));
    }

    /** RF11: datos del gráfico en JSON, con las comillas escapadas. */
    public String getOperacionesJSON() {
        StringBuilder json = new StringBuilder("{");
        conteoPorOperacion.forEach((operacion, total) -> {
            if (json.length() > 1) {
                json.append(',');
            }
            json.append('"').append(escaparJson(operacion)).append("\":").append(total);
        });
        return json.append('}').toString();
    }

    /** RF11: conserva los filtros en los enlaces de paginación y exportación. */
    public String getConsultaFiltros() {
        StringBuilder q = new StringBuilder();
        agregar(q, "tabla", filtro.tabla());
        agregar(q, "usuario", filtro.usuario());
        agregar(q, "operacion", filtro.operacion());
        return q.toString();
    }

    /** Lista blanca: solo se aceptan valores conocidos (List.of no admite contains(null)). */
    private static String permitido(List<String> valores, String valor) {
        return valor != null && valores.contains(valor) ? valor : null;
    }

    private static void agregar(StringBuilder q, String nombre, String valor) {
        if (valor != null) {
            q.append('&').append(nombre).append('=').append(URLEncoder.encode(valor, StandardCharsets.UTF_8));
        }
    }

    private static String escaparJson(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder r = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> r.append("\\\"");
                case '\\' -> r.append("\\\\");
                case '<' -> r.append("\\u003c");
                case '>' -> r.append("\\u003e");
                default -> r.append(c < 0x20 ? ' ' : c);
            }
        }
        return r.toString();
    }

    public FiltroAuditoria getFiltro() { return filtro; }
    public List<Auditoria> getRegistros() { return registros; }
    public int getPaginaActual() { return paginaActual; }
    public int getTotalPaginas() { return totalPaginas; }
    public int getTotalRegistros() { return totalRegistros; }
    public List<String> getTablas() { return TABLAS; }
    public List<String> getOperaciones() { return OPERACIONES; }
}
