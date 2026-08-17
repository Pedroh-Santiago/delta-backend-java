package br.com.deltaglobalbank.customers.features.customers.listCustomer;

import java.util.List;

public record ListCustomerResponse(
    List<ListedCustomer> items,
    int page,
    Integer size,
    long totalElements,
    int totalPages
) {
}
