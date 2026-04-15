package com.londres.drogueria.controller;

import com.londres.drogueria.config.CustomUserDetails;
import com.londres.drogueria.model.Caja;
import com.londres.drogueria.service.CajaService;
import com.londres.drogueria.repository.MovimientoCajaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.londres.drogueria.model.MovimientoCaja;

@Controller
@RequestMapping("/admin/caja")
public class CajaController {

    @Autowired
    private CajaService cajaService;

    @Autowired
    private MovimientoCajaRepository movimientoCajaRepository;

    @GetMapping
    public String index(Model model, 
                       @AuthenticationPrincipal CustomUserDetails userDetails,
                       @RequestParam(required = false) String start,
                       @RequestParam(required = false) String end) {
        
        Long empresaId = userDetails.getEmpresaId();
        Caja cajaAbierta = cajaService.obtenerCajaAbierta(empresaId).orElse(null);
        model.addAttribute("caja", cajaAbierta);

        List<MovimientoCaja> movimientos;
        if (start != null && end != null) {
            LocalDateTime startDate = LocalDateTime.parse(start + "T00:00:00");
            LocalDateTime endDate = LocalDateTime.parse(end + "T23:59:59");
            movimientos = movimientoCajaRepository.findByEmpresaIdAndFechaBetweenOrderByFechaDesc(empresaId, startDate, endDate);
            model.addAttribute("filtrado", true);
        } else if (cajaAbierta != null) {
            movimientos = movimientoCajaRepository.findByCajaIdOrderByFechaDesc(cajaAbierta.getId());
        } else {

            LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
            movimientos = movimientoCajaRepository.findByEmpresaIdAndFechaBetweenOrderByFechaDesc(empresaId, monthAgo, LocalDateTime.now());
        }
        
        model.addAttribute("movimientos", movimientos);
        return "admin/caja/index";
    }

    @PostMapping("/apertura")
    public String aperturar(@RequestParam BigDecimal montoInicial, 
                           @AuthenticationPrincipal CustomUserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        try {
            cajaService.abrirCaja(montoInicial, userDetails.getUsuario());
            redirectAttributes.addFlashAttribute("success", "Caja aperturada con éxito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/caja";
    }

    @PostMapping("/cierre")
    public String cerrar(RedirectAttributes redirectAttributes, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            cajaService.cerrarCaja(userDetails.getEmpresaId());
            redirectAttributes.addFlashAttribute("success", "Caja cerrada con éxito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/caja";
    }

    @PostMapping("/movimiento")
    public String registrarMovimiento(@RequestParam String tipo, 
                                     @RequestParam String descripcion, 
                                     @RequestParam BigDecimal monto,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        try {
            Caja caja = cajaService.obtenerCajaAbierta(userDetails.getEmpresaId())
                    .orElseThrow(() -> new RuntimeException("No hay caja abierta"));
            cajaService.registrarMovimiento(caja, tipo, descripcion, monto, userDetails.getUsuario());
            redirectAttributes.addFlashAttribute("success", "Movimiento registrado con éxito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/caja";
    }

    @Autowired
    private com.londres.drogueria.service.ReporteService reporteService;

    @GetMapping("/exportar/excel")
    public org.springframework.web.servlet.ModelAndView exportarExcel(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Caja caja = cajaService.obtenerCajaAbierta(userDetails.getEmpresaId()).orElse(null);
            if (caja == null) return new org.springframework.web.servlet.ModelAndView("redirect:/admin/caja");
            
            java.util.List<com.londres.drogueria.model.MovimientoCaja> movimientos = movimientoCajaRepository.findByCajaIdOrderByFechaDesc(caja.getId());
            java.io.ByteArrayInputStream in = reporteService.generarExcelCaja(caja, movimientos);
            
            return new org.springframework.web.servlet.ModelAndView(new org.springframework.web.servlet.View() {
                @Override
                public void render(java.util.Map<String, ?> model, jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) throws Exception {
                    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                    response.setHeader("Content-Disposition", "attachment; filename=caja_movimientos.xlsx");
                    in.transferTo(response.getOutputStream());
                }
            });
        } catch (Exception e) {
            return new org.springframework.web.servlet.ModelAndView("redirect:/admin/caja");
        }
    }

    @GetMapping("/exportar/pdf")
    public org.springframework.web.servlet.ModelAndView exportarPdf(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Caja caja = cajaService.obtenerCajaAbierta(userDetails.getEmpresaId()).orElse(null);
            if (caja == null) return new org.springframework.web.servlet.ModelAndView("redirect:/admin/caja");
            
            java.util.List<com.londres.drogueria.model.MovimientoCaja> movimientos = movimientoCajaRepository.findByCajaIdOrderByFechaDesc(caja.getId());
            java.io.ByteArrayInputStream in = reporteService.generarPdfCaja(caja, movimientos);
            
            return new org.springframework.web.servlet.ModelAndView(new org.springframework.web.servlet.View() {
                @Override
                public void render(java.util.Map<String, ?> model, jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) throws Exception {
                    response.setContentType("application/pdf");
                    response.setHeader("Content-Disposition", "attachment; filename=caja_movimientos.pdf");
                    in.transferTo(response.getOutputStream());
                }
            });
        } catch (Exception e) {
            return new org.springframework.web.servlet.ModelAndView("redirect:/admin/caja");
        }
    }
}
