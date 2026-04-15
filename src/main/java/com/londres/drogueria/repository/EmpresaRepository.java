package com.londres.drogueria.repository;

import com.londres.drogueria.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByRuc(String ruc);
    boolean existsByRuc(String ruc);
    
    @org.springframework.data.jpa.repository.Query("SELECT e.departamento, COUNT(e) FROM Empresa e GROUP BY e.departamento")
    java.util.List<Object[]> countCompaniesByDepartment();

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(e) FROM Empresa e WHERE e.activo = true")
    long countActiveCompanies();
}
