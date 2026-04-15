package com.londres.drogueria.controller;

import com.londres.drogueria.config.CustomUserDetails;
import com.londres.drogueria.service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/almacen")
public class AlmacenController {

    @Autowired
    private AdminDashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> stats = dashboardService.getAlmacenStats(userDetails.getEmpresaId());
        model.addAllAttributes(stats);
        return "almacen/dashboard";
    }
}
