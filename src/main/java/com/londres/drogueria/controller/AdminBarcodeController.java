package com.londres.drogueria.controller;

import com.londres.drogueria.model.Producto;
import com.londres.drogueria.service.BarcodeService;
import com.londres.drogueria.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@Controller
@RequestMapping("/admin/codigos")
public class AdminBarcodeController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private BarcodeService barcodeService;

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("productos", productoRepository.findAll());
        return "admin/codigos/lista";
    }

    @GetMapping("/producto/{id}")
    public String verCodigos(@PathVariable Long id, Model model) throws Exception {
        Optional<Producto> productoOpt = productoRepository.findById(id);
        if (productoOpt.isPresent()) {
            Producto p = productoOpt.get();
            model.addAttribute("producto", p);
            
            // Generate QR
            model.addAttribute("qrCode", barcodeService.generateQRCode("PROD-" + p.getId(), 250, 250));
            
            // Generate EAN13
            if (p.getCodigoEan13() != null && p.getCodigoEan13().length() == 13) {
                try {
                    model.addAttribute("ean13", barcodeService.generateEAN13(p.getCodigoEan13()));
                } catch (IllegalArgumentException e) {
                    // Invalid checksum or format, skip EAN13 generation
                    model.addAttribute("ean13Error", "El código (" + p.getCodigoEan13() + ") tiene un dígito verificador inválido.");
                }
            }
            
            // Generate Code128
            model.addAttribute("code128", barcodeService.generateCode128("PROD-" + p.getId()));
            
            return "admin/codigos/producto";
        }
        return "redirect:/admin/codigos";
    }
}
