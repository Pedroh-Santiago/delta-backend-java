package br.com.deltaglobalbank.internal_treasury.features.listInternalTrasference;

import java.time.Instant;
import java.util.UUID;

public record InternalTransferenceItem(
    UUID id,
    long payerId,
    long accountNumber,
    int amount,
    String status,
    Instant requestedAt
) {
}
