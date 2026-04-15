package com.londres.drogueria.controller;

import com.londres.drogueria.config.CustomUserDetails;
import com.londres.drogueria.model.Venta;
import com.londres.drogueria.model.MetodoPago;
import com.londres.drogueria.service.VentaService;
import com.londres.drogueria.service.FileStorageService;
import com.londres.drogueria.repository.ClienteRepository;
import com.londres.drogueria.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Controller
@RequestMapping("/admin/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private com.londres.drogueria.service.CajaService cajaService;

    @GetMapping
    public String lista(Model model, 
                       @RequestParam(required = false) String numero,
                       @RequestParam(required = false) String cliente,
                       @RequestParam(required = false) MetodoPago metodo,
                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long empresaId = userDetails.getEmpresaId();
        model.addAttribute("ventas", ventaRepository.findFiltered(empresaId, numero, cliente, metodo));
        
        // KPIs
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        model.addAttribute("totalVentas", ventaRepository.countByEmpresaId(empresaId));
        model.addAttribute("ventasHoy", ventaRepository.countByFechaAfter(empresaId, startOfDay));
        model.addAttribute("montoHoy", ventaRepository.sumTotalByFechaAfter(empresaId, startOfDay));
        model.addAttribute("ventasReceta", ventaRepository.countVentasConRecetaAfter(empresaId, startOfDay));
        
        model.addAttribute("metodos", MetodoPago.values());
        return "admin/ventas/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model, RedirectAttributes redirectAttributes, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long empresaId = userDetails.getEmpresaId();
        if (cajaService.obtenerCajaAbierta(empresaId).isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "DEBE ABRIR LA CAJA PARA REALIZAR VENTAS");
            return "redirect:/admin/caja";
        }
        Venta venta = new Venta();
        venta.setDetalles(new ArrayList<>());
        model.addAttribute("venta", venta);
        model.addAttribute("clientes", clienteRepository.findByEmpresaId(empresaId));
        return "admin/ventas/nueva";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Venta venta, 
                         @RequestParam(value = "recetaFile", required = false) MultipartFile recetaFile,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        try {

            if (venta.getCliente() != null && venta.getCliente().getId() == null) {
                venta.setCliente(null);
            }


            if (recetaFile != null && !recetaFile.isEmpty()) {
                String fullPath = fileStorageService.storeFile(recetaFile, "recetas");
                venta.setRecetaPath(fullPath);
            }

            ventaService.registrarVenta(venta, userDetails.getUsuario());
            redirectAttributes.addFlashAttribute("success", "Venta registrada con éxito. N°: " + venta.getNumeroVenta());
            return "redirect:/admin/ventas";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar: " + e.getMessage());
            return "redirect:/admin/ventas/nueva";
        }
    }

    @GetMapping("/boleta/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> descargarBoleta(@PathVariable Long id) {
        byte[] pdfBytes = ventaService.generarBoletaPdf(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "boleta-venta-" + id + ".pdf");
        
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
        

        if (!venta.getEmpresaId().equals(userDetails.getEmpresaId())) {
            throw new RuntimeException("Acceso denegado");
        }

        model.addAttribute("venta", venta);
        model.addAttribute("clientes", clienteRepository.findByEmpresaId(userDetails.getEmpresaId()));
        model.addAttribute("esEdicion", true);
        return "admin/ventas/nueva"; // Using the same form for edit
    }
}
