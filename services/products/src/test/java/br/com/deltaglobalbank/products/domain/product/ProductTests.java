package br.com.deltaglobalbank.products.domain.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class ProductTests {

    private Product newValidProduct(
        BigDecimal minMonthlyRate,
        BigDecimal maxMonthlyRate,
        int minMonths,
        int maxMonths,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        BigDecimal commissionRate
    ) {
        return Product.newProduct(
            UUID.randomUUID(),
            UUID.randomUUID(),
            ProductType.LENDING,
            new AgreementName("Convênio X"),
            new DisplayName("Produto X"),
            minMonthlyRate,
            maxMonthlyRate,
            minMonths,
            maxMonths,
            minAmount,
            maxAmount,
            commissionRate,
            UUID.randomUUID()
        );
    }

    private Product newValidProduct() {
        return newValidProduct(
            new BigDecimal("1.5"),
            new BigDecimal("3.0"),
            12,
            60,
            new BigDecimal("1000.00"),
            new BigDecimal("50000.00"),
            new BigDecimal("2.0")
        );
    }

    @Test
    void mustCreateProductAsActiveWithValidFields() {
        UUID createdBy = UUID.randomUUID();
        Product product = Product.newProduct(
            UUID.randomUUID(),
            UUID.randomUUID(),
            ProductType.LENDING,
            new AgreementName("Convênio X"),
            new DisplayName("Produto X"),
            new BigDecimal("1.5"),
            new BigDecimal("3.0"),
            12,
            60,
            new BigDecimal("1000.00"),
            new BigDecimal("50000.00"),
            new BigDecimal("2.0"),
            createdBy
        );

        ProductSnapshot s = product.snapshot();
        assertTrue(s.active());
        assertEquals(createdBy, s.createdBy());
        assertEquals(createdBy, s.updatedBy());
        assertEquals(s.createdAt(), s.updatedAt());
    }

    @Test
    void mustRejectNegativeMinMonthlyRate() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            newValidProduct(new BigDecimal("-0.1"), new BigDecimal("3.0"), 12, 60,
                new BigDecimal("1000.00"), new BigDecimal("50000.00"), new BigDecimal("2.0")));
        assertEquals("min_monthly_rate_negative", ex.getMessage());
    }

    @Test
    void mustRejectMaxMonthlyRateBelowMin() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            newValidProduct(new BigDecimal("3.0"), new BigDecimal("2.0"), 12, 60,
                new BigDecimal("1000.00"), new BigDecimal("50000.00"), new BigDecimal("2.0")));
        assertEquals("max_monthly_rate_lt_min", ex.getMessage());
    }

    @Test
    void mustRejectMinMonthsBelow1() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            newValidProduct(new BigDecimal("1.5"), new BigDecimal("3.0"), 0, 60,
                new BigDecimal("1000.00"), new BigDecimal("50000.00"), new BigDecimal("2.0")));
        assertEquals("min_months_lt_1", ex.getMessage());
    }

    @Test
    void mustRejectMaxMonthsBelowMin() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            newValidProduct(new BigDecimal("1.5"), new BigDecimal("3.0"), 60, 12,
                new BigDecimal("1000.00"), new BigDecimal("50000.00"), new BigDecimal("2.0")));
        assertEquals("max_months_lt_min", ex.getMessage());
    }

    @Test
    void mustRejectNegativeMinAmount() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            newValidProduct(new BigDecimal("1.5"), new BigDecimal("3.0"), 12, 60,
                new BigDecimal("-1.00"), new BigDecimal("50000.00"), new BigDecimal("2.0")));
        assertEquals("min_amount_negative", ex.getMessage());
    }

    @Test
    void mustRejectMaxAmountBelowMin() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            newValidProduct(new BigDecimal("1.5"), new BigDecimal("3.0"), 12, 60,
                new BigDecimal("50000.00"), new BigDecimal("1000.00"), new BigDecimal("2.0")));
        assertEquals("max_amount_lt_min", ex.getMessage());
    }

    @Test
    void mustRejectNegativeCommissionRate() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            newValidProduct(new BigDecimal("1.5"), new BigDecimal("3.0"), 12, 60,
                new BigDecimal("1000.00"), new BigDecimal("50000.00"), new BigDecimal("-0.5")));
        assertEquals("commission_rate_negative", ex.getMessage());
    }

    @Test
    void mustAcceptMaxMonthlyRateEqualToMin() {
        Product product = newValidProduct(new BigDecimal("2.0"), new BigDecimal("2.0"), 12, 60,
            new BigDecimal("1000.00"), new BigDecimal("50000.00"), new BigDecimal("2.0"));
        assertEquals(new BigDecimal("2.0"), product.snapshot().maxMonthlyRate());
    }

    @Test
    void mustAcceptMinMonthsEqualTo1() {
        Product product = newValidProduct(new BigDecimal("1.5"), new BigDecimal("3.0"), 1, 1,
            new BigDecimal("1000.00"), new BigDecimal("50000.00"), new BigDecimal("2.0"));
        assertEquals(1, product.snapshot().minMonths());
    }

    @Test
    void mustAcceptNullCommissionRate() {
        Product product = newValidProduct(new BigDecimal("1.5"), new BigDecimal("3.0"), 12, 60,
            new BigDecimal("1000.00"), new BigDecimal("50000.00"), null);
        assertNull(product.snapshot().commissionRate());
    }

    @Test
    void mustBeEqualWhenIdsMatch() {
        UUID sharedId = UUID.randomUUID();

        Product a = new Product(
            sharedId, UUID.randomUUID(), ProductType.LENDING,
            new AgreementName("A"), new DisplayName("A"),
            new BigDecimal("1.0"), new BigDecimal("2.0"),
            1, 12, new BigDecimal("100"), new BigDecimal("200"),
            null, true,
            Instant.now(), Instant.now(),
            null, null
        );
        Product b = new Product(
            sharedId, UUID.randomUUID(), ProductType.LENDING,
            new AgreementName("B"), new DisplayName("B"),
            new BigDecimal("5.0"), new BigDecimal("9.0"),
            3, 24, new BigDecimal("500"), new BigDecimal("900"),
            new BigDecimal("1.0"), false,
            Instant.now(), Instant.now(),
            null, null
        );
        Product c = new Product(
            UUID.randomUUID(), UUID.randomUUID(), ProductType.LENDING,
            new AgreementName("A"), new DisplayName("A"),
            new BigDecimal("1.0"), new BigDecimal("2.0"),
            1, 12, new BigDecimal("100"), new BigDecimal("200"),
            null, true,
            Instant.now(), Instant.now(),
            null, null
        );

        assertEquals(a, b);
        assertNotEquals(a, c);
    }

    @Test
    void mustExposeFieldsThroughSnapshot() {
        Product product = newValidProduct(new BigDecimal("1.5"), new BigDecimal("3.0"), 6, 48,
            new BigDecimal("1000.00"), new BigDecimal("50000.00"), new BigDecimal("2.0"));
        ProductSnapshot s = product.snapshot();
        assertEquals(ProductType.LENDING, s.type());
        assertEquals("Convênio X", s.agreementName().value());
        assertEquals(6, s.minMonths());
        assertEquals(48, s.maxMonths());
        assertTrue(s.active());
    }

    @Test
    void mustRejectBlankAgreementName() {
        assertThrows(IllegalArgumentException.class, (Executable) () -> new AgreementName(""));
        assertThrows(IllegalArgumentException.class, (Executable) () -> new AgreementName("   "));
    }

    @Test
    void mustRejectBlankDisplayName() {
        assertThrows(IllegalArgumentException.class, (Executable) () -> new DisplayName(""));
        assertThrows(IllegalArgumentException.class, (Executable) () -> new DisplayName("   "));
    }

    @Test
    void mustRejectUnknownProductTypeFromDatabaseValue() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            ProductType.fromDatabaseValue("XPTO"));
        assertTrue(ex.getMessage().contains("invalid_product_type"));
    }

    @Test
    void mustResolveProductTypeCaseInsensitivelyFromDatabaseValue() {
        assertEquals(ProductType.LENDING, ProductType.fromDatabaseValue("lending"));
        assertEquals(ProductType.LENDING, ProductType.fromDatabaseValue("LENDING"));
        assertEquals(ProductType.LENDING, ProductType.fromDatabaseValue("Lending"));
    }
}
