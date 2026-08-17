package br.com.deltaglobalbank.identity.domain.token

enum class SigningKeyStatus {
    ACTIVE,
    RETIRED,
    REVOKED;

    fun toDatabaseValue(): String = name.lowercase()

    companion object {
        fun fromDatabaseValue(value: String): SigningKeyStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("signing_key_status_invalid: $value")
    }
}
