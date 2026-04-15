package com.londres.drogueria.controller;

import com.londres.drogueria.config.CustomUserDetails;
import com.londres.drogueria.model.Rol;
import com.londres.drogueria.model.Usuario;
import com.londres.drogueria.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String lista(Model model) {
        List<Usuario> usuarios = usuarioService.listarTodos();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("totalAdmin", usuarios.stream()
                .filter(u -> u.getRoles().contains(Rol.ADMIN)).count());
        model.addAttribute("totalEmp", usuarios.stream()
                .filter(u -> !u.getRoles().contains(Rol.ADMIN)).count());
        return "admin/usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", Rol.values());
        model.addAttribute("modo", "crear");
        return "admin/usuarios/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        usuarioService.buscarPorId(id).ifPresent(u -> model.addAttribute("usuario", u));
        model.addAttribute("roles", Rol.values());
        model.addAttribute("modo", "editar");
        return "admin/usuarios/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam("username") String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "nombres", defaultValue = "") String nombres,
            @RequestParam(value = "apellidos", defaultValue = "") String apellidos,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "telefono", required = false) String telefono,
            // 'activoCheck' name avoids the hidden-input conflict with boolean conversion
            @RequestParam(value = "activoCheck", required = false) String activoStr,
            @RequestParam(value = "roles", required = false) List<String> rolesParam,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            // activo is true ONLY when the checkbox was checked (activoStr == "true")
            boolean activo = "true".equalsIgnoreCase(activoStr);


            Set<Rol> roles = new HashSet<>();
            if (rolesParam != null) {
                for (String r : rolesParam) {
                    try {
                        roles.add(Rol.valueOf(r.trim().toUpperCase()));
                    } catch (IllegalArgumentException ignored) {
                        System.err.println("Unknown role: " + r);
                    }
                }
            }
            if (roles.isEmpty()) {
                roles.add(Rol.VENDEDOR);
            }

            if (id == null) {
                usuarioService.crear(
                    userDetails.getEmpresaId(),
                    username, password, nombres, apellidos,
                    email, telefono, activo, roles
                );
                redirectAttributes.addFlashAttribute("success", "Usuario creado con éxito. Ya puede iniciar sesión.");
            } else {
                usuarioService.actualizar(id, password, nombres, apellidos, email, telefono, activo, roles);
                redirectAttributes.addFlashAttribute("success", "Usuario actualizado con éxito.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Usuario eliminado con éxito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
}
