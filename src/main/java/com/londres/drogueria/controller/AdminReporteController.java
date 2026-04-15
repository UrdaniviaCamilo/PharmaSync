package com.londres.drogueria.controller;

import com.londres.drogueria.model.Venta;
import com.londres.drogueria.service.ReporteService;
import com.londres.drogueria.service.AdminDashboardService;
import com.londres.drogueria.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import java.io.ByteArrayInputStream;
import java.util.List;

@Controller
@RequestMapping("/admin/reportes")
public class AdminReporteController {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private AdminDashboardService dashboardService;

    @GetMapping
    public String index(Model model) {
        Long empresaId = com.londres.drogueria.config.TenantContext.getCurrentTenant();
        java.util.Map<String, Object> stats = dashboardService.getDashboardStats(empresaId);
        
        model.addAllAttributes(stats);
        

        model.addAttribute("totalIngresos", stats.get("ventasMes") != null ? stats.get("ventasMes") : java.math.BigDecimal.ZERO);
        model.addAttribute("totalVentas", stats.get("cantidadVentasMes"));


        @SuppressWarnings("unchecked")
        List<List<Object>> ventasPorDia = (List<List<Object>>) stats.get("ventasPorDia");
        List<String> labels = new java.util.ArrayList<>();
        List<java.math.BigDecimal> data = new java.util.ArrayList<>();
        
        if (ventasPorDia != null) {
            for (List<Object> row : ventasPorDia) {
                labels.add(row.get(0) != null ? row.get(0).toString() : "N/A");
                Object val = row.get(2);
                data.add(val != null ? new java.math.BigDecimal(val.toString()) : java.math.BigDecimal.ZERO);
            }
        }
        

        model.addAttribute("chartLabels", labels);
        model.addAttribute("chartData", data);


        @SuppressWarnings("unchecked")
        List<List<Object>> catDataRaw = (List<List<Object>>) stats.get("productosPorCategoria");
        List<String> catLabels = new java.util.ArrayList<>();
        List<Long> catCounts = new java.util.ArrayList<>();
        if (catDataRaw != null) {
            for (List<Object> row : catDataRaw) {
                catLabels.add(row.get(0).toString());
                catCounts.add(Long.parseLong(row.get(1).toString()));
            }
        }
        model.addAttribute("catLabels", catLabels);
        model.addAttribute("catCounts", catCounts);

        // Top Productos
        model.addAttribute("topProductos", stats.get("topProductos"));

        // Ventas por Metodo de Pago
        model.addAttribute("ventasPorMetodo", stats.get("ventasPorMetodoPago"));


        List<Object[]> hourlyData = ventaRepository.findSalesByHour(empresaId);
        List<String> hourLabels = new java.util.ArrayList<>();
        List<Long> hourCounts = new java.util.ArrayList<>();
        for (Object[] row : hourlyData) {
            if (row[0] != null) {

                int h = (int) Double.parseDouble(row[0].toString());
                hourLabels.add(String.format("%02d:00", h));
                hourCounts.add(row[1] != null ? Long.parseLong(row[1].toString()) : 0L);
            }
        }
        model.addAttribute("hourLabels", hourLabels);
        model.addAttribute("hourCounts", hourCounts);


        List<Object[]> revenueCatData = ventaRepository.findRevenueByCategory(empresaId);
        List<String> revCatLabels = new java.util.ArrayList<>();
        List<java.math.BigDecimal> revCatTotals = new java.util.ArrayList<>();
        for (Object[] row : revenueCatData) {
            if (row[0] != null) {
                revCatLabels.add(row[0].toString());
                revCatTotals.add(row[1] != null ? new java.math.BigDecimal(row[1].toString()) : java.math.BigDecimal.ZERO);
            }
        }
        model.addAttribute("revCatLabels", revCatLabels);
        model.addAttribute("revCatTotals", revCatTotals);
        
        return "admin/reportes/index";
    }

    @GetMapping("/ticket/{id}")
    public ResponseEntity<InputStreamResource> ticketVenta(@PathVariable Long id) {
        Venta venta = ventaRepository.findById(id).orElseThrow();
        ByteArrayInputStream bis = reporteService.generarPdfVenta(venta);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=ticket_" + venta.getNumeroVenta() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/ventas/excel")
    public ResponseEntity<InputStreamResource> excelVentas() throws Exception {
        List<Venta> ventas = ventaRepository.findAll();
        ByteArrayInputStream bis = reporteService.generarExcelVentas(ventas);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=reporte_ventas.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/ventas/pdf")
    public ResponseEntity<InputStreamResource> pdfVentas() throws Exception {
        List<Venta> ventas = ventaRepository.findAll();
        ByteArrayInputStream bis = reporteService.generarPdfVentas(ventas);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=reporte_ventas.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/inventario/excel")
    public ResponseEntity<InputStreamResource> excelInventario() throws Exception {
        ByteArrayInputStream bis = reporteService.generarExcelInventario();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=reporte_inventario.xlsx");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).body(new InputStreamResource(bis));
    }
}
