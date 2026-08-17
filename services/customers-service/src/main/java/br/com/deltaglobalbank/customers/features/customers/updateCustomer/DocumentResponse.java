package br.com.deltaglobalbank.customers.features.customers.updateCustomer;

import java.time.LocalDate;
import java.util.UUID;

public record DocumentResponse(
    UUID id,
    String type,
    String number,
    String issuer,
    String issuerState,
    LocalDate issuedAt
) {
}
