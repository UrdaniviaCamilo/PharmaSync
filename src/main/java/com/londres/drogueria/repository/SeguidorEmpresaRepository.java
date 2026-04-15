package com.londres.drogueria.repository;

import com.londres.drogueria.model.SeguidorEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface SeguidorEmpresaRepository extends JpaRepository<SeguidorEmpresa, Long> {
    Optional<SeguidorEmpresa> findByUsuarioIdAndEmpresaId(Long usuarioId, Long empresaId);
    List<SeguidorEmpresa> findByEmpresaId(Long empresaId);
    List<SeguidorEmpresa> findByUsuarioId(Long usuarioId);
}
