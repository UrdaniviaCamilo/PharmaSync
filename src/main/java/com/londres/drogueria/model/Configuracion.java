package com.londres.drogueria.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "configuraciones")
@Getter
@Setter
public class Configuracion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String moneda = "S/";

    @Column(nullable = false)
    private String impuestoNombre = "IGV";

    @Column(nullable = false)
    private BigDecimal impuestoPorcentaje = new BigDecimal("18");

    private String nombreFarmacia = "PharmaSync";
    @Column(columnDefinition = "TEXT")
    private String logoUrl;
    private String eslogan;
    private String direccion;
    private String telefono;
    private String email;
    private String sitioWeb;

    private String tema = "light"; // light, dark, blue, green, etc.

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", insertable = false, updatable = false)
    private Empresa empresa;
}
