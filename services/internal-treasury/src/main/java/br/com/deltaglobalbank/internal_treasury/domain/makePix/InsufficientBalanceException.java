package br.com.deltaglobalbank.internal_treasury.domain.makePix;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException() {
        super("Insufficient balance");
    }
}
