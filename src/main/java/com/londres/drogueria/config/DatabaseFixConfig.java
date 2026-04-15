package com.londres.drogueria.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.logging.Logger;

@Configuration
public class DatabaseFixConfig {

    private static final Logger logger = Logger.getLogger(DatabaseFixConfig.class.getName());

    @Bean
    public CommandLineRunner dropEmpresaIdConstraints(JdbcTemplate jdbcTemplate) {
        return args -> {
            String[] tables = {
                    "usuarios", "ventas", "proveedores", "productos",
                    "movimientos_stock", "movimientos_caja", "detalles_venta",
                    "configuraciones", "clientes", "categorias", "cajas"
            };

            for (String table : tables) {
                try {
                    logger.info("Asegurando que " + table + ".empresa_id sea nullable...");
                    jdbcTemplate.execute("ALTER TABLE " + table + " ALTER COLUMN empresa_id DROP NOT NULL;");
                } catch (Exception e) {
                    logger.warning("No se pudo actualizar " + table + ": " + e.getMessage());
                }
            }


            try {
                logger.info("Verificando columnas faltantes en 'ventas'...");

                jdbcTemplate.execute("ALTER TABLE ventas ADD COLUMN IF NOT EXISTS estado VARCHAR(255) DEFAULT 'NUEVA';");

                jdbcTemplate.execute("UPDATE ventas SET estado = 'NUEVA' WHERE estado IS NULL;");

                jdbcTemplate.execute("ALTER TABLE ventas ALTER COLUMN estado SET NOT NULL;");
                

                jdbcTemplate.execute("ALTER TABLE ventas ADD COLUMN IF NOT EXISTS receta_path VARCHAR(255);");
                
                logger.info("¡Columnas de 'ventas' actualizadas!");
            } catch (Exception e) {
                logger.warning("Error al actualizar columnas de 'ventas': " + e.getMessage());
            }


            try {
                logger.info("Eliminando restricción Check en usuario_roles...");
                jdbcTemplate.execute("ALTER TABLE usuario_roles DROP CONSTRAINT IF EXISTS usuario_roles_roles_check;");
                logger.info("¡Restricción de roles eliminada!");
            } catch (Exception e) {
                logger.warning("Error al eliminar restricción de roles: " + e.getMessage());
            }
        };
    }
}
