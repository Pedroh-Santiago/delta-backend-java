package br.com.deltaglobalbank.internal_treasury.features.listPix;

import java.time.Instant;
import java.util.UUID;

public record MakePixItem(
    UUID id,
    long accountId,
    String recipientName,
    String recipientAccountNumber,
    long operationAmount,
    String status,
    Instant createdAt
) {
}
