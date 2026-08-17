package br.com.deltaglobalbank.products.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import br.com.deltaglobalbank.products.TestcontainersConfiguration;
import br.com.deltaglobalbank.products.infrastructure.persistence.entities.ProductEntity;
import br.com.deltaglobalbank.products.infrastructure.persistence.repositories.JpaProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class ProductConstraintsTests {

    @Autowired
    JpaProductRepository jpa;

    private ProductEntity malformedEntity(
        BigDecimal minMonthlyRate,
        BigDecimal maxMonthlyRate,
        int minMonths,
        int maxMonths,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        BigDecimal commissionRate
    ) {
        return new ProductEntity(
            UUID.randomUUID(), UUID.randomUUID(), "LENDING",
            "INSS", "P",
            minMonthlyRate, maxMonthlyRate,
            minMonths, maxMonths,
            minAmount, maxAmount,
            commissionRate, true,
            Instant.now(), Instant.now(),
            null, null
        );
    }

    @Test
    void mustRejectNegativeMinMonthlyRateAtDb() {
        assertThrows(DataIntegrityViolationException.class, () ->
            jpa.saveAndFlush(malformedEntity(
                new BigDecimal("-1"), new BigDecimal("3.0"), 12, 60,
                new BigDecimal("1000"), new BigDecimal("50000"), new BigDecimal("2.0"))));
    }

    @Test
    void mustRejectMaxMonthlyRateBelowMinAtDb() {
        assertThrows(DataIntegrityViolationException.class, () ->
            jpa.saveAndFlush(malformedEntity(
                new BigDecimal("3"), new BigDecimal("2"), 12, 60,
                new BigDecimal("1000"), new BigDecimal("50000"), new BigDecimal("2.0"))));
    }

    @Test
    void mustRejectMinMonthsBelow1AtDb() {
        assertThrows(DataIntegrityViolationException.class, () ->
            jpa.saveAndFlush(malformedEntity(
                new BigDecimal("1.5"), new BigDecimal("3.0"), 0, 60,
                new BigDecimal("1000"), new BigDecimal("50000"), new BigDecimal("2.0"))));
    }

    @Test
    void mustRejectMaxMonthsBelowMinAtDb() {
        assertThrows(DataIntegrityViolationException.class, () ->
            jpa.saveAndFlush(malformedEntity(
                new BigDecimal("1.5"), new BigDecimal("3.0"), 60, 12,
                new BigDecimal("1000"), new BigDecimal("50000"), new BigDecimal("2.0"))));
    }

    @Test
    void mustRejectNegativeMinAmountAtDb() {
        assertThrows(DataIntegrityViolationException.class, () ->
            jpa.saveAndFlush(malformedEntity(
                new BigDecimal("1.5"), new BigDecimal("3.0"), 12, 60,
                new BigDecimal("-1"), new BigDecimal("50000"), new BigDecimal("2.0"))));
    }

    @Test
    void mustRejectMaxAmountBelowMinAtDb() {
        assertThrows(DataIntegrityViolationException.class, () ->
            jpa.saveAndFlush(malformedEntity(
                new BigDecimal("1.5"), new BigDecimal("3.0"), 12, 60,
                new BigDecimal("50000"), new BigDecimal("1000"), new BigDecimal("2.0"))));
    }

    @Test
    void mustRejectNegativeCommissionRateAtDb() {
        assertThrows(DataIntegrityViolationException.class, () ->
            jpa.saveAndFlush(malformedEntity(
                new BigDecimal("1.5"), new BigDecimal("3.0"), 12, 60,
                new BigDecimal("1000"), new BigDecimal("50000"), new BigDecimal("-1"))));
    }

    @Test
    void mustAllowNullCommissionRateAtDb() {
        assertDoesNotThrow(() -> {
            jpa.saveAndFlush(malformedEntity(
                new BigDecimal("1.5"), new BigDecimal("3.0"), 12, 60,
                new BigDecimal("1000"), new BigDecimal("50000"), null));
        });
    }
}
