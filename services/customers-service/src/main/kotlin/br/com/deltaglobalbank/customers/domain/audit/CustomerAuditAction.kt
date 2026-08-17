package br.com.deltaglobalbank.customers.domain.audit

enum class CustomerAuditAction {
    BANK_ACCOUNT_ADDED,
    BANK_ACCOUNT_REMOVED,
    CPF_CHANGED,
    CUSTOMER_DELETED,
    FULL_NAME_CHANGED,
    STATUS_CHANGED;

    fun toDatabaseValue(): String = name.lowercase()

    companion object {
        fun fromDatabaseValue(value: String): CustomerAuditAction =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("invalid customer audit action '$value'")
    }
}