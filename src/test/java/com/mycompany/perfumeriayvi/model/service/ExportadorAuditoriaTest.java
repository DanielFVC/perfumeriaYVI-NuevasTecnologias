package com.mycompany.perfumeriayvi.model.service;

import com.mycompany.perfumeriayvi.model.entity.Auditoria;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas de RF12 (CU12): generación de archivos Excel y PDF. */
class ExportadorAuditoriaTest {

    private final ExportadorAuditoria exportador = new ExportadorAuditoria();
    private final List<Auditoria> registros = List.of(new Auditoria(1, Timestamp.valueOf("2026-10-07 10:00:00"),
            "cliente", "INSERT", 5, "admin", "Cliente registrado -> Nombre: Laura Gómez"));

    @Test
    void generaUnArchivoExcelValido() {
        byte[] excel = exportador.aExcel(registros);
        assertTrue(excel.length > 0);
        assertEquals("PK", new String(excel, 0, 2, StandardCharsets.US_ASCII)); // los .xlsx son ZIP
    }

    @Test
    void generaUnArchivoPdfValido() {
        byte[] pdf = exportador.aPdf(registros);
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }
}
