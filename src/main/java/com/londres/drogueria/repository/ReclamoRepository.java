package com.londres.drogueria.repository;

import com.londres.drogueria.model.Reclamo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReclamoRepository extends JpaRepository<Reclamo, Long> {
    long countByEstado(com.londres.drogueria.model.ReclamoEstado estado);
}
