package com.londres.drogueria.controller;

import com.londres.drogueria.model.Notificacion;
import com.londres.drogueria.model.Usuario;
import com.londres.drogueria.service.NotificacionService;
import com.londres.drogueria.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

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

    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications() {
        Long userId = getAuthenticatedUserId();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        
        List<Notificacion> noLeidas = notificacionService.getNotificacionesNoLeidas(userId);
        return ResponseEntity.ok(noLeidas);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllNotifications() {
        Long userId = getAuthenticatedUserId();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        
        List<Notificacion> todas = notificacionService.getNotificacionesByUsuario(userId);
        return ResponseEntity.ok(todas);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        Long userId = getAuthenticatedUserId();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        
        try {
            notificacionService.marcarComoLeida(id, userId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        Long userId = getAuthenticatedUserId();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        
        try {
            notificacionService.marcarTodasComoLeidas(userId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
