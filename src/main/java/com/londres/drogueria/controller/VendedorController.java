package com.londres.drogueria.controller;

import com.londres.drogueria.config.CustomUserDetails;
import com.londres.drogueria.service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/vendedor")
public class VendedorController {

    @Autowired
    private AdminDashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null)
            return "redirect:/login";

        Map<String, Object> stats = dashboardService
                .getVendedorStats(userDetails.getEmpresaId(), userDetails.getUsuario());

        model.addAllAttributes(stats);

        return "vendedor/dashboard";
    }


    @GetMapping("/buscar-medicamento")
    public String buscarMedicamento() {
        return "vendedor/buscar";
    }


    @GetMapping("/stock")
    public String verStock() {
        return "vendedor/stock";
    }
}