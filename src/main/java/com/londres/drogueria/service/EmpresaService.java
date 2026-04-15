package com.londres.drogueria.service;

import com.londres.drogueria.model.*;
import com.londres.drogueria.repository.ConfiguracionRepository;
import com.londres.drogueria.repository.EmpresaRepository;
import com.londres.drogueria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;

@Service
public class EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ConfiguracionRepository configuracionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public Empresa registrarEmpresa(Empresa empresa, Usuario admin) {
        if (empresaRepository.existsByRuc(empresa.getRuc())) {
            throw new RuntimeException("El RUC " + empresa.getRuc() + " ya está registrado.");
        }

        // 1. Create Empresa
        Empresa savedEmpresa = empresaRepository.save(empresa);

        // 2. Create Default Configuration
        Configuracion config = new Configuracion();
        config.setEmpresaId(savedEmpresa.getId());
        configuracionRepository.save(config);

        // 3. Create Admin User
        admin.setEmpresaId(savedEmpresa.getId());
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.getRoles().add(Rol.ADMIN);
        usuarioRepository.save(admin);

        return savedEmpresa;
    }
}
