package br.com.deltaglobalbank.products.features.lending.listLendingProduct;

import java.util.List;

public record ListLendingProductsResponse(
    List<ListedLendingProduct> items,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
}
