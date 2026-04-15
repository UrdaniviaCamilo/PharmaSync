package com.londres.drogueria.controller;

import com.londres.drogueria.config.CustomUserDetails;
import com.londres.drogueria.model.Usuario;
import com.londres.drogueria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil/ajustes")
public class PerfilController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private com.londres.drogueria.repository.EmpresaRepository empresaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //  MOSTRAR DATOS EN LA VISTA
    @GetMapping
    public String index(Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null)
            return "redirect:/login";

        Usuario user = usuarioRepository.findById(userDetails.getUsuario().getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        model.addAttribute("usuario", user);

        return "perfil/ajustes";
    }

    // ACTUALIZAR DATOS
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Usuario usuarioForm,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario user = usuarioRepository.findById(userDetails.getUsuario().getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            usuarioRepository.findByEmail(usuarioForm.getEmail()).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new RuntimeException("El correo electrónico " + usuarioForm.getEmail() + " ya está en uso por otra cuenta.");
                }
            });

            user.setNombres(usuarioForm.getNombres());
            user.setApellidos(usuarioForm.getApellidos());
            user.setEmail(usuarioForm.getEmail());
            user.setTelefono(usuarioForm.getTelefono());

            usuarioRepository.save(user);

            //  ACTUALIZAR SESIÓN
            userDetails.getUsuario().setNombres(user.getNombres());
            userDetails.getUsuario().setApellidos(user.getApellidos());
            userDetails.getUsuario().setEmail(user.getEmail());
            userDetails.getUsuario().setTelefono(user.getTelefono());

            redirectAttributes.addFlashAttribute("success", "Datos actualizados correctamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        return "redirect:/perfil/ajustes";
    }

    //  FOTO PERFIL
    @PostMapping("/foto")
    public String guardarFoto(@RequestParam("fotoPerfil") String fotoPerfil,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario user = usuarioRepository.findById(userDetails.getUsuario().getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            user.setFotoPerfil(fotoPerfil);
            usuarioRepository.save(user);

            userDetails.getUsuario().setFotoPerfil(fotoPerfil);
            redirectAttributes.addFlashAttribute("success", "Foto actualizada");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        return "redirect:/perfil/ajustes";
    }

    //  CAMBIAR PASSWORD
    @PostMapping("/cambiar-password")
    public String cambiarPassword(@RequestParam String passwordActual,
            @RequestParam String passwordNueva,
            @RequestParam String passwordConfirm,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario user = usuarioRepository.findById(userDetails.getUsuario().getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (!passwordEncoder.matches(passwordActual, user.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Contraseña actual incorrecta");
                return "redirect:/perfil/ajustes";
            }

            if (!passwordNueva.equals(passwordConfirm)) {
                redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
                return "redirect:/perfil/ajustes";
            }

            if (passwordNueva.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Mínimo 6 caracteres");
                return "redirect:/perfil/ajustes";
            }

            user.setPassword(passwordEncoder.encode(passwordNueva));
            usuarioRepository.save(user);

            redirectAttributes.addFlashAttribute("success", "Contraseña actualizada");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        return "redirect:/perfil/ajustes";
    }
}