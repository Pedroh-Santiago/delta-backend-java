package br.com.deltaglobalbank.customers.domain.customer;

public final class InvalidStatusFilter extends CustomerDomainException {
    public InvalidStatusFilter() {
        super("invalid_status_filter");
    }
}
