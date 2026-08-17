package br.com.deltaglobalbank.customers.domain.customer;

public final class DuplicatePersonalDocument extends CustomerDomainException {
    public DuplicatePersonalDocument() {
        super("duplicate_document");
    }
}
