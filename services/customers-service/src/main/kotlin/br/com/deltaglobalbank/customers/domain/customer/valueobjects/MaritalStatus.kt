package br.com.deltaglobalbank.customers.domain.customer.valueobjects

enum class MaritalStatus {
    SINGLE,
    MARRIED,
    DIVORCED,
    WIDOWED,
    STABLE_UNION;

    fun toDatabaseValue() : String = name.lowercase()

    companion object {
        fun fromDatabaseValue(value: String): MaritalStatus =
            MaritalStatus.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Marital status invalid: $value")
    }
}