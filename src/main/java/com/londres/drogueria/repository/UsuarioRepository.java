package com.londres.drogueria.repository;

import com.londres.drogueria.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmpresaIdAndUsername(Long empresaId, String username);
    boolean existsByUsername(String username);
    long countByEmpresaId(Long empresaId);
    long countByEmpresaIdAndActivoTrue(Long empresaId);
    java.util.List<Usuario> findByEmpresaId(Long empresaId);
    Optional<Usuario> findByEmail(String email);
    
    @org.springframework.data.jpa.repository.Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r = :rol")
    java.util.List<Usuario> findByRole(com.londres.drogueria.model.Rol rol);
}
