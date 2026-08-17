package br.com.deltaglobalbank.products.features.lending.getLendingProduct;

import br.com.deltaglobalbank.products.domain.product.Product;
import br.com.deltaglobalbank.products.domain.product.ProductNotFound;
import br.com.deltaglobalbank.products.domain.product.ProductRepository;
import br.com.deltaglobalbank.products.domain.product.ProductSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetLendingProductUseCase {

    private final ProductRepository productRepository;

    public GetLendingProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public GetLendingProductResponse execute(GetLendingProductCommand command) {
        Product product = productRepository.findById(command.productId(), command.tenantId());
        if (product == null) {
            throw new ProductNotFound();
        }

        ProductSnapshot s = product.snapshot();

        return new GetLendingProductResponse(
            s.id(),
            s.type().toDatabaseValue(),
            s.agreementName().value(),
            s.displayName().value(),
            s.minMonthlyRate(),
            s.maxMonthlyRate(),
            s.minMonths(),
            s.maxMonths(),
            s.minAmount(),
            s.maxAmount(),
            s.commissionRate(),
            s.active(),
            s.createdAt(),
            s.updatedAt()
        );
    }
}
