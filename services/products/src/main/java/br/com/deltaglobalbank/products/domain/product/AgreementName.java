package br.com.deltaglobalbank.products.domain.product;

public record AgreementName(String value) {
    public AgreementName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("agreement_name_required");
        }
    }
}
