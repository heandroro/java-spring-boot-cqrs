package com.company.orders.enums;

import java.util.Arrays;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED;

    public String getValue() {
        return name().toLowerCase();
    }

    public static OrderStatus fromValue(String value) {
        return Arrays.stream(values())
            .filter(status -> status.getValue().equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid status: " + value));
    }
}
