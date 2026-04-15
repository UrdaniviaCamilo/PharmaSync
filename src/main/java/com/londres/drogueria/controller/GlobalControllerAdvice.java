package com.londres.drogueria.controller;

import com.londres.drogueria.model.Configuracion;
import com.londres.drogueria.model.Usuario;
import com.londres.drogueria.model.Producto;
import com.londres.drogueria.repository.ConfiguracionRepository;
import com.londres.drogueria.repository.UsuarioRepository;
import com.londres.drogueria.repository.VentaRepository;
import com.londres.drogueria.repository.ProductoRepository;
import com.londres.drogueria.repository.CajaRepository;
import com.londres.drogueria.repository.EmpresaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import java.util.Optional;
import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ConfiguracionRepository configuracionRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CajaRepository cajaRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private com.londres.drogueria.repository.ReclamoRepository reclamoRepository;

    @Autowired
    private com.londres.drogueria.service.DigemidScraperService digemidScraperService;

    @Autowired
    private VentaRepository ventaRepository;

    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            // Usuario actual
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(auth.getName());
            if (usuarioOpt.isPresent()) {
                Usuario u = usuarioOpt.get();
                model.addAttribute("usuario", u);
                Long empresaId = u.getEmpresaId();

                if (empresaId != null) {
                    // Configuración
                    Configuracion config = configuracionRepository.findByEmpresaId(empresaId)
                            .orElse(new Configuracion());
                    model.addAttribute("config", config);

                    // SINCRO DATOS: Asegurar que el logo del marketplace coincida con 'Configuración'
                    if (config.getLogoUrl() != null && !config.getLogoUrl().isEmpty()) {
                        empresaRepository.findById(empresaId).ifPresent(e -> {
                            if (!config.getLogoUrl().equals(e.getLogoPath())) {
                                e.setLogoPath(config.getLogoUrl());
                                empresaRepository.save(e);
                            }
                        });
                    }
                    
                    // Thema de interfaz
                    String tema = config.getTema();
                    model.addAttribute("tema", tema != null ? tema : "light");

                    // Alertas de stock bajo
                    List<Producto> lowStock = productoRepository.findLowStockProducts(empresaId);
                    model.addAttribute("cantidadStockBajo", (long) lowStock.size());
                    model.addAttribute("listaStockBajo", productoRepository.findTop10LowStockProducts(empresaId));

                    // Alertas de vencimiento (30 días)
                    java.time.LocalDate next30Days = java.time.LocalDate.now().plusDays(30);
                    List<Producto> porVencer = productoRepository.findExpiringProducts(empresaId, next30Days);
                    model.addAttribute("cantidadPorVencer", porVencer.size());
                    model.addAttribute("listaPorVencer", porVencer);
                    
                    // Estado de caja
                    boolean cajaAbierta = cajaRepository.findFirstByEmpresaIdAndAbiertaTrue(empresaId).isPresent();
                    model.addAttribute("cajaAbierta", cajaAbierta);

                    // Total local notifications
                    model.addAttribute("localNotifCount", (long) (lowStock.size() + porVencer.size()));
                } else {
                    model.addAttribute("tema", "dark"); // Default for Super Admin
                    model.addAttribute("localNotifCount", 0L);
                    model.addAttribute("cajaAbierta", false);
                }

                // Página activa
                String uri = request.getRequestURI();
                String paginaActiva = "dashboard";
                if (uri.contains("/super-admin")) paginaActiva = "super-admin";
                else if (uri.contains("/productos")) paginaActiva = "productos";
                else if (uri.contains("/ventas")) paginaActiva = "ventas";
                else if (uri.contains("/recetas")) paginaActiva = "recetas";
                else if (uri.contains("/clientes")) paginaActiva = "clientes";
                else if (uri.contains("/usuarios")) paginaActiva = "usuarios";
                else if (uri.contains("/proveedores")) paginaActiva = "proveedores";
                else if (uri.contains("/caja")) paginaActiva = "caja";
                else if (uri.contains("/reportes")) paginaActiva = "reportes";
                else if (uri.contains("/perfil")) paginaActiva = "perfil";
                else if (uri.contains("/ajustes")) paginaActiva = "ajustes";
                
                model.addAttribute("paginaActiva", paginaActiva);

                // Prescription Pending Count
                if (empresaId != null) {
                    long pendingRecetas = ventaRepository.findAll().stream()
                            .filter(v -> java.util.Objects.equals(v.getEmpresaId(), empresaId) && v.getEstado() == com.londres.drogueria.model.VentaEstado.VALIDACION_PENDIENTE)
                            .count();
                    model.addAttribute("cantidadRecetasPendientes", pendingRecetas);
                }

                // DIGEMID Alerts (Visible for everyone)
                List<java.util.Map<String, String>> digemidAlerts = digemidScraperService.getLatestAlerts();
                model.addAttribute("digemidAlerts", digemidAlerts);
                model.addAttribute("totalDigemidAlerts", digemidAlerts.size());

                // Libro de Reclamaciones (Global Pending count for Super Admin)
                long pendingReclamaciones = reclamoRepository.countByEstado(com.londres.drogueria.model.ReclamoEstado.PENDIENTE);
                model.addAttribute("cantidadReclamacionesPendientes", pendingReclamaciones);

                // Total notifications (Local + Global)
                Long localCount = (Long) model.getAttribute("localNotifCount");
                model.addAttribute("totalNotificaciones", (localCount != null ? localCount : 0) + digemidAlerts.size());
            }
        }
    }
}
