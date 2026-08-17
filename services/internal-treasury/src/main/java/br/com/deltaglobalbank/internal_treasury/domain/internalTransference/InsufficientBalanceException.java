package br.com.deltaglobalbank.internal_treasury.domain.internalTransference;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException() {
        super("Insufficient Balance");
    }
}
