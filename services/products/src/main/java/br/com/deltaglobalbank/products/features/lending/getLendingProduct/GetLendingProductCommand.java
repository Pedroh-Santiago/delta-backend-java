package br.com.deltaglobalbank.products.features.lending.getLendingProduct;

import java.util.UUID;

public record GetLendingProductCommand(
    UUID productId,
    UUID tenantId
) {
}
