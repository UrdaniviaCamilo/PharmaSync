package com.londres.drogueria.repository;

import com.londres.drogueria.model.Venta;
import com.londres.drogueria.model.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    Optional<Venta> findByNumeroVenta(String numeroVenta);
    List<Venta> findByFechaBetween(LocalDateTime start, LocalDateTime end);
    List<Venta> findByClienteId(Long clienteId);
    List<Venta> findByUsuarioId(Long usuarioId);
    List<Venta> findByMarketplaceUsuarioIdOrderByFechaDesc(Long marketplaceUsuarioId);
    
    long countByEmpresaId(Long empresaId);
    
    @Query("SELECT v FROM Venta v LEFT JOIN v.cliente c WHERE v.empresaId = :empresaId AND " +
           "(CAST(:numero AS string) IS NULL OR LOWER(v.numeroVenta) LIKE LOWER(CONCAT('%', CAST(:numero AS string), '%'))) AND " +
           "(CAST(:cliente AS string) IS NULL OR LOWER(c.nombres) LIKE LOWER(CONCAT('%', CAST(:cliente AS string), '%')) OR LOWER(c.apellidos) LIKE LOWER(CONCAT('%', CAST(:cliente AS string), '%')) OR LOWER(v.observaciones) LIKE LOWER(CONCAT('%', CAST(:cliente AS string), '%'))) AND " +
           "(:metodo IS NULL OR v.metodoPago = :metodo) " +
           "ORDER BY v.fecha DESC")
    List<Venta> findFiltered(@Param("empresaId") Long empresaId, @Param("numero") String numero, @Param("cliente") String cliente, @Param("metodo") MetodoPago metodo);
    
    @Query("SELECT COUNT(v) FROM Venta v WHERE v.empresaId = :empresaId AND v.recetaPath IS NOT NULL AND v.fecha >= :start")
    long countVentasConRecetaAfter(@Param("empresaId") Long empresaId, @Param("start") LocalDateTime start);
    
    @Query("SELECT COUNT(v) FROM Venta v WHERE v.empresaId = :empresaId AND v.fecha >= :start")
    long countByFechaAfter(@Param("empresaId") Long empresaId, @Param("start") LocalDateTime start);
    
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.empresaId = :empresaId AND v.fecha >= :start")
    java.math.BigDecimal sumTotalByFechaAfter(@Param("empresaId") Long empresaId, @Param("start") LocalDateTime start);

    @Query("SELECT v FROM Venta v WHERE v.empresaId = :empresaId ORDER BY v.fecha DESC LIMIT 10")
    List<Venta> findTop10ByOrderByFechaDesc(@Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.usuario.id = :usuarioId AND v.fecha >= :start")
    java.math.BigDecimal sumTotalByUsuarioAndFechaAfter(@Param("usuarioId") Long usuarioId, @Param("start") LocalDateTime start);

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.usuario.id = :usuarioId AND v.fecha >= :start")
    long countByUsuarioAndFechaAfter(@Param("usuarioId") Long usuarioId, @Param("start") LocalDateTime start);

    @Query("SELECT v.metodoPago, COUNT(v), SUM(v.total) FROM Venta v WHERE v.empresaId = :empresaId GROUP BY v.metodoPago")
    List<Object[]> findSalesByMetodoPago(@Param("empresaId") Long empresaId);

    @Query("SELECT CAST(v.fecha AS date), COUNT(v), SUM(v.total) FROM Venta v WHERE v.empresaId = :empresaId AND v.fecha >= :start GROUP BY CAST(v.fecha AS date) ORDER BY CAST(v.fecha AS date) ASC")
    List<Object[]> findSalesByDayAfter(@Param("empresaId") Long empresaId, @Param("start") LocalDateTime start);

    @Query("SELECT d.producto.nombre, SUM(d.cantidad) FROM DetalleVenta d WHERE d.empresaId = :empresaId GROUP BY d.producto.nombre ORDER BY SUM(d.cantidad) DESC LIMIT 5")
    List<Object[]> findTop5MostSoldProducts(@Param("empresaId") Long empresaId);

    @Query("SELECT v.usuario.nombres, COUNT(v), SUM(v.total) FROM Venta v WHERE v.empresaId = :empresaId AND v.fecha >= :start GROUP BY v.usuario.nombres")
    List<Object[]> findSalesPerformanceByEmployee(@Param("empresaId") Long empresaId, @Param("start") LocalDateTime start);

    @Query("SELECT CAST(v.fecha AS date), COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fecha >= :start GROUP BY CAST(v.fecha AS date) ORDER BY CAST(v.fecha AS date) ASC")
    List<Object[]> findGlobalSalesTrend(@Param("start") LocalDateTime start);

    @Query("SELECT (SELECT e.nombreComercial FROM Empresa e WHERE e.id = v.empresaId), COALESCE(SUM(v.total), 0) FROM Venta v GROUP BY v.empresaId ORDER BY SUM(v.total) DESC")
    List<Object[]> findRevenueByBusinessRanking();

    @Query("SELECT CAST(v.fecha AS date), COUNT(v), SUM(v.total) FROM Venta v WHERE v.usuario.id = :usuarioId AND v.fecha >= :start GROUP BY CAST(v.fecha AS date) ORDER BY CAST(v.fecha AS date) ASC")
    List<Object[]> findPersonalSalesByDayAfter(@Param("usuarioId") Long usuarioId, @Param("start") LocalDateTime start);

    @Query("SELECT EXTRACT(HOUR FROM v.fecha), COUNT(v) FROM Venta v WHERE v.empresaId = :empresaId GROUP BY EXTRACT(HOUR FROM v.fecha) ORDER BY EXTRACT(HOUR FROM v.fecha) ASC")
    List<Object[]> findSalesByHour(@Param("empresaId") Long empresaId);

    @Query("SELECT p.categoria.nombre, SUM(d.subtotal) FROM DetalleVenta d JOIN d.producto p WHERE d.venta.empresaId = :empresaId GROUP BY p.categoria.nombre ORDER BY SUM(d.subtotal) DESC")
    List<Object[]> findRevenueByCategory(@Param("empresaId") Long empresaId);
}
