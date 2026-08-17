package br.com.deltaglobalbank.internal_treasury.features.listInternalTrasference;

import java.util.List;

public record ListInternalTransferenceResponse(
    List<InternalTransferenceItem> content,
    int page,
    int pageSize,
    long totalItems,
    int totalPages
) {
}
