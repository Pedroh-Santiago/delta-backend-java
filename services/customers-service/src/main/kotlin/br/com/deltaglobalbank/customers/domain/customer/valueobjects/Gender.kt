package br.com.deltaglobalbank.customers.domain.customer.valueobjects

enum class Gender {
    MALE,
    FEMALE,
    OTHER;

    fun toDatabaseValue() : String = name.lowercase()

    companion object {
        fun fromDatabaseValue(value: String): Gender =
            Gender.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Gender invalid: $value")
    }
}