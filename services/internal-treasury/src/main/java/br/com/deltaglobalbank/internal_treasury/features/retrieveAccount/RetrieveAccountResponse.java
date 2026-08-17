package br.com.deltaglobalbank.internal_treasury.features.retrieveAccount;

public record RetrieveAccountResponse(
    long accountId,
    long accountNumber
) {
}
