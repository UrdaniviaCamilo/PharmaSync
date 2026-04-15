package com.londres.drogueria.controller;

import com.londres.drogueria.config.CustomUserDetails;
import com.londres.drogueria.model.Producto;
import com.londres.drogueria.service.ProductoService;
import com.londres.drogueria.service.FileStorageService;
import com.londres.drogueria.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private com.londres.drogueria.repository.ProductoRepository productoRepository;

    @GetMapping
    public String lista(Model model,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false, defaultValue = "false") Boolean stockBajo,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long empresaId = userDetails.getEmpresaId();
        List<Producto> productos = productoService.buscar(empresaId, search, categoriaId,
                stockBajo != null && stockBajo);

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("categoriaSeleccionada", categoriaId);
        model.addAttribute("searchQuery", search);
        model.addAttribute("stockBajoFiltro", stockBajo);
        model.addAttribute("totalProductos", productoRepository.countByEmpresaId(empresaId));
        model.addAttribute("productosBajoStock", productoService.stockBajo(empresaId).size());
        model.addAttribute("productosVencimiento", productoService.listarPorVencimientoProximo(empresaId).size());

        return "admin/productos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "admin/productos/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("producto", productoService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado")));
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "admin/productos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Producto producto,
            @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
            RedirectAttributes redirectAttributes) {
        try {
            if (imagenFile != null && !imagenFile.isEmpty()) {
                String path = fileStorageService.storeFile(imagenFile, "productos");
                producto.setImagenPath(path);
            }
            productoService.guardar(producto);
            redirectAttributes.addFlashAttribute("success", "Producto guardado con éxito");
            return "redirect:/admin/productos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
            return "redirect:/admin/productos/nuevo";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productoService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Producto eliminado con éxito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar: " + e.getMessage());
        }
        return "redirect:/admin/productos";
    }

    @GetMapping("/buscar")
    @ResponseBody
    public List<Producto> buscar(@RequestParam("q") String query) {
        return productoRepository.findByNombreContainingIgnoreCase(query);
    }

    @GetMapping("/api/buscar")
    @ResponseBody
    public List<ProductoDTO> buscarApi(@RequestParam String q) {
        return productoService.buscarPredictivo(q).stream()
                .map(p -> new ProductoDTO(p.getId(), p.getNombre(), p.getLaboratorio(), p.getPrecioVenta(),
                        p.getStock(), p.isRequiereReceta()))
                .collect(Collectors.toList());
    }


    public static class ProductoDTO {
        public Long id;
        public String nombre;
        public String laboratorio;
        public java.math.BigDecimal precio;
        public Integer stock;
        public boolean requiereReceta;

        public ProductoDTO(Long id, String nombre, String laboratorio, java.math.BigDecimal precio, Integer stock,
                boolean requiereReceta) {
            this.id = id;
            this.nombre = nombre;
            this.laboratorio = laboratorio;
            this.precio = precio;
            this.stock = stock;
            this.requiereReceta = requiereReceta;
        }
    }
}
