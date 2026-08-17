package br.com.deltaglobalbank.internal_treasury.infrastructure.web.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaysmartTransferResponse(
    String message,
    String authenticationCode,
    String freeField,
    long transactionId,
    String dateTimeTransfer,
    int amount,
    String recipientAccountHolder,
    long accountNumber,
    int sendersAccountNumber,
    String sendersName
) {
}
