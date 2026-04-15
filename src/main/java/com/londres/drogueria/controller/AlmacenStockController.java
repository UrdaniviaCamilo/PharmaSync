package com.londres.drogueria.controller;

import com.londres.drogueria.config.CustomUserDetails;
import com.londres.drogueria.model.Producto;
import com.londres.drogueria.service.ProductoService;
import com.londres.drogueria.repository.MovimientoStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/almacen/stock")
public class AlmacenStockController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private MovimientoStockRepository movimientoRepository;

    @GetMapping("/ingreso")
    public String vistaIngreso(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long empresaId = userDetails.getEmpresaId();
        model.addAttribute("productos", productoService.listarTodos(empresaId));
        model.addAttribute("recientes", movimientoRepository.findTop10ByEmpresaIdOrderByFechaDesc(empresaId));
        return "almacen/stock/ingreso";
    }

    @PostMapping("/ingreso/procesar")
    public String procesarIngreso(@RequestParam Long productoId,
                                  @RequestParam Integer cantidad,
                                  @RequestParam(required = false) String motivo,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        try {
            productoService.actualizarStock(productoId, cantidad, "INGRESO", 
                    (motivo == null || motivo.isEmpty() ? "Ingreso de Mercadería" : motivo), 
                    userDetails.getUsuario());
            
            redirectAttributes.addFlashAttribute("success", "Stock actualizado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar: " + e.getMessage());
        }
        return "redirect:/almacen/stock/ingreso";
    }

    @GetMapping("/kardex/{productoId}")
    public String verKardex(@PathVariable Long productoId, Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Producto producto = productoService.buscarPorId(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        model.addAttribute("producto", producto);
        model.addAttribute("movimientos", movimientoRepository.findKardex(userDetails.getEmpresaId(), productoId));
        return "almacen/stock/kardex";
    }
}
