package br.com.deltaglobalbank.internal_treasury.domain.account;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException() {
        super("Account not found");
    }
}
