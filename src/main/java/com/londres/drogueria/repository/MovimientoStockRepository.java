package com.londres.drogueria.repository;

import com.londres.drogueria.model.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {

    List<MovimientoStock> findByEmpresaIdOrderByFechaDesc(Long empresaId);

    List<MovimientoStock> findByProductoIdOrderByFechaDesc(Long productoId);

    @Query("SELECT m FROM MovimientoStock m WHERE m.empresaId = :empresaId AND m.producto.id = :productoId ORDER BY m.fecha DESC")
    List<MovimientoStock> findKardex(@Param("empresaId") Long empresaId, @Param("productoId") Long productoId);

    List<MovimientoStock> findTop10ByEmpresaIdOrderByFechaDesc(Long empresaId);

    @Query("SELECT m.usuario.nombres, COUNT(m) FROM MovimientoStock m WHERE m.empresaId = :empresaId AND m.fecha >= :start GROUP BY m.usuario.nombres")
    List<Object[]> findStockMovementPerformanceByEmployee(@Param("empresaId") Long empresaId, @Param("start") java.time.LocalDateTime start);

    @Query("SELECT CAST(m.fecha AS date), COUNT(m) FROM MovimientoStock m WHERE m.usuario.id = :usuarioId AND m.fecha >= :start GROUP BY CAST(m.fecha AS date) ORDER BY CAST(m.fecha AS date) ASC")
    List<Object[]> findPersonalStockMovementsByDayAfter(@Param("usuarioId") Long usuarioId, @Param("start") java.time.LocalDateTime start);
}
