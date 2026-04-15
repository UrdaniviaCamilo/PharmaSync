package com.londres.drogueria.repository;

import com.londres.drogueria.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    java.util.List<Categoria> findByEmpresaId(Long empresaId);
    java.util.List<Categoria> findByNombreContainingIgnoreCase(String nombre);
    long countByEmpresaId(Long empresaId);
}
