package br.com.deltaglobalbank.products.domain.product;

public abstract sealed class ProductDomainException extends RuntimeException
    permits DuplicateActiveProduct, ProductNotFound {

    protected ProductDomainException(String message) {
        super(message);
    }
}
