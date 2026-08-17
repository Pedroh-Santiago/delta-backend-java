package br.com.deltaglobalbank.customers.domain.customer.valueobjects

@JvmInline
value class FullName private constructor(val value: String) {
    companion object {
        operator fun invoke(raw: String): FullName {
            val fullName = raw.trim()
            require(fullName.isNotBlank()) { "full_name_blank" }
            require(fullName.length in 3..255) { "full_name_invalid_length" }
            require(fullName.split(Regex("\\s+")).size >= 2) { "full_name_incomplete" }
            return FullName(fullName)
        }
    }
}