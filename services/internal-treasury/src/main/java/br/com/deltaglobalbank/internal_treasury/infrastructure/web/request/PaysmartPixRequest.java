package br.com.deltaglobalbank.internal_treasury.infrastructure.web.request;

import java.math.BigDecimal;

public record PaysmartPixRequest(
    long accountId,
    String recipientInstitutionCode,
    String recipientBranchCode,
    String recipientAccountNumber,
    String recipientAccountType,
    String recipientName,
    BigDecimal operationAmount
) {
}
