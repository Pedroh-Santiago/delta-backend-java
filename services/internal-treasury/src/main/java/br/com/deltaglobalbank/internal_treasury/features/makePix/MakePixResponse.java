package br.com.deltaglobalbank.internal_treasury.features.makePix;

import java.time.Instant;
import java.util.UUID;

public record MakePixResponse(
    UUID id,
    long accountId,
    long operationAmount,
    Instant createdAt
) {
}
