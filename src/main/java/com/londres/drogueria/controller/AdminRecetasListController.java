package com.londres.drogueria.controller;

import com.londres.drogueria.config.TenantContext;
import com.londres.drogueria.model.Venta;
import com.londres.drogueria.model.VentaEstado;
import com.londres.drogueria.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/recetas")
public class AdminRecetasListController {

    @Autowired
    private VentaRepository ventaRepository;

    @GetMapping
    public String listarPendientes(Model model) {
        Long empresaId = TenantContext.getCurrentTenant();
        List<Venta> pendientes = ventaRepository.findAll().stream()
                .filter(v -> v.getEmpresaId().equals(empresaId) && v.getEstado() == VentaEstado.VALIDACION_PENDIENTE)
                .collect(Collectors.toList());

        List<Venta> historial = ventaRepository.findAll().stream()
                .filter(v -> v.getEmpresaId().equals(empresaId)
                        && (v.getEstado() == VentaEstado.APROBADA || v.getEstado() == VentaEstado.RECHAZADA))
                .collect(Collectors.toList());

        model.addAttribute("pendientes", pendientes);
        model.addAttribute("historial", historial);
        model.addAttribute("paginaActiva", "recetas");
        return "admin/ventas/recetas";
    }
}
