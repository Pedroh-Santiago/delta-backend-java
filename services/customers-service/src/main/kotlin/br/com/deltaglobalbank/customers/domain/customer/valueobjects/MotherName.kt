package br.com.deltaglobalbank.customers.domain.customer.valueobjects

@JvmInline
value class MotherName private constructor(val value: String) {
    companion object {
        operator fun invoke(raw: String): MotherName {
            val motherName = raw.trim()
            require(motherName.isNotBlank()) { "mother_name_blank" }
            require(motherName.length <= 255) { "mother_name_too_long" }
            return MotherName(motherName)
        }
    }
}