package br.com.deltaglobalbank.products.infrastructure.persistence.mappers;

import br.com.deltaglobalbank.products.domain.product.AgreementName;
import br.com.deltaglobalbank.products.domain.product.DisplayName;
import br.com.deltaglobalbank.products.domain.product.Product;
import br.com.deltaglobalbank.products.domain.product.ProductSnapshot;
import br.com.deltaglobalbank.products.domain.product.ProductType;
import br.com.deltaglobalbank.products.infrastructure.persistence.entities.ProductEntity;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static Product toDomain(ProductEntity entity) {
        return new Product(
            entity.getId(),
            entity.getTenantId(),
            ProductType.fromDatabaseValue(entity.getType()),
            new AgreementName(entity.getAgreementName()),
            new DisplayName(entity.getDisplayName()),
            entity.getMinMonthlyRate(),
            entity.getMaxMonthlyRate(),
            entity.getMinMonths(),
            entity.getMaxMonths(),
            entity.getMinAmount(),
            entity.getMaxAmount(),
            entity.getCommissionRate(),
            entity.isActive(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getCreatedBy(),
            entity.getUpdatedBy()
        );
    }

    public static ProductEntity toEntity(Product product) {
        ProductSnapshot s = product.snapshot();
        return new ProductEntity(
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
            s.updatedAt(),
            s.createdBy(),
            s.updatedBy()
        );
    }

    public static ProductEntity applyTo(Product product, ProductEntity entity) {
        ProductSnapshot s = product.snapshot();
        entity.setAgreementName(s.agreementName().value());
        entity.setDisplayName(s.displayName().value());
        entity.setMinMonthlyRate(s.minMonthlyRate());
        entity.setMaxMonthlyRate(s.maxMonthlyRate());
        entity.setMinMonths(s.minMonths());
        entity.setMaxMonths(s.maxMonths());
        entity.setMinAmount(s.minAmount());
        entity.setMaxAmount(s.maxAmount());
        entity.setCommissionRate(s.commissionRate());
        entity.setActive(s.active());
        entity.setUpdatedAt(s.updatedAt());
        entity.setUpdatedBy(s.updatedBy());
        return entity;
    }
}
