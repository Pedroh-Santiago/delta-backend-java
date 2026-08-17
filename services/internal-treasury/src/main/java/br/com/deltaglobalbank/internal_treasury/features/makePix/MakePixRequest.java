package br.com.deltaglobalbank.internal_treasury.features.makePix;

public record MakePixRequest(
    long accountId,
    String recipientInstitutionCode,
    String recipientBranchCode,
    String recipientAccountNumber,
    String recipientAccountType,
    String recipientName,
    long operationAmount
) {
}
