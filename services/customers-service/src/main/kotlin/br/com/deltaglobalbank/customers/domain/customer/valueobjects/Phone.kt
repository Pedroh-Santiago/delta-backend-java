package br.com.deltaglobalbank.customers.domain.customer.valueobjects

@JvmInline
value class Phone(val value: String) {
    init { require(REGEX.matches(value)) { "phone_invalid" } }
    companion object { private val REGEX = Regex("^\\+\\d{8,15}$") }
}