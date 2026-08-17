package br.com.deltaglobalbank.customers.domain.customer

enum class CustomerStatus {
    ACTIVE,
    INACTIVE;

    fun toDatabaseValue() : String = name.lowercase()

    companion object {
        fun fromDatabaseValue(value: String): CustomerStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("invalid costumer status: $value")
    }
}