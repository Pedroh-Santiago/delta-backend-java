package br.com.deltaglobalbank.products.domain.product;

public record DisplayName(String value) {
    public DisplayName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("display_name_required");
        }
    }
}
