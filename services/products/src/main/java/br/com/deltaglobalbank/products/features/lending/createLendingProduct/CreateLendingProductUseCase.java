package br.com.deltaglobalbank.products.features.lending.createLendingProduct;

import java.util.UUID;

import br.com.deltaglobalbank.products.domain.product.AgreementName;
import br.com.deltaglobalbank.products.domain.product.DisplayName;
import br.com.deltaglobalbank.products.domain.product.DuplicateActiveProduct;
import br.com.deltaglobalbank.products.domain.product.Product;
import br.com.deltaglobalbank.products.domain.product.ProductRepository;
import br.com.deltaglobalbank.products.domain.product.ProductSnapshot;
import br.com.deltaglobalbank.products.domain.product.ProductType;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateLendingProductUseCase {

    private final ProductRepository repository;

    public CreateLendingProductUseCase(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CreateLendingProductResponse execute(CreateLendingProductCommand command) {
        CreateLendingProductRequest req = command.request();
        ProductType type = ProductType.LENDING;

        if (repository.existsActiveByTypeAndAgreement(command.tenantId(), type, req.agreementName())) {
            throw new DuplicateActiveProduct();
        }

        String displayName = req.displayName() != null && !req.displayName().isBlank()
            ? req.displayName()
            : "Consignado " + req.agreementName();

        Product product = Product.newProduct(
            UuidCreator.getTimeOrderedEpoch(),
            command.tenantId(),
            type,
            new AgreementName(req.agreementName()),
            new DisplayName(displayName),
            req.minMonthlyRate(),
            req.maxMonthlyRate(),
            req.minMonths(),
            req.maxMonths(),
            req.minAmount(),
            req.maxAmount(),
            req.commissionRate(),
            command.createdBy()
        );

        Product saved = repository.save(product);

        ProductSnapshot s = saved.snapshot();
        return new CreateLendingProductResponse(
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
            s.createdAt()
        );
    }
}
