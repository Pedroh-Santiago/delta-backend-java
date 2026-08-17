package br.com.deltaglobalbank.customers.domain.customer;

public final class CpfAlreadyExists extends CustomerDomainException {
    public CpfAlreadyExists() {
        super("cpf_already_exists");
    }
}
