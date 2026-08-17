package br.com.deltaglobalbank.identity.features.apiClients.listClients;

import java.util.List;

public record ListClientsResponse(
    List<ItemsResponse> items,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
}
