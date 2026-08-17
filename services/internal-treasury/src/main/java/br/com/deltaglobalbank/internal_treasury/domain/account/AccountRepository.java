package br.com.deltaglobalbank.internal_treasury.domain.account;

public interface AccountRepository {
    Account findByCpf(Cpf cpf);
}
