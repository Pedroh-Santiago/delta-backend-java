package br.com.deltaglobalbank.internal_treasury.domain.account;

public class Account {

    private final long accountId;
    private final long accountNumber;
    private final Cpf personalDocument;

    public Account(long accountId, long accountNumber, Cpf personalDocument) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.personalDocument = personalDocument;
    }

    public long accountId() {
        return accountId;
    }

    public long accountNumber() {
        return accountNumber;
    }

    public Cpf personalDocument() {
        return personalDocument;
    }
}
