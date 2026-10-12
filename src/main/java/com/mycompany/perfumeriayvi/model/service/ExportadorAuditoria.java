package com.mycompany.perfumeriayvi.model.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.mycompany.perfumeriayvi.model.entity.Auditoria;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Genera los archivos de exportación de la auditoría.
 *
 * Requisito: RF12 (CU12, HU09) exportar el historial filtrado a Excel (.xlsx) y PDF.
 * (En la versión anterior estos métodos lanzaban UnsupportedOperationException.)
 */
@Component
public class ExportadorAuditoria {

    private static final String[] COLUMNAS = {"Fecha", "Tabla", "Operación", "ID registro", "Usuario", "Detalle"};
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** RF12: archivo Excel con una fila por registro. */
    public byte[] aExcel(List<Auditoria> registros) {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Auditoría");

            CellStyle encabezado = libro.createCellStyle();
            org.apache.poi.ss.usermodel.Font negrita = libro.createFont();
            negrita.setBold(true);
            encabezado.setFont(negrita);

            Row fila = hoja.createRow(0);
            for (int i = 0; i < COLUMNAS.length; i++) {
                fila.createCell(i).setCellValue(COLUMNAS[i]);
                fila.getCell(i).setCellStyle(encabezado);
            }

            int r = 1;
            for (Auditoria a : registros) {
                Row f = hoja.createRow(r++);
                f.createCell(0).setCellValue(fecha(a));
                f.createCell(1).setCellValue(texto(a.getTablaAfectada()));
                f.createCell(2).setCellValue(texto(a.getOperacion()));
                f.createCell(3).setCellValue(a.getIdRegistro());
                f.createCell(4).setCellValue(texto(a.getUsuario()));
                f.createCell(5).setCellValue(texto(a.getDetalle()));
            }
            // Anchos fijos (autoSizeColumn necesita fuentes del sistema que no hay en Docker)
            int[] anchos = {20, 15, 12, 12, 18, 100};
            for (int i = 0; i < anchos.length; i++) {
                hoja.setColumnWidth(i, anchos[i] * 256);
            }

            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new ReglaNegocioException("No fue posible generar el archivo Excel.");
        }
    }

    /** RF12: archivo PDF horizontal con la tabla de auditoría. */
    public byte[] aPdf(List<Auditoria> registros) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
        PdfWriter.getInstance(documento, salida);
        documento.open();

        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font cabecera = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 8);

        Paragraph encabezado = new Paragraph("Perfumería YVI - Historial de auditoría", titulo);
        encabezado.setAlignment(Element.ALIGN_CENTER);
        encabezado.setSpacingAfter(10);
        documento.add(encabezado);

        PdfPTable tabla = new PdfPTable(new float[]{2.2f, 1.4f, 1.2f, 1f, 1.6f, 6f});
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        for (String c : COLUMNAS) {
            PdfPCell celda = new PdfPCell(new Phrase(c, cabecera));
            celda.setBackgroundColor(new Color(33, 37, 41));
            tabla.addCell(celda);
        }
        for (Auditoria a : registros) {
            tabla.addCell(new Phrase(fecha(a), normal));
            tabla.addCell(new Phrase(texto(a.getTablaAfectada()), normal));
            tabla.addCell(new Phrase(texto(a.getOperacion()), normal));
            tabla.addCell(new Phrase(String.valueOf(a.getIdRegistro()), normal));
            tabla.addCell(new Phrase(texto(a.getUsuario()), normal));
            tabla.addCell(new Phrase(texto(a.getDetalle()), normal));
        }
        documento.add(tabla);
        documento.close();
        return salida.toByteArray();
    }

    private static String fecha(Auditoria a) {
        return a.getFecha() == null ? "" : a.getFecha().toLocalDateTime().format(FORMATO);
    }

    private static String texto(String s) {
        return s == null ? "" : s;
    }
}
