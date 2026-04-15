package com.londres.drogueria.model;

import lombok.Getter;

@Getter
public enum MetodoPago {
    EFECTIVO("Efectivo"),
    TARJETA_CREDITO("Tarjeta Crédito"),
    TARJETA_DEBITO("Tarjeta Débito"),
    YAPE("Yape"),
    PLIN("Plin"),
    TRANSFERENCIA("Transferencia");

    private final String displayName;

    MetodoPago(String displayName) {
        this.displayName = displayName;
    }
}
