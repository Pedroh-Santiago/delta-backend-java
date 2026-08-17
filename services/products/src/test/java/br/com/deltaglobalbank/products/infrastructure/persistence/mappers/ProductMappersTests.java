package br.com.deltaglobalbank.products.infrastructure.persistence.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import br.com.deltaglobalbank.products.domain.product.AgreementName;
import br.com.deltaglobalbank.products.domain.product.DisplayName;
import br.com.deltaglobalbank.products.domain.product.Product;
import br.com.deltaglobalbank.products.domain.product.ProductSnapshot;
import br.com.deltaglobalbank.products.domain.product.ProductType;
import br.com.deltaglobalbank.products.infrastructure.persistence.entities.ProductEntity;
import org.junit.jupiter.api.Test;

class ProductMappersTests {

    @Test
    void toDomainMustMapAllFieldsFromEntity() {
        UUID id = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        ProductEntity entity = new ProductEntity(
            id,
            tenantId,
            "LENDING",
            "Convênio X",
            "Produto X",
            new BigDecimal("1.5"),
            new BigDecimal("3.0"),
            12,
            60,
            new BigDecimal("1000.00"),
            new BigDecimal("50000.00"),
            new BigDecimal("2.0"),
            true,
            Instant.now(),
            Instant.now(),
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        Product domain = ProductMapper.toDomain(entity);
        ProductSnapshot s = domain.snapshot();

        assertEquals(id, s.id());
        assertEquals(tenantId, s.tenantId());
        assertEquals(ProductType.LENDING, s.type());
        assertEquals("Convênio X", s.agreementName().value());
        assertEquals("Produto X", s.displayName().value());
        assertEquals(new BigDecimal("1.5"), s.minMonthlyRate());
        assertEquals(12, s.minMonths());
        assertEquals(true, s.active());
    }

    @Test
    void toEntityMustMapAllFieldsFromDomain() {
        Product product = Product.newProduct(
            UUID.randomUUID(),
            UUID.randomUUID(),
            ProductType.LENDING,
            new AgreementName("Convênio Y"),
            new DisplayName("Produto Y"),
            new BigDecimal("2.0"),
            new BigDecimal("4.0"),
            6,
            36,
            new BigDecimal("500.00"),
            new BigDecimal("10000.00"),
            null,
            UUID.randomUUID()
        );

        ProductEntity entity = ProductMapper.toEntity(product);

        assertEquals(product.getId(), entity.getId());
        assertEquals("LENDING", entity.getType());
        assertEquals("Convênio Y", entity.getAgreementName());
        assertEquals(6, entity.getMinMonths());
        assertEquals(null, entity.getCommissionRate());
        assertEquals(true, entity.isActive());
    }

    @Test
    void applyToMustUpdateOnlyMutableFieldsAndPreserveTheRest() {
        UUID originalId = UUID.randomUUID();
        Instant originalCreatedAt = Instant.now().minusSeconds(3600);
        ProductEntity entity = new ProductEntity(
            originalId,
            UUID.randomUUID(),
            "LENDING",
            "Convênio Original",
            "Produto Original",
            new BigDecimal("1.0"),
            new BigDecimal("2.0"),
            12,
            60,
            new BigDecimal("1000"),
            new BigDecimal("50000"),
            new BigDecimal("1.5"),
            true,
            originalCreatedAt,
            originalCreatedAt,
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        Product domain = new Product(
            UUID.randomUUID(),
            UUID.randomUUID(),
            ProductType.LENDING,
            new AgreementName("Convênio Novo"),
            new DisplayName("Produto Novo"),
            new BigDecimal("9.0"),
            new BigDecimal("9.0"),
            1, 1,
            new BigDecimal("1"), new BigDecimal("1"),
            new BigDecimal("9.0"),
            false,
            Instant.now(),
            Instant.now(),
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        ProductEntity result = ProductMapper.applyTo(domain, entity);

        assertEquals(false, result.isActive());
        assertEquals(domain.snapshot().updatedAt(), result.getUpdatedAt());
        assertEquals(domain.snapshot().updatedBy(), result.getUpdatedBy());
        assertEquals(originalId, result.getId());
        assertEquals("Convênio Novo", result.getAgreementName());
        assertEquals(0, new BigDecimal("9.0").compareTo(result.getMinMonthlyRate()));
        assertEquals(originalCreatedAt, result.getCreatedAt());
    }
}
