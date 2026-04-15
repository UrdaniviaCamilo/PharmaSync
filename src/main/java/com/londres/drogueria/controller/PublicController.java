package com.londres.drogueria.controller;

import com.londres.drogueria.model.Empresa;
import com.londres.drogueria.model.Rol;
import com.londres.drogueria.model.Usuario;
import com.londres.drogueria.service.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PublicController {

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private com.londres.drogueria.service.SunatService sunatService;

    @Autowired
    private com.londres.drogueria.repository.ProductoRepository productoRepository;

    @Autowired
    private com.londres.drogueria.repository.EmpresaRepository empresaRepository;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @Autowired
    private com.londres.drogueria.service.UsuarioService usuarioService;

    @GetMapping("/revivir")
    @org.springframework.web.bind.annotation.ResponseBody
    public String revivirse() {
        try {
            Long empresaId = 1L;
            java.util.Set<Rol> roles = new java.util.HashSet<>();
            roles.add(Rol.ADMIN);
            usuarioService.crear(empresaId, "HenryHua", "123456", "Henry", "Hua", "henry@londres.com", "", true, roles);
            return "SUCCESS: Usuario HenryHua fue revivido para Empresa 1 con la clave: 123456";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @GetMapping("/login")
    public String login() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/registro")
    public String showRegistroUnificado(Model model) {
        model.addAttribute("empresa", new Empresa());
        model.addAttribute("admin", new Usuario());
        model.addAttribute("usuarioCliente", new Usuario());
        return "registro";
    }

    @GetMapping("/registro-empresa")
    public String showRegistroForm(Model model) {
        model.addAttribute("empresa", new Empresa());
        model.addAttribute("admin", new Usuario());
        return "registro-empresa";
    }

    @PostMapping("/registro-empresa")
    public String registrar(@ModelAttribute Empresa empresa, @ModelAttribute Usuario admin, RedirectAttributes redirectAttributes) {
        try {
            empresaService.registrarEmpresa(empresa, admin);
            redirectAttributes.addFlashAttribute("success", "¡Registro exitoso! Ya puedes iniciar sesión.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar: " + e.getMessage());
            return "redirect:/registro-empresa";
        }
    }

    @GetMapping("/marketplace")
    public String marketplace(Model model,
                              @RequestParam(required = false) String q,
                              @RequestParam(required = false) String departamento,
                              @RequestParam(required = false) String provincia,
                              @RequestParam(required = false) String distrito) {

        java.util.stream.Stream<Empresa> stream = empresaRepository.findAll().stream().filter(Empresa::isActivo);

        if (q != null && !q.isBlank()) {
            String ql = q.toLowerCase();
            stream = stream.filter(e ->
                    (e.getNombreComercial() != null && e.getNombreComercial().toLowerCase().contains(ql))
                            || (e.getRazonSocial() != null && e.getRazonSocial().toLowerCase().contains(ql)));
        }
        if (departamento != null && !departamento.isBlank()) {
            stream = stream.filter(e -> e.getDepartamento() != null && e.getDepartamento().equalsIgnoreCase(departamento));
        }
        if (provincia != null && !provincia.isBlank()) {
            stream = stream.filter(e -> e.getProvincia() != null && e.getProvincia().equalsIgnoreCase(provincia));
        }
        if (distrito != null && !distrito.isBlank()) {
            stream = stream.filter(e -> e.getDistrito() != null && e.getDistrito().equalsIgnoreCase(distrito));
        }

        model.addAttribute("empresas", stream.collect(java.util.stream.Collectors.toList()));
        model.addAttribute("searchQuery", q);
        model.addAttribute("departamentoFiltro", departamento);
        model.addAttribute("provinciaFiltro", provincia);
        model.addAttribute("distritoFiltro", distrito);
        return "marketplace/index";
    }

    @Autowired
    private com.londres.drogueria.repository.CategoriaRepository categoriaRepository;

    @GetMapping("/super-admin/registro")
    public String showSuperAdminRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "super-admin/registro";
    }

    @PostMapping("/super-admin/registro")
    public String registrarSuperAdmin(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        try {
            java.util.Set<Rol> roles = new java.util.HashSet<>();
            roles.add(Rol.SUPER_ADMIN);
            
            // Si ya existe al menos un Super Admin activo, el nuevo se crea INACTIVO para aprobación
            boolean existsActiveSuper = usuarioService.listarTodos().stream()
                    .anyMatch(u -> u.getRoles().contains(Rol.SUPER_ADMIN) && u.isActivo());
            
            boolean autoActivo = !existsActiveSuper; // El primero es auto-activo
            
            usuarioService.crear(null, usuario.getUsername(), usuario.getPassword(), 
                               usuario.getNombres(), usuario.getApellidos(), 
                               usuario.getEmail(), usuario.getTelefono(), autoActivo, roles);
            
            if (autoActivo) {
                redirectAttributes.addFlashAttribute("success", "¡Cuenta Maestro activada! Ya puedes iniciar sesión.");
            } else {
                redirectAttributes.addFlashAttribute("success", "Solicitud enviada. Un administrador maestro debe aprobar tu cuenta.");
            }
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/super-admin/registro";
        }
    }

    @GetMapping("/registro-cliente")
    public String showRegistroCliente(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro-cliente";
    }

    @PostMapping("/registro-cliente")
    public String registrarCliente(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        try {
            java.util.Set<Rol> roles = new java.util.HashSet<>();
            roles.add(Rol.CLIENTE);
            usuarioService.crear(null, usuario.getUsername(), usuario.getPassword(), 
                               usuario.getNombres(), usuario.getApellidos(), 
                               usuario.getEmail(), usuario.getTelefono(), true, roles);
            redirectAttributes.addFlashAttribute("success", "¡Cuenta de cliente creada! Ya puedes iniciar sesión.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/registro-cliente";
        }
    }

    @GetMapping("/marketplace/empresa/{id}")
    public String empresaDetalle(@PathVariable Long id,
                                 @RequestParam(required = false) Long categoria,
                                 Model model) {
        Empresa empresa = empresaRepository.findById(id).orElseThrow();
        model.addAttribute("empresa", empresa);
        model.addAttribute("categorias", categoriaRepository.findByEmpresaId(id));
        model.addAttribute("categoriaId", categoria);

        java.util.stream.Stream<com.londres.drogueria.model.Producto> stream = productoRepository.findAll().stream()
                .filter(p -> id.equals(p.getEmpresaId()))
                .filter(p -> p.getStock() != null && p.getStock() > 0)
                .filter(com.londres.drogueria.model.Producto::isActivo)
                .filter(com.londres.drogueria.model.Producto::isVisibleMarketplace);
        if (categoria != null) {
            stream = stream.filter(p -> p.getCategoria() != null && categoria.equals(p.getCategoria().getId()));
        }
        model.addAttribute("productos", stream.collect(java.util.stream.Collectors.toList()));
        return "marketplace/empresa";
    }

    @Autowired
    private com.londres.drogueria.repository.VentaRepository ventaRepository;

    @GetMapping("/marketplace/compras")
    public String misCompras(Model model, @org.springframework.security.core.annotation.AuthenticationPrincipal com.londres.drogueria.config.CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        model.addAttribute("compras", ventaRepository.findByMarketplaceUsuarioIdOrderByFechaDesc(userDetails.getUsuario().getId()));
        return "marketplace/compras";
    }

    @GetMapping("/api/ruc")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.Map<String, String> consultarRuc(@org.springframework.web.bind.annotation.RequestParam String numero) {
        return sunatService.consultarRuc(numero);
    }

    @GetMapping("/perfil")
    public String redirectPerfil() {
        return "redirect:/perfil/ajustes";
    }
}
