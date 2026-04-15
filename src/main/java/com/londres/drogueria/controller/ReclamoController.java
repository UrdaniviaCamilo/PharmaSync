package com.londres.drogueria.controller;

import com.londres.drogueria.model.Reclamo;
import com.londres.drogueria.model.ReclamoEstado;
import com.londres.drogueria.repository.ReclamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ReclamoController {

    @Autowired
    private ReclamoRepository reclamoRepository;

    @Autowired
    private com.londres.drogueria.service.NotificacionService notificacionService;

    @Autowired
    private com.londres.drogueria.repository.UsuarioRepository usuarioRepository;

    // --- PÚBLICO: ENVIAR RECLAMO ---

    @GetMapping("/reclamaciones")
    public String libroReclamaciones(Model model) {
        model.addAttribute("reclamo", new Reclamo());
        return "reclamaciones/index";
    }

    @PostMapping("/reclamaciones")
    public String guardarReclamo(@ModelAttribute Reclamo reclamo, RedirectAttributes attributes) {
        try {
            reclamo.setEstado(ReclamoEstado.PENDIENTE);
            Reclamo guardado = reclamoRepository.save(reclamo);
            
            // Notificar a todos los Super Admins
            java.util.List<com.londres.drogueria.model.Usuario> supers = usuarioRepository.findByRole(com.londres.drogueria.model.Rol.SUPER_ADMIN);
            for (com.londres.drogueria.model.Usuario s : supers) {
                notificacionService.crearNotificacion(s, 
                    "Nuevo Reclamo recibido (#" + guardado.getId() + "): " + guardado.getNombreCompleto(), 
                    "/super-admin/reclamaciones");
            }
            
            attributes.addFlashAttribute("success", "Hemos registrado tu reclamo/queja exitosamente. Te contactaremos pronto.");
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "Hubo un error registrando el reclamo: " + e.getMessage());
            return "redirect:/reclamaciones";
        }
        return "redirect:/"; // Enviar al index
    }

    // --- SUPER ADMIN: GESTIÓN DE RECLAMACIONES ---

    @GetMapping("/super-admin/reclamaciones")
    public String gestionarReclamos(Model model) {
        List<Reclamo> reclamos = reclamoRepository.findAll();
        model.addAttribute("reclamaciones", reclamos);
        return "super-admin/reclamaciones";
    }

    @PostMapping("/super-admin/reclamaciones/{id}/resolver")
    public String resolverReclamo(@PathVariable Long id, @RequestParam String respuestaAdmin, RedirectAttributes attributes) {
        Reclamo reclamo = reclamoRepository.findById(id).orElse(null);
        if (reclamo != null) {
            reclamo.setRespuestaAdmin(respuestaAdmin);
            reclamo.setEstado(ReclamoEstado.RESUELTO);
            reclamoRepository.save(reclamo);
            attributes.addFlashAttribute("success", "El reclamo #" + id + " ha sido resuelto y notificado.");
        } else {
            attributes.addFlashAttribute("error", "Reclamo no encontrado.");
        }
        return "redirect:/super-admin/reclamaciones";
    }
}
