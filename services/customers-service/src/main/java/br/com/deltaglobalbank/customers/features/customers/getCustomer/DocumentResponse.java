package br.com.deltaglobalbank.customers.features.customers.getCustomer;

import java.time.LocalDate;
import java.util.UUID;

public record DocumentResponse(
    UUID id,
    String type,
    String number,
    String issuer,
    String issuerState,
    LocalDate issuedAt,
    LocalDate expiresAt
) {
}
