package br.com.deltaglobalbank.identity.domain.tenant

enum class TenantStatus {
    ACTIVE,
    SUSPENDED,
    INACTIVE;

    fun toDatabaseValue(): String = name.lowercase()

    companion object {
        fun fromDatabaseValue(value: String): TenantStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Status de tenant inválido: $value")
    }
}