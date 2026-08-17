package br.com.deltaglobalbank.internal_treasury.domain.account

interface BalanceService {
    fun getBalance(accountId : Long): Long
}