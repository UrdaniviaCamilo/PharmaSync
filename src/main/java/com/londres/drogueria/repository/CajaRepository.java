package com.londres.drogueria.repository;

import com.londres.drogueria.model.Caja;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CajaRepository extends JpaRepository<Caja, Long> {
    Optional<Caja> findByEmpresaIdAndAbiertaTrue(Long empresaId);
    Optional<Caja> findFirstByEmpresaIdAndAbiertaTrue(Long empresaId);
    java.util.List<Caja> findByEmpresaId(Long empresaId);
}
