package com.seguidores.mvc.models.entity;

public enum StatusPayment {
    EN_PROCESO("EN_PROCESO"),
    PAGO_REALIZADO("PAGO_REALIZADO"),
    PAGO_RECHAZADO("PAGO_RECHAZADO");

    private final String value;

    StatusPayment(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
