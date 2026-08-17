package br.com.deltaglobalbank.identity.domain.apiClient

enum class ApiClientStatus {
    ACTIVE,
    SUSPENDED;

    fun toDatabaseValue(): String = name.lowercase()

    companion object {
        fun fromDatabaseValue(value: String): ApiClientStatus =
            ApiClientStatus.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Status de API CLIENT inválido: $value")
    }
}