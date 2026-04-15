package com.londres.drogueria.controller;

import com.londres.drogueria.model.Usuario;
import com.londres.drogueria.service.SeguidorEmpresaService;
import com.londres.drogueria.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/empresas")
public class SeguidorController {

    @Autowired
    private SeguidorEmpresaService seguidorEmpresaService;

    @Autowired
    private UsuarioService usuarioService;

    private Long getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            Usuario usuario = usuarioService.findByUsername(auth.getName()).orElse(null);
            return usuario != null ? usuario.getId() : null;
        }
        return null;
    }

    @PostMapping("/{id}/seguir")
    public ResponseEntity<?> toggleSeguir(@PathVariable Long id) {
        Long userId = getAuthenticatedUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Debes iniciar sesión para seguir a una farmacia"));
        }

        try {
            boolean isFollowing = seguidorEmpresaService.toggleSeguir(userId, id);
            return ResponseEntity.ok(Map.of("following", isFollowing, "success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/is-following")
    public ResponseEntity<?> checkIsFollowing(@PathVariable Long id) {
        Long userId = getAuthenticatedUserId();
        if (userId == null) {
            return ResponseEntity.ok(Map.of("following", false));
        }
        boolean isFollowing = seguidorEmpresaService.isSiguiendo(userId, id);
        return ResponseEntity.ok(Map.of("following", isFollowing));
    }
}
