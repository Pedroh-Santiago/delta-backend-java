package br.com.deltaglobalbank.products.features.lending.deleteLendingProduct;

import java.util.UUID;

public record DeleteLendingProductCommand(
    UUID productId,
    UUID tenantId,
    UUID deletedBy
) {
}
