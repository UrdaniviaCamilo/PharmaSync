package com.londres.drogueria.controller;

import com.londres.drogueria.model.Proveedor;
import com.londres.drogueria.service.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import com.londres.drogueria.config.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.londres.drogueria.service.ProductoService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public String lista(Model model, @AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam(required = false) String search) {
        java.util.List<Proveedor> lista;
        if (search != null && !search.isEmpty()) {
            lista = proveedorService.buscar(search);
        } else {
            lista = proveedorService.listarTodos();
        }
        

        model.addAttribute("proveedores", lista);
        

        long totalProv = lista.size();
        long activos = lista.stream().filter(Proveedor::isActivo).count();
        
        model.addAttribute("totalProv", totalProv);
        model.addAttribute("activos", activos);
        model.addAttribute("inactivos", totalProv - activos);
        model.addAttribute("searchQuery", search);
        
        return "admin/proveedores/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        model.addAttribute("proveedor", new Proveedor());
        model.addAttribute("productos", productoService.listarTodos(userDetails.getEmpresaId()));
        model.addAttribute("modo", "crear");
        return "admin/proveedores/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        proveedorService.buscarPorId(id).ifPresent(p -> {
            model.addAttribute("proveedor", p);
        });
        model.addAttribute("productos", productoService.listarTodos(userDetails.getEmpresaId()));
        model.addAttribute("modo", "editar");
        return "admin/proveedores/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Proveedor proveedor, RedirectAttributes redirectAttributes) {
        try {
            proveedorService.guardar(proveedor);
            redirectAttributes.addFlashAttribute("success", "Proveedor guardado con éxito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
        }
        return "redirect:/admin/proveedores";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            proveedorService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Proveedor eliminado con éxito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar: " + e.getMessage());
        }
        return "redirect:/admin/proveedores";
    }
}
