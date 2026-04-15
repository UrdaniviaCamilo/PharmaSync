package com.londres.drogueria.controller;

import com.londres.drogueria.config.CustomUserDetails;
import com.londres.drogueria.config.TenantContext;
import com.londres.drogueria.model.*;
import com.londres.drogueria.repository.ConfiguracionRepository;
import com.londres.drogueria.repository.EmpresaRepository;
import com.londres.drogueria.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Optional;

@Controller
@RequestMapping("/admin/ajustes")
public class AdminConfigController {

    @Autowired
    private ConfiguracionRepository configuracionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String index(Model model) {
        Long empresaId = TenantContext.getCurrentTenant();
        model.addAttribute("config",
                configuracionRepository.findByEmpresaId(empresaId).orElse(new Configuracion()));
        return "admin/ajustes/index";
    }

    @PostMapping("/tema")
    public String cambiarTema(@RequestParam("tema") String tema, HttpSession session) {
        session.setAttribute("tema", tema);
        return "redirect:/admin/ajustes?success";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Configuracion config, RedirectAttributes redirectAttributes) {
        try {
            Long empresaId = TenantContext.getCurrentTenant();
            Configuracion existing = configuracionRepository.findByEmpresaId(empresaId).orElse(new Configuracion());


            if (config.getLogoUrl() == null || config.getLogoUrl().isEmpty()) {
                config.setLogoUrl(existing.getLogoUrl());
            }

            config.setEmpresaId(empresaId);
            configuracionRepository.save(config);
            redirectAttributes.addFlashAttribute("success", "Configuración actualizada con éxito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
        }
        return "redirect:/admin/ajustes";
    }

    @PostMapping("/logo")
    public String guardarLogo(@RequestParam("logoUrl") String logoUrl,
            RedirectAttributes redirectAttributes) {
        try {
            Long empresaId = TenantContext.getCurrentTenant();
            Configuracion config = configuracionRepository.findByEmpresaId(empresaId)
                    .orElseGet(() -> {
                        Configuracion newC = new Configuracion();
                        newC.setEmpresaId(empresaId);
                        return newC;
                    });
            config.setLogoUrl(logoUrl);
            configuracionRepository.save(config);

            // ACTUALIZAR LOGO EN EL NEGOCIO (MARKETPLACE GRID)
            Empresa emp = empresaRepository.findById(empresaId).orElse(null);
            if (emp != null) {
                emp.setLogoPath(logoUrl);
                empresaRepository.save(emp);
            }

            redirectAttributes.addFlashAttribute("success", "Logo de la empresa actualizado");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar logo: " + e.getMessage());
        }
        return "redirect:/admin/ajustes";
    }
}
