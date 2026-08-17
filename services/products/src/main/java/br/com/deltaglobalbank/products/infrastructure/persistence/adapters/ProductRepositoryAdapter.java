package br.com.deltaglobalbank.products.infrastructure.persistence.adapters;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.products.domain.product.Product;
import br.com.deltaglobalbank.products.domain.product.ProductRepository;
import br.com.deltaglobalbank.products.domain.product.ProductType;
import br.com.deltaglobalbank.products.infrastructure.persistence.entities.ProductEntity;
import br.com.deltaglobalbank.products.infrastructure.persistence.mappers.ProductMapper;
import br.com.deltaglobalbank.products.infrastructure.persistence.repositories.JpaProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProductRepositoryAdapter implements ProductRepository {

    private final JpaProductRepository jpa;

    public ProductRepositoryAdapter(JpaProductRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public Product save(Product product) {
        ProductEntity existing = jpa.findById(product.getId()).orElse(null);
        ProductEntity entity = existing != null
            ? ProductMapper.applyTo(product, existing)
            : ProductMapper.toEntity(product);
        jpa.save(entity);
        return findById(product.getId(), product.getTenantId());
    }

    @Override
    public Product findById(UUID id, UUID tenantId) {
        ProductEntity entity = jpa.findByIdAndTenantId(id, tenantId);
        return entity != null ? ProductMapper.toDomain(entity) : null;
    }

    @Override
    public boolean existsActiveByTypeAndAgreement(UUID tenantId, ProductType type, String agreementName) {
        return jpa.existsByTenantIdAndTypeAndAgreementNameIgnoreCaseAndActiveTrue(
            tenantId, type.toDatabaseValue(), agreementName);
    }

    @Override
    public Page<Product> findLendingPage(UUID tenantId, String agreementName, Boolean active, Pageable pageable) {
        return jpa.searchLending(tenantId, ProductType.LENDING.toDatabaseValue(), agreementName, active, pageable)
            .map(ProductMapper::toDomain);
    }

    @Override
    public boolean existsAnotherActiveWithAgreement(UUID tenantId, ProductType type, String agreementName, UUID excludeId) {
        return jpa.existsAnotherActiveWithAgreement(tenantId, type.toDatabaseValue(), agreementName, excludeId);
    }

    @Override
    public List<Product> findProducts(UUID tenantId, ProductType type, Boolean active, String agreementName) {
        return jpa.searchProducts(tenantId, type != null ? type.toDatabaseValue() : null, active, agreementName)
            .stream()
            .map(ProductMapper::toDomain)
            .toList();
    }
}
