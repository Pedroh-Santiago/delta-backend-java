package br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects

@JvmInline
value class Agency(val value: String) {
    init { require(REGEX.matches(value)) { "agency_invalid" } }
    companion object { private val REGEX = Regex("^\\d{4}$") }
}