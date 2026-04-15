package com.londres.drogueria.controller;

import com.londres.drogueria.config.CustomUserDetails;
import com.londres.drogueria.model.*;
import com.londres.drogueria.repository.ProductoRepository;
import com.londres.drogueria.repository.VentaRepository;
import com.londres.drogueria.service.MercadoPagoService;
import com.londres.drogueria.service.NotificacionService;
import com.londres.drogueria.service.VentaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/marketplace/checkout")
public class CheckoutController {

    @Autowired
    private MercadoPagoService mercadoPagoService;

    @Autowired
    private VentaService ventaService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    public String viewCheckout(Model model) {
        return "marketplace/checkout";
    }

    @GetMapping("/pagar/{ventaId}")
    public String pagarPedido(@PathVariable Long ventaId, Model model,
                              @AuthenticationPrincipal CustomUserDetails principal) {
        Venta v = ventaRepository.findById(ventaId).orElseThrow();
        if (principal == null || v.getMarketplaceUsuario() == null
                || !v.getMarketplaceUsuario().getId().equals(principal.getUsuario().getId())) {
            return "redirect:/marketplace?error=acceso";
        }
        if (v.getEstado() != VentaEstado.APROBADA) {
            return "redirect:/marketplace?error=estado";
        }
        model.addAttribute("venta", v);
        model.addAttribute("metodos", MetodoPago.values());
        return "marketplace/pagar";
    }

    @PostMapping("/pagar/{ventaId}/confirmar")
    public String confirmarPago(@PathVariable Long ventaId,
                                @RequestParam MetodoPago metodo,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                RedirectAttributes ra) {
        try {
            Venta pagada = ventaService.marcarPagadaMarketplace(ventaId, metodo, principal.getUsuario());
            notificacionService.crearNotificacion(
                    principal.getUsuario(),
                    "Compra exitosa — Pedido " + pagada.getNumeroVenta() + " registrado. ¡Gracias por tu compra!",
                    "/marketplace");
            ra.addFlashAttribute("success", "¡Pago registrado correctamente!");
            return "redirect:/marketplace/checkout/success";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/marketplace/checkout/pagar/" + ventaId;
        }
    }

    @PostMapping("/pay")
    @ResponseBody
    public Map<String, Object> processPayment(
            @RequestParam("cartJson") String cartJson,
            @RequestParam("total") Double total,
            @RequestParam("metodo") String metodo,
            @RequestParam(value = "receta", required = false) org.springframework.web.multipart.MultipartFile receta,
            @RequestParam("nombres") String nombres,
            @RequestParam("apellidos") String apellidos,
            @RequestParam("email") String email,
            @RequestParam("direccion") String direccion,
            @RequestParam("telefono") String telefono,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        try {
            List<Map<String, Object>> cart = objectMapper.readValue(cartJson, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});

            if (cart == null || cart.isEmpty()) {
                return Map.of("error", "Carrito vacío");
            }

            Venta venta = new Venta();
            if (metodo != null && !metodo.equals("POR_DEFINIR") && !metodo.isEmpty()) {
                venta.setMetodoPago(MetodoPago.valueOf(metodo));
            } else {
                venta.setMetodoPago(MetodoPago.EFECTIVO); // Fallback
            }
            venta.setObservaciones("Pedido Marketplace - " + nombres + " " + apellidos + " | " + email + " | " + telefono + " | " + direccion);

            if (principal != null) {
                venta.setMarketplaceUsuario(principal.getUsuario());
            }

            if (receta != null && !receta.isEmpty()) {
                String fileName = UUID.randomUUID() + "_" + receta.getOriginalFilename();
                java.nio.file.Path path = java.nio.file.Paths.get("uploads/recetas/" + fileName);
                java.nio.file.Files.createDirectories(path.getParent());
                java.nio.file.Files.copy(receta.getInputStream(), path);
                venta.setRecetaPath("/uploads/recetas/" + fileName);
                venta.setEstado(VentaEstado.VALIDACION_PENDIENTE);
            } else {
                venta.setEstado(VentaEstado.PAGADA);
            }

            List<DetalleVenta> detalles = new ArrayList<>();
            for (Map<String, Object> item : cart) {
                DetalleVenta d = new DetalleVenta();
                Long prodId = Long.valueOf(item.get("id").toString());
                int qty = Integer.parseInt(item.get("qty").toString());

                Producto p = productoRepository.findById(prodId).orElseThrow();
                d.setProducto(p);
                d.setCantidad(qty);
                detalles.add(d);
            }
            venta.setDetalles(detalles);

            Venta savedVenta = ventaService.registrarVenta(venta, null);

            if (savedVenta.getEstado() == VentaEstado.VALIDACION_PENDIENTE) {
                return Map.of("success", true, "orderId", savedVenta.getId());
            }

            // Bypass Mercado Pago
            return Map.of("success", true, "orderId", savedVenta.getId());

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "Error al procesar: " + e.getMessage());
        }
    }

    @GetMapping("/success")
    public String success() {
        return "marketplace/success";
    }
}
