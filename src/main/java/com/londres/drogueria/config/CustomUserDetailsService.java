package com.londres.drogueria.config;

import com.londres.drogueria.model.Usuario;
import com.londres.drogueria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private com.londres.drogueria.repository.EmpresaRepository empresaRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        

        if (usuario.getEmpresaId() != null) {
            com.londres.drogueria.model.Empresa empresa = empresaRepository.findById(usuario.getEmpresaId()).orElse(null);
            if (empresa != null && !empresa.isActivo()) {
                throw new org.springframework.security.authentication.DisabledException(
                    "Su negocio (" + empresa.getNombreComercial() + ") ha sido suspendido. Por favor, contacte con soporte.");
            }
        }
        
        return new CustomUserDetails(usuario);
    }
}
