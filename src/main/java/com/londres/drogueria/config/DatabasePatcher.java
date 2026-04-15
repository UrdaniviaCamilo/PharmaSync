package com.londres.drogueria.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class DatabasePatcher {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void patchDatabase() {
        try {
            jdbcTemplate.execute("ALTER TABLE empresas ALTER COLUMN logo_path TYPE TEXT;");
            System.out.println("[DatabasePatcher] patched empresas.logo_path to TEXT");
        } catch (Exception e) {
            System.out.println("[DatabasePatcher] empresas.logo_path might already be TEXT: " + e.getMessage());
        }
        
        try {
            jdbcTemplate.execute("ALTER TABLE usuarios ALTER COLUMN foto_perfil TYPE TEXT;");
            System.out.println("[DatabasePatcher] patched usuarios.foto_perfil to TEXT");
        } catch (Exception e) {
            System.out.println("[DatabasePatcher] usuarios.foto_perfil might already be TEXT: " + e.getMessage());
        }
    }
}
