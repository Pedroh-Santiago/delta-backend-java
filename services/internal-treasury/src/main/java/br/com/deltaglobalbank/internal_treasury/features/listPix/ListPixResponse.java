package br.com.deltaglobalbank.internal_treasury.features.listPix;

import java.util.List;

public record ListPixResponse(
    List<MakePixItem> content,
    int page,
    int pageSize,
    long totalItems,
    int totalPages
) {
}
