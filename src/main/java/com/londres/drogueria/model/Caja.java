package com.londres.drogueria.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cajas")
@Getter
@Setter
public class Caja extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaApertura = LocalDateTime.now();
    private LocalDateTime fechaCierre;

    @Column(nullable = false)
    private BigDecimal montoInicial = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal montoFinal = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal totalVentas = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal totalGastos = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean abierta = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
