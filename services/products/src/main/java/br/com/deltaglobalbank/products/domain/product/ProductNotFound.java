package br.com.deltaglobalbank.products.domain.product;

public final class ProductNotFound extends ProductDomainException {

    public ProductNotFound() {
        super("product_not_found");
    }
}
