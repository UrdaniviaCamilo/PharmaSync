package com.londres.drogueria.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@MappedSuperclass
@Getter
@Setter
@FilterDef(name = "empresaFilter", parameters = @ParamDef(name = "empresaId", type = Long.class))
@Filter(name = "empresaFilter", condition = "empresa_id = :empresaId")
public abstract class BaseEntity {

    @Column(name = "empresa_id", nullable = true)
    private Long empresaId;

    @Column(name = "fecha_creacion", updatable = false)
    private java.time.LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private java.time.LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        if (this.empresaId == null) {
            this.empresaId = com.londres.drogueria.config.TenantContext.getCurrentTenant();
        }
        this.fechaCreacion = java.time.LocalDateTime.now();
        this.fechaActualizacion = java.time.LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = java.time.LocalDateTime.now();
    }
}
