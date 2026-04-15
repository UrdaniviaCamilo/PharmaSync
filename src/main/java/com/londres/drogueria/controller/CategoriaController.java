package com.londres.drogueria.controller;

import com.londres.drogueria.model.Categoria;
import com.londres.drogueria.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categorias")
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class CategoriaController {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private com.londres.drogueria.service.FileStorageService fileStorageService;

    @GetMapping
    public String lista(Model model) {
        Long empresaId = com.londres.drogueria.config.TenantContext.getCurrentTenant();
        java.util.List<Categoria> categorias = categoriaRepository.findByEmpresaId(empresaId);
        model.addAttribute("categorias", categorias);
        model.addAttribute("nuevaCategoria", new Categoria());
        
        addStatsToModel(model, categorias);
        
        return "admin/categorias/lista";
    }

    private void addStatsToModel(Model model, java.util.List<Categoria> categorias) {
        String catMasStock = "-";
        int maxStockGlobal = -1;
        long conImagenLocalCount = 0;
        
        java.util.Map<Long, Integer> productCountMap = new java.util.HashMap<>();
        java.util.Map<Long, Integer> stockTotalMap = new java.util.HashMap<>();

        if (categorias != null) {
            for (Categoria c : categorias) {
                // Calcular stock de esta categoria
                int catProductCount = 0;
                int catTotalStock = 0;

                if (c.getProductos() != null) {
                    catProductCount = c.getProductos().size();
                    catTotalStock = c.getProductos().stream()
                            .mapToInt(p -> p.getStock() != null ? p.getStock() : 0)
                            .sum();
                }
                
                // Guardar en mapas para la vista
                productCountMap.put(c.getId(), catProductCount);
                stockTotalMap.put(c.getId(), catTotalStock);

                // Estadisticas globales
                if (catTotalStock > maxStockGlobal) {
                    maxStockGlobal = catTotalStock;
                    catMasStock = (c.getNombre() != null) ? c.getNombre() : "-";
                }

                // Contar imágenes locales
                if (c.getImagenPath() != null && c.getImagenPath().startsWith("/uploads/")) {
                    conImagenLocalCount++;
                }
            }
        }
        
        model.addAttribute("catMasStock", catMasStock);
        model.addAttribute("conImagenLocalCount", conImagenLocalCount);
        model.addAttribute("productCountMap", productCountMap);
        model.addAttribute("stockTotalMap", stockTotalMap);
    }

    @PostMapping("/guardar")
    @org.springframework.transaction.annotation.Transactional
    public String guardar(@ModelAttribute Categoria categoria, 
                          @RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file,
                          RedirectAttributes redirectAttributes) {
        try {
            // Procesar archivo local si existe
            if (file != null && !file.isEmpty()) {
                String localPath = fileStorageService.storeFile(file, "categorias");
                categoria.setImagenPath(localPath);
            }

            if (categoria.getId() != null) {

                Categoria existing = categoriaRepository.findById(categoria.getId())
                        .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
                existing.setNombre(categoria.getNombre());
                existing.setDescripcion(categoria.getDescripcion());
                // Solo actualizar imagen si se proporcionó una nueva (vía archivo o URL)
                if (categoria.getImagenPath() != null && !categoria.getImagenPath().isBlank()) {
                    existing.setImagenPath(categoria.getImagenPath());
                }
                categoriaRepository.save(existing);
            } else {
                categoriaRepository.save(categoria);
            }
            redirectAttributes.addFlashAttribute("success", "Categoría guardada con éxito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
        }
        return "redirect:/admin/categorias";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Long empresaId = com.londres.drogueria.config.TenantContext.getCurrentTenant();
        java.util.List<Categoria> categorias = categoriaRepository.findByEmpresaId(empresaId);
        model.addAttribute("categorias", categorias);
        model.addAttribute("nuevaCategoria", categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada")));
        
        addStatsToModel(model, categorias);
        
        return "admin/categorias/lista";
    }

    @GetMapping("/eliminar/{id}")
    @org.springframework.transaction.annotation.Transactional
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Categoria categoria = categoriaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            
            if (categoria.getProductos() != null && !categoria.getProductos().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se puede eliminar una categoría que tiene productos asociados");
            } else {
                categoriaRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Categoría eliminada con éxito");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/admin/categorias";
    }
    @GetMapping("/{id}/productos")
    @ResponseBody
    public org.springframework.http.ResponseEntity<java.util.List<java.util.Map<String, Object>>> getProductos(@PathVariable Long id) {
        Categoria cat = categoriaRepository.findById(id).orElse(null);
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        
        if (cat != null && cat.getProductos() != null) {
            for (com.londres.drogueria.model.Producto p : cat.getProductos()) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("nombre", p.getNombre());
                map.put("stock", p.getStock() != null ? p.getStock() : 0);
                map.put("precio", p.getPrecioVenta() != null ? p.getPrecioVenta() : java.math.BigDecimal.ZERO);
                result.add(map);
            }
        }
        return org.springframework.http.ResponseEntity.ok(result);
    }
}
