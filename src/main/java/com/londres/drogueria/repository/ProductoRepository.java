package com.londres.drogueria.repository;

import com.londres.drogueria.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByProveedorId(Long proveedorId);

    List<Producto> findByNombreContainingIgnoreCaseOrLaboratorioContainingIgnoreCaseOrPrincipioActivoContainingIgnoreCase(
            String nombre, String laboratorio, String principioActivo);

    long countByEmpresaId(Long empresaId);

    @Query("SELECT p FROM Producto p WHERE p.empresaId = :empresaId AND p.stock <= p.stockMinimo")
    List<Producto> findLowStockProducts(@Param("empresaId") Long empresaId);

    @Query("SELECT p FROM Producto p WHERE p.empresaId = :empresaId AND p.stock <= p.stockMinimo ORDER BY p.stock ASC LIMIT 10")
    List<Producto> findTop10LowStockProducts(@Param("empresaId") Long empresaId);

    @Query("SELECT p FROM Producto p WHERE p.empresaId = :empresaId AND " +
            "(CAST(:query AS string) IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')) OR LOWER(p.laboratorio) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%'))) AND "
            +
            "(:categoriaId IS NULL OR p.categoria.id = :categoriaId) AND " +
            "(:stockBajo = false OR p.stock <= p.stockMinimo) " +
            "ORDER BY p.nombre ASC")
    List<Producto> findFiltered(@Param("empresaId") Long empresaId, @Param("query") String query,
            @Param("categoriaId") Long categoriaId, @Param("stockBajo") boolean stockBajo);

    @Query("SELECT p.categoria.nombre, COUNT(p) FROM Producto p WHERE p.empresaId = :empresaId GROUP BY p.categoria.nombre")
    List<Object[]> countProductsByCategory(@Param("empresaId") Long empresaId);

    List<Producto> findByCategoriaId(Long categoriaId);

    List<Producto> findByLaboratorioContainingIgnoreCase(String laboratorio);

    @Query("SELECT p FROM Producto p WHERE p.empresaId = :empresaId AND p.fechaVencimiento <= :date AND p.fechaVencimiento >= CURRENT_DATE")
    List<Producto> findExpiringProducts(@Param("empresaId") Long empresaId, @Param("date") java.time.LocalDate date);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    @Query("SELECT p FROM Producto p WHERE " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.laboratorio) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.principioActivo) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "p.stock > 0")
    List<Producto> searchMarketplace(@Param("query") String query);

    @Query("SELECT p FROM Producto p WHERE p.stock > 0")
    List<Producto> findAllMarketplace();
}
