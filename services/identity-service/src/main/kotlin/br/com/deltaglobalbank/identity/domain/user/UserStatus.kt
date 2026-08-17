package br.com.deltaglobalbank.identity.domain.user

enum class UserStatus {
    ACTIVE,
    SUSPENDED,
    LOCKED;

    fun toDatabaseValue(): String = name.lowercase()

    companion object {
        fun fromDatabaseValue(value: String): UserStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Status de user inválido: $value")
    }
}