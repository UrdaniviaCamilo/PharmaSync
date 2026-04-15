package com.londres.drogueria.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "proveedores")
@Getter
@Setter
public class Proveedor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String ruc;
    private String contacto;
    private String telefono;
    private String email;
    private String sitioWeb;
    private String direccion;

    @Column(columnDefinition = "TEXT")
    private String notas;

    private boolean activo = true;

    @OneToMany(mappedBy = "proveedor")
    private java.util.List<Producto> productos = new java.util.ArrayList<>();
}
