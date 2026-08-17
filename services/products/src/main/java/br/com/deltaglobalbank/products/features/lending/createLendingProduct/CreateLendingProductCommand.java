package br.com.deltaglobalbank.products.features.lending.createLendingProduct;

import java.util.UUID;

public record CreateLendingProductCommand(
    UUID tenantId,
    UUID createdBy,
    CreateLendingProductRequest request
) {
}
