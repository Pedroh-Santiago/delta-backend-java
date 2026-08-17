package br.com.deltaglobalbank.customers.domain.customer;

public final class CustomerNotFound extends CustomerDomainException {
    public CustomerNotFound() {
        super("customer_not_found");
    }
}
