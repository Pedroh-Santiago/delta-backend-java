package br.com.deltaglobalbank.internal_treasury.features.orderInternalTransference;

import java.time.Instant;
import java.util.UUID;

public record OrderInternalTransferenceResponse(
    UUID id,
    long payerId,
    long accountNumber,
    int amount,
    Instant requestedAt
) {
}
