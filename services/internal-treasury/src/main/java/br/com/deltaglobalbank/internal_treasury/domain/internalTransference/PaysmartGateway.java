package br.com.deltaglobalbank.internal_treasury.domain.internalTransference;

public interface PaysmartGateway {
    long transfer(
        long payerAccountNumber,
        long recipientAccountNumber,
        int amount,
        String description
    );
}
