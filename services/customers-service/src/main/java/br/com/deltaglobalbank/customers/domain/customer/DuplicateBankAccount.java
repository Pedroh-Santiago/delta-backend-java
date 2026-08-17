package br.com.deltaglobalbank.customers.domain.customer;

public final class DuplicateBankAccount extends CustomerDomainException {
    public DuplicateBankAccount() {
        super("duplicate_bank_account");
    }
}
