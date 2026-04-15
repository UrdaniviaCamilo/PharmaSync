package com.londres.drogueria.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.londres.drogueria.model.Venta;
import com.londres.drogueria.model.DetalleVenta;
import com.londres.drogueria.model.MovimientoCaja;
import com.londres.drogueria.model.Caja;
import com.londres.drogueria.model.Producto;
import com.londres.drogueria.repository.ProductoRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ReporteService {
 
    @Autowired
    private ProductoRepository productoRepository;

    public ByteArrayInputStream generarPdfVenta(Venta venta) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("TICKET DE VENTA").setBold().setFontSize(18));
        document.add(new Paragraph("N° Venta: " + venta.getNumeroVenta()));
        document.add(new Paragraph("Fecha: " + venta.getFecha()));
        document.add(new Paragraph("Cliente: " + (venta.getCliente() != null ? venta.getCliente().getNombres() : "General")));
        document.add(new Paragraph("--------------------------------------------------"));

        float[] columnWidths = {40f, 15f, 20f, 25f};
        Table table = new Table(columnWidths);
        table.useAllAvailableWidth();
        table.addHeaderCell("Producto");
        table.addHeaderCell("Cant");
        table.addHeaderCell("P. Unit");
        table.addHeaderCell("Subtotal");

        for (DetalleVenta detalle : venta.getDetalles()) {
            table.addCell(detalle.getProducto().getNombre());
            table.addCell(detalle.getCantidad().toString());
            table.addCell("S/ " + detalle.getPrecioUnitario().toString());
            table.addCell("S/ " + detalle.getSubtotal().toString());
        }
        document.add(table);

        document.add(new Paragraph("--------------------------------------------------"));
        document.add(new Paragraph("TOTAL: S/ " + venta.getTotal()).setBold().setFontSize(14));
        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream generarExcelVentas(List<Venta> ventas) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Ventas");

        // Style for header
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);
        String[] columns = {"N° Venta", "Fecha", "Cliente", "Total", "Método Pago"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (Venta v : ventas) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(v.getNumeroVenta());
            row.createCell(1).setCellValue(v.getFecha() != null ? v.getFecha().toString() : "N/A");
            row.createCell(2).setCellValue(v.getCliente() != null ? v.getCliente().getNombres() : "General");
            row.createCell(3).setCellValue(v.getTotal() != null ? v.getTotal().doubleValue() : 0.0);
            row.createCell(4).setCellValue(v.getMetodoPago() != null ? v.getMetodoPago().getDisplayName() : "");
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream generarExcelCaja(Caja caja, List<MovimientoCaja> movimientos) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Movimientos Caja");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row infoRow = sheet.createRow(0);
        infoRow.createCell(0).setCellValue("REPORTE DE CAJA - ID: " + caja.getId());
        
        Row headerRow = sheet.createRow(2);
        String[] columns = {"Fecha", "Tipo", "Descripción", "Monto", "Usuario"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 3;
        for (MovimientoCaja m : movimientos) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(m.getFecha() != null ? m.getFecha().toString() : "");
            row.createCell(1).setCellValue(m.getTipo());
            row.createCell(2).setCellValue(m.getDescripcion());
            row.createCell(3).setCellValue(m.getMonto().doubleValue());
            row.createCell(4).setCellValue(m.getUsuario() != null ? m.getUsuario().getUsername() : "");
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream generarPdfCaja(Caja caja, List<MovimientoCaja> movimientos) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("REPORTE DE MOVIMIENTOS DE CAJA").setBold().setFontSize(16));
        document.add(new Paragraph("Caja ID: " + caja.getId()));
        document.add(new Paragraph("Apertura: " + (caja.getFechaApertura() != null ? caja.getFechaApertura().toString() : "")));
        document.add(new Paragraph("Monto Inicial: S/ " + caja.getMontoInicial()));
        document.add(new Paragraph("\n"));

        float[] columnWidths = {25f, 15f, 35f, 25f};
        Table table = new Table(columnWidths);
        table.useAllAvailableWidth();
        table.addHeaderCell("Fecha");
        table.addHeaderCell("Tipo");
        table.addHeaderCell("Descripción");
        table.addHeaderCell("Monto");

        for (MovimientoCaja m : movimientos) {
            table.addCell(m.getFecha() != null ? m.getFecha().toString().substring(11, 19) : "");
            table.addCell(m.getTipo());
            table.addCell(m.getDescripcion());
            table.addCell("S/ " + m.getMonto());
        }
        document.add(table);
        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    }
    public ByteArrayInputStream generarPdfVentas(List<Venta> ventas) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("REPORTE GLOBAL DE VENTAS").setBold().setFontSize(18));
        document.add(new Paragraph("Total de Registros: " + ventas.size()));
        document.add(new Paragraph("\n"));

        float[] columnWidths = {20f, 20f, 30f, 15f, 15f};
        Table table = new Table(columnWidths);
        table.useAllAvailableWidth();
        table.addHeaderCell("N° Venta");
        table.addHeaderCell("Fecha");
        table.addHeaderCell("Cliente");
        table.addHeaderCell("Total");
        table.addHeaderCell("Pago");

        for (Venta v : ventas) {
            table.addCell(v.getNumeroVenta());
            table.addCell(v.getFecha() != null ? v.getFecha().toString().substring(0, 10) : "");
            table.addCell(v.getCliente() != null ? v.getCliente().getNombres() : "General");
            table.addCell("S/ " + v.getTotal());
            table.addCell(v.getMetodoPago() != null ? v.getMetodoPago().name() : "");
        }
        document.add(table);
        document.close();
        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream generarExcelInventario() throws Exception {
        List<Producto> productos = productoRepository.findAll();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Inventario");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);
        String[] columns = {"ID", "Código", "Producto", "Categoría", "Stock", "P. Venta", "Estado"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (Producto p : productos) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(p.getId());
            row.createCell(1).setCellValue(p.getCodigoEan13());
            row.createCell(2).setCellValue(p.getNombre());
            row.createCell(3).setCellValue(p.getCategoria() != null ? p.getCategoria().getNombre() : "");
            row.createCell(4).setCellValue(p.getStock() != null ? p.getStock().doubleValue() : 0.0);
            row.createCell(5).setCellValue(p.getPrecioVenta() != null ? p.getPrecioVenta().doubleValue() : 0.0);
            row.createCell(6).setCellValue(p.isActivo() ? "Activo" : "Inactivo");
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return new ByteArrayInputStream(out.toByteArray());
    }
}
