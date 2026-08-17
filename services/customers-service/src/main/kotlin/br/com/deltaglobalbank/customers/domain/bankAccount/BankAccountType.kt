package br.com.deltaglobalbank.customers.domain.bankAccount

enum class BankAccountType {
    CHECKING,
    SAVINGS;

    fun toDatabaseValue(): String = name.lowercase()

    companion object {
        fun fromDatabaseValue(value: String): BankAccountType =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Bank account type invalid")
    }
}