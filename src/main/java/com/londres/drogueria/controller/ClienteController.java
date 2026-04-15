package com.londres.drogueria.controller;

import com.londres.drogueria.config.CustomUserDetails;
import com.londres.drogueria.model.Cliente;
import com.londres.drogueria.service.ClienteService;
import com.londres.drogueria.service.ReniecService;
import com.londres.drogueria.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ReniecService reniecService;

    @GetMapping("/api/verificar/{dni}")
    @ResponseBody
    public java.util.Map<String, String> verificar(@PathVariable String dni) {
        return reniecService.consultarDni(dni);
    }

    @GetMapping
    public String lista(Model model, @RequestParam(required = false) String search, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long empresaId = userDetails.getEmpresaId();
        if (search != null && !search.isEmpty()) {
            model.addAttribute("clientes", clienteRepository.findFiltered(empresaId, search));
        } else {
            model.addAttribute("clientes", clienteRepository.findByEmpresaId(empresaId));
        }
        
        // KPIs
        model.addAttribute("totalClientes", clienteRepository.countByEmpresaId(empresaId));
        model.addAttribute("clientesActivos", clienteRepository.countByEmpresaIdAndActivoTrue(empresaId));
        model.addAttribute("clientesInactivos", clienteRepository.countByEmpresaIdAndActivoFalse(empresaId));
        
        return "admin/clientes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model, 
                       @RequestParam(required = false) String dni,
                       @RequestParam(required = false) String nombres,
                       @RequestParam(required = false) String paterno,
                       @RequestParam(required = false) String materno) {
        Cliente cliente = new Cliente();
        if (dni != null) {
            cliente.setDni(dni);
            cliente.setNombres(nombres);
            cliente.setApellidos(paterno + " " + materno);
        }
        model.addAttribute("cliente", cliente);
        return "admin/clientes/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clienteService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado")));
        return "admin/clientes/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Cliente cliente, RedirectAttributes redirectAttributes) {
        try {
            clienteService.guardar(cliente);
            redirectAttributes.addFlashAttribute("success", "Cliente guardado con éxito");
            return "redirect:/admin/clientes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
            return "redirect:/admin/clientes/nuevo";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            clienteService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Cliente eliminado con éxito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar: " + e.getMessage());
        }
        return "redirect:/admin/clientes";
    }
}
