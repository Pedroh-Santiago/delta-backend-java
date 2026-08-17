package br.com.deltaglobalbank.products.features.lending.deleteLendingProduct;

import br.com.deltaglobalbank.products.domain.product.Product;
import br.com.deltaglobalbank.products.domain.product.ProductNotFound;
import br.com.deltaglobalbank.products.domain.product.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteLendingProductUseCase {

    private final ProductRepository repository;

    public DeleteLendingProductUseCase(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void execute(DeleteLendingProductCommand command) {
        Product product = repository.findById(command.productId(), command.tenantId());
        if (product == null) {
            throw new ProductNotFound();
        }

        product.deactivate(command.deletedBy());
        repository.save(product);
    }
}
