package br.com.deltaglobalbank.products.features.lending.listLendingProduct;

import java.util.List;

import br.com.deltaglobalbank.products.domain.product.Product;
import br.com.deltaglobalbank.products.domain.product.ProductRepository;
import br.com.deltaglobalbank.products.domain.product.ProductSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListLendingProductsUseCase {

    public static final int MAX = 100;
    public static final int DEFAULT = 20;

    private final ProductRepository repository;

    public ListLendingProductsUseCase(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ListLendingProductsResponse execute(ListLendingProductsQuery query) {
        int safePage = Math.max(query.page(), 0);
        int requestedSize = query.size() != null ? query.size() : DEFAULT;
        int safeSize = Math.min(Math.max(requestedSize, 1), MAX);

        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> result = repository.findLendingPage(query.tenantId(), query.agreementName(), query.active(), pageable);

        List<ListedLendingProduct> items = result.getContent().stream()
            .map(ListLendingProductsUseCase::toListed)
            .toList();

        return new ListLendingProductsResponse(
            items,
            safePage,
            safeSize,
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    private static ListedLendingProduct toListed(Product product) {
        ProductSnapshot s = product.snapshot();
        return new ListedLendingProduct(
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
