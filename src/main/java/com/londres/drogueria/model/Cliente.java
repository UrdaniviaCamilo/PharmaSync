package com.londres.drogueria.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "clientes")
@Getter
@Setter
public class Cliente extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    @Column(length = 8)
    private String dni;
    private String telefono;
    private String email;
    private String direccion;
    private LocalDate fechaNacimiento;
    private boolean activo = true;
}
