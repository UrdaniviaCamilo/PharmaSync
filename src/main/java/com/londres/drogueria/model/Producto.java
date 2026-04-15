package com.londres.drogueria.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "productos")
@Getter
@Setter
public class Producto extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;
    private String laboratorio;
    private String principioActivo;
    private String presentacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    @Column(nullable = false)
    private BigDecimal precioCompra = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal precioVenta = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(nullable = false)
    private Integer stockMinimo = 5;

    private LocalDate fechaVencimiento;
    private boolean requiereReceta = false;
    private String imagenPath;
    private boolean activo = true;

    private String codigoEan13;
    private String codigoQr;
    private String codigo128;
    
    @Column(nullable = false)
    private boolean visibleMarketplace = true;
}
