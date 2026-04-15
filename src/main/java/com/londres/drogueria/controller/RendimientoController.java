package com.londres.drogueria.controller;

import com.londres.drogueria.model.Usuario;
import com.londres.drogueria.repository.MovimientoStockRepository;
import com.londres.drogueria.repository.VentaRepository;
import com.londres.drogueria.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/rendimiento")
public class RendimientoController {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private MovimientoStockRepository movimientoStockRepository;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String verRendimiento(Model model, Authentication auth, 
                                 @RequestParam(defaultValue = "30") int dias) {
        com.londres.drogueria.config.CustomUserDetails userDetails = (com.londres.drogueria.config.CustomUserDetails) auth.getPrincipal();
        Usuario usuario = userDetails.getUsuario();
        LocalDateTime start = LocalDateTime.now().minusDays(dias);
        Long empresaId = usuario.getEmpresaId();

        model.addAttribute("paginaActiva", "rendimiento");
        model.addAttribute("dias", dias);

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {

            List<Object[]> salesPerf = ventaRepository.findSalesPerformanceByEmployee(empresaId, start);
            List<Object[]> stockPerf = movimientoStockRepository.findStockMovementPerformanceByEmployee(empresaId, start);
            

            List<Usuario> usuarios = usuarioService.listarTodos();
            List<java.util.Map<String, Object>> combinedPerf = new java.util.ArrayList<>();
            
            for (Usuario u : usuarios) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                

                long vCount = ventaRepository.countByUsuarioAndFechaAfter(u.getId(), start);
                java.math.BigDecimal vTotal = ventaRepository.sumTotalByUsuarioAndFechaAfter(u.getId(), start);
                long mCount = movimientoStockRepository.findPersonalStockMovementsByDayAfter(u.getId(), start).stream()
                        .mapToLong(row -> ((Number) row[1]).longValue()).sum();
                
                map.put("nombre", u.getNombres() + " " + u.getApellidos());
                map.put("rol", u.getRoles().toString().replace("[", "").replace("]", ""));
                map.put("ventas", vCount);
                map.put("total", vTotal != null ? vTotal : java.math.BigDecimal.ZERO);
                map.put("movimientos", mCount);
                combinedPerf.add(map);
            }

            model.addAttribute("salesPerf", salesPerf);
            model.addAttribute("stockPerf", stockPerf);
            model.addAttribute("combinedPerf", combinedPerf);
            return "admin/rendimiento";
        } else {
            // Data for Vendor/Warehouse (Personal performance)
            List<Object[]> personalSales = ventaRepository.findPersonalSalesByDayAfter(usuario.getId(), start);
            List<Object[]> personalMoves = movimientoStockRepository.findPersonalStockMovementsByDayAfter(usuario.getId(), start);
            
            model.addAttribute("personalSales", personalSales);
            model.addAttribute("personalMoves", personalMoves);
            model.addAttribute("usuario", usuario);
            return "vendedor/rendimiento";
        }
    }
}
