package br.com.deltaglobalbank.internal_treasury.infrastructure.web.responses;

public record PaysmartAccountResponse(
    long accountId,
    String personalName,
    String email,
    String personalDocument,
    Long personId,
    Integer productId,
    long accountNumber,
    Integer status,
    String statusDescription,
    Long mainAccountId,
    String arrangementType,
    String accountName,
    Object transactionInfo
) {
}
