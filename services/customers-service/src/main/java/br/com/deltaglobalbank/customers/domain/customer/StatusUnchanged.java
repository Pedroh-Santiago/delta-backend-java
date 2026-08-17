package br.com.deltaglobalbank.customers.domain.customer;

public final class StatusUnchanged extends CustomerDomainException {
    public StatusUnchanged() {
        super("status_unchanged");
    }
}
