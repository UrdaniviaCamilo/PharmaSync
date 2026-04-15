package com.londres.drogueria.repository;

import com.londres.drogueria.model.MovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {
    java.util.List<MovimientoCaja> findByCajaIdOrderByFechaDesc(Long cajaId);
    java.util.List<MovimientoCaja> findByEmpresaId(Long empresaId);
    java.util.List<MovimientoCaja> findByEmpresaIdAndFechaBetweenOrderByFechaDesc(Long empresaId, java.time.LocalDateTime start, java.time.LocalDateTime end);
}
