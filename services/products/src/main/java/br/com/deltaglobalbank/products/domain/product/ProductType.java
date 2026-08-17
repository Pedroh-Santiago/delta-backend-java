package br.com.deltaglobalbank.products.domain.product;

import java.util.Arrays;

public enum ProductType {
    LENDING;

    public String toDatabaseValue() {
        return name();
    }

    public static ProductType fromDatabaseValue(String value) {
        return Arrays.stream(values())
            .filter(type -> type.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("invalid_product_type: " + value));
    }
}
