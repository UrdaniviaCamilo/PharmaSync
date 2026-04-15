package com.londres.drogueria.controller;

import com.londres.drogueria.config.CustomUserDetails;
import com.londres.drogueria.model.Usuario;
import com.londres.drogueria.model.Venta;
import com.londres.drogueria.model.VentaEstado;
import com.londres.drogueria.repository.VentaRepository;
import com.londres.drogueria.service.NotificacionService;
import com.londres.drogueria.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/recetas")
public class RecetaEstadoController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private NotificacionService notificacionService;

    @PostMapping("/actualizar/{id}")
    public String actualizarEstado(
            @PathVariable Long id,
            @RequestParam("estado") String estadoCodigo,
            @RequestParam(value = "observaciones", required = false) String observaciones,
            RedirectAttributes ra,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            ra.addFlashAttribute("error", "Sesion no valida.");
            return "redirect:/admin/recetas";
        }

        try {
            VentaEstado nuevoEstado = VentaEstado.valueOf(estadoCodigo);
            Venta venta = ventaRepository.findById(id).orElseThrow();
            Usuario compradorMarketplace = venta.getMarketplaceUsuario();
            String motivo = observaciones != null && !observaciones.isEmpty() ? observaciones : "Sin observaciones.";
            String msj = "\n[Revisado por " + userDetails.getUsuario().getNombres() + "]: " + motivo;

            ventaService.actualizarEstado(id, nuevoEstado, msj, userDetails.getUsuario());

            if (compradorMarketplace != null) {
                if (nuevoEstado == VentaEstado.APROBADA) {
                    notificacionService.crearNotificacion(
                            compradorMarketplace,
                            "Tu receta fue aprobada. Pedido " + venta.getNumeroVenta()
                                    + " - completa el pago para finalizar.",
                            "/marketplace/checkout/pagar/" + venta.getId());
                } else if (nuevoEstado == VentaEstado.RECHAZADA) {
                    notificacionService.crearNotificacion(
                            compradorMarketplace,
                            "Tu receta fue rechazada. Pedido " + venta.getNumeroVenta() + ". Motivo: " + motivo,
                            "/marketplace");
                }
            }

            ra.addFlashAttribute("success", "Venta actualizada: " + nuevoEstado.name());
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", "Parametro invalido.");
        } catch (RuntimeException ex) {
            String msg = ex.getMessage();
            ra.addFlashAttribute("error", msg != null ? msg : "No se pudo actualizar.");
        }

        return "redirect:/admin/recetas";
    }
}
