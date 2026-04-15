package com.londres.drogueria.repository;

import com.londres.drogueria.model.Configuracion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConfiguracionRepository extends JpaRepository<Configuracion, Long> {
    Optional<Configuracion> findByEmpresaId(Long empresaId);
}
