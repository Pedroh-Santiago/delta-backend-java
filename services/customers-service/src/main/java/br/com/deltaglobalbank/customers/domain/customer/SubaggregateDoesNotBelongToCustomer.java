package br.com.deltaglobalbank.customers.domain.customer;

public final class SubaggregateDoesNotBelongToCustomer extends CustomerDomainException {
    public SubaggregateDoesNotBelongToCustomer() {
        super("subaggregate_does_not_belong_to_customer");
    }
}
