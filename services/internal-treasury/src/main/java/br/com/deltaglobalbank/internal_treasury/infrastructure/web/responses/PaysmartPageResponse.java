package br.com.deltaglobalbank.internal_treasury.infrastructure.web.responses;

import java.util.List;

public record PaysmartPageResponse<T>(
    List<T> data,
    int page,
    int pageSize,
    int totalItems,
    int totalPages,
    boolean hasNextPage
) {
}
