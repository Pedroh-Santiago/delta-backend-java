package br.com.deltaglobalbank.products.features.lending.updateLendingProduct;

import br.com.deltaglobalbank.products.domain.product.AgreementName;
import br.com.deltaglobalbank.products.domain.product.DisplayName;
import br.com.deltaglobalbank.products.domain.product.DuplicateActiveProduct;
import br.com.deltaglobalbank.products.domain.product.Product;
import br.com.deltaglobalbank.products.domain.product.ProductNotFound;
import br.com.deltaglobalbank.products.domain.product.ProductRepository;
import br.com.deltaglobalbank.products.domain.product.ProductSnapshot;
import br.com.deltaglobalbank.products.domain.product.ProductType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateLendingProductUseCase {

    private final ProductRepository productRepository;

    public UpdateLendingProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public UpdateLendingProductResponse execute(UpdateLendingProductCommand command) {
        UpdateLendingProductRequest req = command.request();
        Product product = productRepository.findById(command.productId(), command.tenantId());
        if (product == null) {
            throw new ProductNotFound();
        }

        if (req.active() && productRepository.existsAnotherActiveWithAgreement(
            command.tenantId(),
            ProductType.LENDING,
            req.agreementName(),
            product.getId())
        ) {
            throw new DuplicateActiveProduct();
        }

        DisplayName displayName = req.displayName() != null && !req.displayName().isBlank()
            ? new DisplayName(req.displayName())
            : new DisplayName("Consignado " + req.agreementName());

        product.updateProduct(
            new AgreementName(req.agreementName()),
            displayName,
            req.minMonthlyRate(),
            req.maxMonthlyRate(),
            req.minMonths(),
            req.maxMonths(),
            req.minAmount(),
            req.maxAmount(),
            req.commissionRate(),
            req.active(),
            command.updatedBy()
        );

        Product saved = productRepository.save(product);
        ProductSnapshot s = saved.snapshot();
        return new UpdateLendingProductResponse(
            s.id(),
            s.tenantId(),
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
