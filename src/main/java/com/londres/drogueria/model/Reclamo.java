package com.londres.drogueria.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "reclamaciones")
@Getter
@Setter
public class Reclamo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(nullable = false)
    private String nombreCompleto;

    @Column(nullable = false)
    private String correo;

    private String telefono;

    @Column(nullable = false)
    private String tipoDocumento;

    @Column(nullable = false)
    private String numeroDocumento;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String detalleReclamo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReclamoEstado estado = ReclamoEstado.PENDIENTE;

    @Column(columnDefinition = "TEXT")
    private String respuestaAdmin;
}
