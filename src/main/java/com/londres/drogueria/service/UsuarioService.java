package com.londres.drogueria.service;

import com.londres.drogueria.config.TenantContext;
import com.londres.drogueria.model.Rol;
import com.londres.drogueria.model.Usuario;
import com.londres.drogueria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> listarTodos() {
        Long empresaId = TenantContext.getCurrentTenant();
        if (empresaId != null) {
            return usuarioRepository.findByEmpresaId(empresaId);
        }
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }


    @Transactional
    public Usuario crear(Long empresaId, String username, String password,
                         String nombres, String apellidos, String email,
                         String telefono, boolean activo, java.util.Set<Rol> roles) {

        if (password == null || password.isBlank()) {
            throw new RuntimeException("La contraseña es obligatoria.");
        }
        if (usuarioRepository.findByEmpresaIdAndUsername(empresaId, username).isPresent()) {
            throw new RuntimeException("El nombre de usuario '" + username + "' ya está en uso.");
        }

        // Crear objeto completamente nuevo — sin contaminación de sesión
        Usuario nuevo = new Usuario();
        nuevo.setEmpresaId(empresaId);
        nuevo.setUsername(username);
        nuevo.setPassword(passwordEncoder.encode(password));
        nuevo.setNombres(nombres);
        nuevo.setApellidos(apellidos);
        nuevo.setEmail(email);
        nuevo.setTelefono(telefono);
        nuevo.setActivo(activo);
        nuevo.setRoles(roles != null ? roles : new java.util.HashSet<>());

        return usuarioRepository.save(nuevo);
    }


    @Transactional
    public Usuario actualizar(Long id, String password, String nombres, String apellidos,
                              String email, String telefono, boolean activo, java.util.Set<Rol> roles) {

        Usuario existing = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        // Actualizar solo los campos editables — empresa_id se preserva del existente
        if (password != null && !password.isBlank()) {
            existing.setPassword(passwordEncoder.encode(password));
        }
        existing.setNombres(nombres);
        existing.setApellidos(apellidos);
        existing.setEmail(email);
        existing.setTelefono(telefono);
        existing.setActivo(activo);
        if (roles != null && !roles.isEmpty()) {
            existing.setRoles(roles);
        }

        return usuarioRepository.save(existing);
    }

    @Transactional
    public void eliminar(Long id) {
        Long empresaId = TenantContext.getCurrentTenant();
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        long adminCount = usuarioRepository.findByEmpresaId(
                empresaId != null ? empresaId : usuario.getEmpresaId()).stream()
                .filter(u -> u.getRoles().contains(Rol.ADMIN) && u.isActivo())
                .count();

        if (usuario.getRoles().contains(Rol.ADMIN) && adminCount <= 1) {
            throw new RuntimeException("No se puede eliminar el único administrador activo.");
        }

        usuarioRepository.deleteById(id);
    }
}
