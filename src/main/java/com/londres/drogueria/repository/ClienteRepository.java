package com.londres.drogueria.repository;

import com.londres.drogueria.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByDni(String dni);
    List<Cliente> findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrDniContaining(String nombres, String apellidos, String dni);
    boolean existsByDni(String dni);
    java.util.List<Cliente> findByEmpresaId(Long empresaId);

    @Query("SELECT c FROM Cliente c WHERE c.empresaId = :empresaId AND " +
           "(CAST(:query AS string) IS NULL OR LOWER(c.nombres) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')) OR LOWER(c.apellidos) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')) OR c.dni LIKE CONCAT('%', CAST(:query AS string), '%')) " +
           "ORDER BY c.nombres ASC")
    java.util.List<Cliente> findFiltered(@Param("empresaId") Long empresaId, @Param("query") String query);

    long countByEmpresaId(Long empresaId);
    long countByEmpresaIdAndActivoTrue(Long empresaId);
    long countByEmpresaIdAndActivoFalse(Long empresaId);

    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.empresaId = :empresaId AND c.fechaCreacion >= :start")
    long countByFechaCreacionAfter(@Param("empresaId") Long empresaId, @Param("start") java.time.LocalDateTime start);
}
