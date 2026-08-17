package br.com.deltaglobalbank.products.domain.product;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {
    Product save(Product product);

    Product findById(UUID id, UUID tenantId);

    boolean existsActiveByTypeAndAgreement(UUID tenantId, ProductType type, String agreementName);

    Page<Product> findLendingPage(UUID tenantId, String agreementName, Boolean active, Pageable pageable);

    boolean existsAnotherActiveWithAgreement(UUID tenantId, ProductType type, String agreementName, UUID excludeId);

    List<Product> findProducts(UUID tenantId, ProductType type, Boolean active, String agreementName);
}
