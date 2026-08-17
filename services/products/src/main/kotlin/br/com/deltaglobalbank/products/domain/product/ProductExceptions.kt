package br.com.deltaglobalbank.products.domain.product

sealed class ProductDomainException(message: String) : RuntimeException(message)
class DuplicateActiveProduct : ProductDomainException("duplicate_active_product")
class ProductNotFound : ProductDomainException("product_not_found")