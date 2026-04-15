package com.londres.drogueria.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_stock")
@Getter
@Setter
public class MovimientoStock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private String tipo; // VENTA, INGRESO, AJUSTE, DEVOLUCION

    @Column(length = 500)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    public MovimientoStock() {
        this.fecha = LocalDateTime.now();
    }

    public MovimientoStock(Producto producto, Integer cantidad, String tipo, String observaciones, Usuario usuario) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.tipo = tipo;
        this.observaciones = observaciones;
        this.usuario = usuario;
        this.fecha = LocalDateTime.now();
        if (usuario != null) {
            this.setEmpresaId(usuario.getEmpresaId());
        }
    }
}
