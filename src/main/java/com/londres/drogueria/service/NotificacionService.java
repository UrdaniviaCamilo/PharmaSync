package com.londres.drogueria.service;

import com.londres.drogueria.model.Notificacion;
import com.londres.drogueria.model.Usuario;
import com.londres.drogueria.repository.NotificacionRepository;
import com.londres.drogueria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public Notificacion crearNotificacion(Long usuarioId, String mensaje, String url) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return crearNotificacion(usuario, mensaje, url);
    }

    @Transactional
    public Notificacion crearNotificacion(Usuario usuario, String mensaje, String url) {
        if (usuario == null || usuario.getId() == null) {
            return null;
        }
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setMensaje(mensaje);
        notificacion.setUrl(url);
        return notificacionRepository.save(notificacion);
    }

    public List<Notificacion> getNotificacionesByUsuario(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    public List<Notificacion> getNotificacionesNoLeidas(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdAndLeidaFalseOrderByFechaDesc(usuarioId);
    }

    public long getCountNotificacionesNoLeidas(Long usuarioId) {
        return notificacionRepository.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    @Transactional
    public void marcarComoLeida(Long notificacionId, Long usuarioId) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
            .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        if (notificacion.getUsuario().getId().equals(usuarioId)) {
            notificacion.setLeida(true);
            notificacionRepository.save(notificacion);
        }
    }
    
    @Transactional
    public void marcarTodasComoLeidas(Long usuarioId) {
        List<Notificacion> noLeidas = notificacionRepository.findByUsuarioIdAndLeidaFalseOrderByFechaDesc(usuarioId);
        noLeidas.forEach(n -> n.setLeida(true));
        notificacionRepository.saveAll(noLeidas);
    }
}
