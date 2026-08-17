package br.com.deltaglobalbank.products.domain.product

enum class ProductType {
    LENDING;
    fun toDatabaseValue(): String = name
    companion object {
        fun fromDatabaseValue(value: String): ProductType =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("invalid_product_type: $value")
    }
}