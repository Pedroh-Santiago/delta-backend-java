package br.com.deltaglobalbank.customers.domain.bankAccount

enum class BankAccountPurpose {
    DISBURSEMENT,
    PAYOFF;

    fun toDatabaseValue(): String = name.lowercase()

    companion object {
        fun fromDatabaseValue(value: String): BankAccountPurpose =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("bank account purpose invalid: $value")
    }
}