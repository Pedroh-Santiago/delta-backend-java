package br.com.deltaglobalbank.products.features.lending.updateLendingProduct;

import java.util.UUID;

public record UpdateLendingProductCommand(
    UUID productId,
    UUID tenantId,
    UUID updatedBy,
    UpdateLendingProductRequest request
) {
}
