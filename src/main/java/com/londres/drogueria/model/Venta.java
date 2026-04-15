package com.londres.drogueria.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
@Getter
@Setter
public class Venta extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_venta", unique = true, nullable = false)
    private String numeroVenta;

    private LocalDateTime fecha = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario; // Vendedor

    /** Cliente (cuenta web) que realizó el pedido en marketplace */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marketplace_usuario_id")
    private Usuario marketplaceUsuario;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles = new ArrayList<>();

    @Column(nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPago metodoPago; // EFECTIVO, TARJETA_CREDITO, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VentaEstado estado = VentaEstado.NUEVA;

    private String recetaPath;

    @Column(name = "receta_validada_manual")
    private boolean recetaValidadaManual = false;

    private String observaciones;

    public void addDetalle(DetalleVenta detalle) {
        detalles.add(detalle);
        detalle.setVenta(this);
    }
}
