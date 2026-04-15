package com.londres.drogueria.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
@Getter
@Setter
public class Notificacion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario; // El destinatario de la notificación

    @Column(nullable = false, length = 500)
    private String mensaje;

    private String url;

    private boolean leida = false;

    private LocalDateTime fecha = LocalDateTime.now();
}
