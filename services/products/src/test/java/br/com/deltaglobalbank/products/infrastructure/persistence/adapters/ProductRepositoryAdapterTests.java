package br.com.deltaglobalbank.products.infrastructure.persistence.adapters;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import br.com.deltaglobalbank.products.TestcontainersConfiguration;
import br.com.deltaglobalbank.products.domain.product.AgreementName;
import br.com.deltaglobalbank.products.domain.product.DisplayName;
import br.com.deltaglobalbank.products.domain.product.Product;
import br.com.deltaglobalbank.products.domain.product.ProductSnapshot;
import br.com.deltaglobalbank.products.domain.product.ProductType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ProductRepositoryAdapter.class, TestcontainersConfiguration.class})
@ActiveProfiles("test")
class ProductRepositoryAdapterTests {

    @Autowired
    ProductRepositoryAdapter adapter;

    private Product buildProduct(UUID tenantId, ProductType type, String agreementName, boolean active) {
        return new Product(
            UUID.randomUUID(),
            tenantId,
            type,
            new AgreementName(agreementName),
            new DisplayName("Produto X"),
            new BigDecimal("1.5"),
            new BigDecimal("3.0"),
            12, 60,
            new BigDecimal("1000.00"), new BigDecimal("50000.00"),
            new BigDecimal("2.0"),
            active,
            Instant.now(),
            Instant.now(),
            UUID.randomUUID(),
            UUID.randomUUID()
        );
    }

    private Product buildProduct(UUID tenantId) {
        return buildProduct(tenantId, ProductType.LENDING, "Convênio X", true);
    }

    private Product buildProduct(UUID tenantId, String agreementName) {
        return buildProduct(tenantId, ProductType.LENDING, agreementName, true);
    }

    @Test
    void mustSaveAndRetrieveProductByIdAndTenant() {
        UUID tenantId = UUID.randomUUID();
        Product product = buildProduct(tenantId);

        adapter.save(product);
        Product found = adapter.findById(product.getId(), tenantId);

        assertNotNull(found);
        assertEquals(product.getId(), found.getId());
        assertEquals("Convênio X", found.snapshot().agreementName().value());
    }

    @Test
    void mustReturnNullWhenProductNotFound() {
        Product found = adapter.findById(UUID.randomUUID(), UUID.randomUUID());
        assertNull(found);
    }

    @Test
    void mustNotFindProductFromAnotherTenant() {
        UUID ownerTenant = UUID.randomUUID();
        Product product = buildProduct(ownerTenant);
        adapter.save(product);

        Product found = adapter.findById(product.getId(), UUID.randomUUID());
        assertNull(found);
    }

    @Test
    void mustDetectActiveProductWithSameTypeAndAgreement() {
        UUID tenantId = UUID.randomUUID();
        adapter.save(buildProduct(tenantId, "Convênio Y"));

        boolean exists = adapter.existsActiveByTypeAndAgreement(tenantId, ProductType.LENDING, "Convênio Y");
        assertTrue(exists);
    }

    @Test
    void mustBeCaseInsensitiveOnAgreementName() {
        UUID tenantId = UUID.randomUUID();
        adapter.save(buildProduct(tenantId, "Convênio Z"));

        boolean exists = adapter.existsActiveByTypeAndAgreement(tenantId, ProductType.LENDING, "convênio z");
        assertTrue(exists);
    }

    @Test
    void mustIsolateExistsCheckByTenant() {
        UUID tenantA = UUID.randomUUID();
        adapter.save(buildProduct(tenantA, "Convênio T"));

        boolean exists = adapter.existsActiveByTypeAndAgreement(UUID.randomUUID(), ProductType.LENDING, "Convênio T");
        assertFalse(exists);
    }

    @Test
    void mustAllowSameAgreementInDifferentTenants() {
        adapter.save(buildProduct(UUID.randomUUID(), "INSS"));

        assertDoesNotThrow(() -> {
            adapter.save(buildProduct(UUID.randomUUID(), "INSS"));
        });
    }

    @Test
    void mustRoundTripAllFieldsThroughPersistence() {
        UUID tenantId = UUID.randomUUID();
        Product product = buildProduct(tenantId, "INSS");
        adapter.save(product);

        Product found = adapter.findById(product.getId(), tenantId);
        ProductSnapshot original = product.snapshot();
        ProductSnapshot persisted = found.snapshot();
        assertAll(
            () -> assertEquals(original.id(), persisted.id()),
            () -> assertEquals(original.tenantId(), persisted.tenantId()),
            () -> assertEquals(original.type(), persisted.type()),
            () -> assertEquals(original.agreementName().value(), persisted.agreementName().value()),
            () -> assertEquals(original.minMonthlyRate(), persisted.minMonthlyRate()),
            () -> assertEquals(original.minMonths(), persisted.minMonths()),
            () -> assertEquals(original.commissionRate(), persisted.commissionRate()),
            () -> assertEquals(original.active(), persisted.active())
        );
    }
}
