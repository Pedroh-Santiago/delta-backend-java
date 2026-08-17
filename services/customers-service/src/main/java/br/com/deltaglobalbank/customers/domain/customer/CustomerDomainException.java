package br.com.deltaglobalbank.customers.domain.customer;

public abstract sealed class CustomerDomainException extends RuntimeException
    permits
        CustomerNotFound,
        BankAccountNotFound,
        CpfAlreadyExists,
        DuplicatePrimaryAccountForPurpose,
        SubaggregateDoesNotBelongToCustomer,
        DuplicateBankAccount,
        DuplicatePersonalDocument,
        StatusUnchanged,
        InvalidStatusFilter {

    protected CustomerDomainException(String message) {
        super(message);
    }
}
