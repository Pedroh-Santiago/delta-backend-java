package br.com.deltaglobalbank.customers.domain.document

enum class DocumentType {
    RG,
    CNH;

    fun toDatabaseValue() : String = name.lowercase()

    companion object {
        fun fromDatabaseValue(value: String): DocumentType =
            DocumentType.entries.firstOrNull{ it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("invalid DocumentType: $value")
    }
}