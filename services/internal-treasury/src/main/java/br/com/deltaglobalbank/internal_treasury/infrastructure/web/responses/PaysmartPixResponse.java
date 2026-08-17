package br.com.deltaglobalbank.internal_treasury.infrastructure.web.responses;

public record PaysmartPixResponse(
    String message,
    long idTransaction,
    long scheduledTransactionId,
    String authenticationCode,
    ArbiPixTransferError arbiPixTransferError
) {
}
