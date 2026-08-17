package br.com.deltaglobalbank.customers.domain.customer;

public final class BankAccountNotFound extends CustomerDomainException {
    public BankAccountNotFound() {
        super("bank_account_not_found");
    }
}
