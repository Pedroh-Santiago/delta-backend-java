package br.com.deltaglobalbank.products.domain.product;

public final class DuplicateActiveProduct extends ProductDomainException {

    public DuplicateActiveProduct() {
        super("duplicate_active_product");
    }
}
