package com.londres.drogueria.repository;

import com.londres.drogueria.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    List<Proveedor> findByNombreContainingIgnoreCase(String nombre);
    List<Proveedor> findByEmpresaId(Long empresaId);
    boolean existsByRuc(String ruc);
    long countByEmpresaId(Long empresaId);
}
