package br.com.deltaglobalbank.internal_treasury.domain.account

interface AccountRepository {
    fun findByCpf(cpf: Cpf): Account?
}