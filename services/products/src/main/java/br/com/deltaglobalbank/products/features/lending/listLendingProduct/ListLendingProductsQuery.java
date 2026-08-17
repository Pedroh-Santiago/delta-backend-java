package br.com.deltaglobalbank.products.features.lending.listLendingProduct;

import java.util.UUID;

public record ListLendingProductsQuery(
    UUID tenantId,
    int page,
    Integer size,
    String agreementName,
    Boolean active
) {
}
