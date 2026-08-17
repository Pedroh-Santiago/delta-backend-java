package br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects

@JvmInline
value class BankCode(val value: String) {
    init { require(REGEX.matches(value)) { "bank_code_invalid" } }
    companion object { private val REGEX = Regex("^\\d{3}$") }
}