package com.londres.drogueria.service;

import com.londres.drogueria.model.Empresa;
import com.londres.drogueria.model.SeguidorEmpresa;
import com.londres.drogueria.model.Usuario;
import com.londres.drogueria.repository.EmpresaRepository;
import com.londres.drogueria.repository.SeguidorEmpresaRepository;
import com.londres.drogueria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SeguidorEmpresaService {

    @Autowired
    private SeguidorEmpresaRepository seguidorEmpresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Transactional
    public boolean toggleSeguir(Long usuarioId, Long empresaId) {
        Optional<SeguidorEmpresa> existente = seguidorEmpresaRepository.findByUsuarioIdAndEmpresaId(usuarioId, empresaId);
        if (existente.isPresent()) {
            seguidorEmpresaRepository.delete(existente.get());
            return false; // unfollowed
        } else {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

            SeguidorEmpresa seguidor = new SeguidorEmpresa();
            seguidor.setUsuario(usuario);
            seguidor.setEmpresa(empresa);
            seguidorEmpresaRepository.save(seguidor);
            return true; // followed
        }
    }

    public boolean isSiguiendo(Long usuarioId, Long empresaId) {
        return seguidorEmpresaRepository.findByUsuarioIdAndEmpresaId(usuarioId, empresaId).isPresent();
    }
}
