package br.com.deltaglobalbank.customers.domain.customer;

public final class DuplicatePrimaryAccountForPurpose extends CustomerDomainException {
    public DuplicatePrimaryAccountForPurpose() {
        super("duplicate_primary_account_for_purpose");
    }
}
