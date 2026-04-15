package com.londres.drogueria.controller;

import com.londres.drogueria.model.Empresa;
import com.londres.drogueria.repository.EmpresaRepository;
import com.londres.drogueria.repository.UsuarioRepository;
import com.londres.drogueria.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/super-admin")
public class SuperAdminController {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalEmpresas", empresaRepository.count());
        model.addAttribute("empresasActivas", empresaRepository.countActiveCompanies());
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        model.addAttribute("totalVentasPlataforma", ventaRepository.count());
        
        // Ingresos totales de TODA la plataforma
        java.math.BigDecimal globalRevenue = ventaRepository.findAll().stream()
                .map(com.londres.drogueria.model.Venta::getTotal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        model.addAttribute("totalIngresosPlataforma", globalRevenue);
        
        // Solicitudes pendientes de Super Admin
        java.util.List<com.londres.drogueria.model.Usuario> solicitudesSuper = usuarioRepository.findAll().stream()
                .filter(u -> u.getRoles().contains(com.londres.drogueria.model.Rol.SUPER_ADMIN) && !u.isActivo())
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("solicitudesSuper", solicitudesSuper);
        
        // Datos para nuevos gráficos (Últimos 30 días)
        java.time.LocalDateTime thirtyDaysAgo = java.time.LocalDateTime.now().minusDays(30);
        model.addAttribute("globalTrend", ventaRepository.findGlobalSalesTrend(thirtyDaysAgo));
        model.addAttribute("businessRanking", ventaRepository.findRevenueByBusinessRanking());
        
        model.addAttribute("empresas", empresaRepository.findAll());
        model.addAttribute("statsDepto", empresaRepository.countCompaniesByDepartment());
        
        model.addAttribute("paginaActiva", "dashboard_global");
        return "super-admin/dashboard";
    }

    @GetMapping("/negocios")
    public String listaNegocios(Model model) {
        model.addAttribute("empresas", empresaRepository.findAll());
        model.addAttribute("paginaActiva", "negocios");
        return "super-admin/negocios";
    }

    @GetMapping("/usuarios")
    public String listaUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("paginaActiva", "usuarios_global");
        return "super-admin/usuarios";
    }

    @PostMapping("/aprobar/{id}")
    public String aprobarSuperAdmin(@PathVariable Long id, RedirectAttributes ra) {
        com.londres.drogueria.model.Usuario u = usuarioRepository.findById(id).orElseThrow();
        u.setActivo(true);
        usuarioRepository.save(u);
        ra.addFlashAttribute("success", "Cuenta de Super Admin aprobada correctamente.");
        return "redirect:/super-admin/dashboard";
    }

    @PostMapping("/rechazar/{id}")
    public String rechazarSuperAdmin(@PathVariable Long id, RedirectAttributes ra) {
        usuarioRepository.deleteById(id);
        ra.addFlashAttribute("success", "Solicitud de Super Admin rechazada y eliminada.");
        return "redirect:/super-admin/dashboard";
    }

    @PostMapping("/empresas/toggle/{id}")
    public String toggleEmpresa(@PathVariable Long id, RedirectAttributes ra) {
        Empresa e = empresaRepository.findById(id).orElseThrow();
        e.setActivo(!e.isActivo());
        empresaRepository.save(e);
        ra.addFlashAttribute("success", "Estado de la empresa actualizado.");
        return "redirect:/super-admin/dashboard";
    }

    @GetMapping("/reportes")
    public String reportesGlobales(Model model) {
        model.addAttribute("totalNegocios", empresaRepository.count());
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        model.addAttribute("totalVentas", ventaRepository.count());
        
        java.math.BigDecimal globalRevenue = ventaRepository.findAll().stream()
                .map(com.londres.drogueria.model.Venta::getTotal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        model.addAttribute("totalRevenue", globalRevenue);
        
        model.addAttribute("businessRanking", ventaRepository.findRevenueByBusinessRanking());
        
        java.time.LocalDateTime ninetyDaysAgo = java.time.LocalDateTime.now().minusDays(90);
        model.addAttribute("globalTrend", ventaRepository.findGlobalSalesTrend(ninetyDaysAgo));
        
        model.addAttribute("paginaActiva", "reportes_global");
        return "super-admin/reportes";
    }

    @GetMapping("/auditoria")
    public String auditoria(Model model) {
        model.addAttribute("logs", new java.util.ArrayList<>()); // Placeholder for logs
        model.addAttribute("paginaActiva", "auditoria");
        return "super-admin/auditoria";
    }

    @GetMapping("/mantenimiento")
    public String mantenimiento(Model model) {
        model.addAttribute("paginaActiva", "mantenimiento");
        return "super-admin/mantenimiento";
    }
}
