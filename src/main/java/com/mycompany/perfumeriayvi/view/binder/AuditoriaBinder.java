package com.mycompany.perfumeriayvi.view.binder;

import com.mycompany.perfumeriayvi.seguridad.Modulo;
import com.mycompany.perfumeriayvi.seguridad.RequiereAcceso;
import com.mycompany.perfumeriayvi.viewmodel.AuditoriaViewModel;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/**
 * Binder (capa View) del historial de auditoría. Vista: auditoria.jsp.
 *
 * Requisitos: RF11 (CU11, HU09) consultar; RF12 (CU12, HU09) exportar Excel/PDF;
 * RF03: solo ADMIN (módulo AUDITORIA).
 */
@Controller
@RequestMapping("/auditoria")
@RequiereAcceso(Modulo.AUDITORIA)
public class AuditoriaBinder {

    private final ObjectProvider<AuditoriaViewModel> viewModels;

    public AuditoriaBinder(ObjectProvider<AuditoriaViewModel> viewModels) {
        this.viewModels = viewModels;
    }

    /** RF11: consulta con filtros y paginación. */
    @GetMapping
    public String consultar(@RequestParam(required = false) String tabla,
                            @RequestParam(required = false) String usuario,
                            @RequestParam(required = false) String operacion,
                            @RequestParam(defaultValue = "1") int pagina,
                            Model model) {
        AuditoriaViewModel vm = viewModels.getObject();
        vm.consultar(tabla, usuario, operacion, pagina);
        model.addAttribute("vm", vm);
        return "auditoria";
    }

    /** RF12: exportar el resultado filtrado a Excel (formato=excel) o PDF (formato=pdf). */
    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar(@RequestParam(defaultValue = "excel") String formato,
                                           @RequestParam(required = false) String tabla,
                                           @RequestParam(required = false) String usuario,
                                           @RequestParam(required = false) String operacion) {
        AuditoriaViewModel vm = viewModels.getObject();
        vm.consultar(tabla, usuario, operacion, 1);

        boolean pdf = "pdf".equalsIgnoreCase(formato);
        byte[] archivo = pdf ? vm.exportarPdf() : vm.exportarExcel();
        String nombre = "auditoria_" + LocalDate.now() + (pdf ? ".pdf" : ".xlsx");
        MediaType tipo = pdf ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nombre).build().toString())
                .body(archivo);
    }
}
