package com.londres.drogueria.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "empresas")
@Getter
@Setter
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 11)
    private String ruc;

    @Column(nullable = false)
    private String nombreComercial;

    private String razonSocial;
    private String departamento;
    private String provincia;
    private String distrito;
    private String direccion;
    private String telefono;
    private String email;
    @Column(columnDefinition = "TEXT")
    private String logoPath;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = false)
    private boolean registrada = true;

    private LocalDateTime fechaRegistro = LocalDateTime.now();
}
