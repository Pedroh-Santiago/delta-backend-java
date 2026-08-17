package br.com.deltaglobalbank.products.infrastructure.persistence.mappers

import br.com.deltaglobalbank.products.domain.product.*
import br.com.deltaglobalbank.products.infrastructure.persistence.entities.ProductEntity
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertAll
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class ProductMappersTests {

    @Test
    fun `toDomain must map all fields from entity`() {
        val id = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val entity = ProductEntity(
            id = id,
            tenantId = tenantId,
            type = "LENDING",
            agreementName = "Convênio X",
            displayName = "Produto X",
            minMonthlyRate = BigDecimal("1.5"),
            maxMonthlyRate = BigDecimal("3.0"),
            minMonths = 12,
            maxMonths = 60,
            minAmount = BigDecimal("1000.00"),
            maxAmount = BigDecimal("50000.00"),
            commissionRate = BigDecimal("2.0"),
            active = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            createdBy = UUID.randomUUID(),
            updatedBy = UUID.randomUUID()
        )

        val domain = entity.toDomain()
        val s = domain.snapshot()

        assertAll(
            { assertEquals(id, s.id) },
            { assertEquals(tenantId, s.tenantId) },
            { assertEquals(ProductType.LENDING, s.type) },
            { assertEquals("Convênio X", s.agreementName.value) },
            { assertEquals("Produto X", s.displayName.value) },
            { assertEquals(BigDecimal("1.5"), s.minMonthlyRate) },
            { assertEquals(12, s.minMonths) },
            { assertEquals(true, s.active) }
        )
    }

    @Test
    fun `toEntity must map all fields from domain`() {
        val product = Product.newProduct(
            id = UUID.randomUUID(),
            tenantId = UUID.randomUUID(),
            type = ProductType.LENDING,
            agreementName = AgreementName("Convênio Y"),
            displayName = DisplayName("Produto Y"),
            minMonthlyRate = BigDecimal("2.0"),
            maxMonthlyRate = BigDecimal("4.0"),
            minMonths = 6,
            maxMonths = 36,
            minAmount = BigDecimal("500.00"),
            maxAmount = BigDecimal("10000.00"),
            commissionRate = null,
            createdBy = UUID.randomUUID()
        )

        val entity = product.toEntity()

        assertAll(
            { assertEquals(product.id, entity.id) },
            { assertEquals("LENDING", entity.type) },
            { assertEquals("Convênio Y", entity.agreementName) },
            { assertEquals(6, entity.minMonths) },
            { assertEquals(null, entity.commissionRate) },
            { assertEquals(true, entity.active) }
        )
    }

    @Test
    fun `applyTo must update only mutable fields and preserve the rest`() {
        val originalId = UUID.randomUUID()
        val originalCreatedAt = Instant.now().minusSeconds(3600)
        val entity = ProductEntity(
            id = originalId,
            tenantId = UUID.randomUUID(),
            type = "LENDING",
            agreementName = "Convênio Original",
            displayName = "Produto Original",
            minMonthlyRate = BigDecimal("1.0"),
            maxMonthlyRate = BigDecimal("2.0"),
            minMonths = 12,
            maxMonths = 60,
            minAmount = BigDecimal("1000"),
            maxAmount = BigDecimal("50000"),
            commissionRate = BigDecimal("1.5"),
            active = true,
            createdAt = originalCreatedAt,
            updatedAt = originalCreatedAt,
            createdBy = UUID.randomUUID(),
            updatedBy = UUID.randomUUID()
        )

        val domain = Product(
            id = UUID.randomUUID(),
            tenantId = UUID.randomUUID(),
            type = ProductType.LENDING,
            agreementName = AgreementName("Convênio Novo"),
            displayName = DisplayName("Produto Novo"),
            minMonthlyRate = BigDecimal("9.0"),
            maxMonthlyRate = BigDecimal("9.0"),
            minMonths = 1, maxMonths = 1,
            minAmount = BigDecimal("1"), maxAmount = BigDecimal("1"),
            commissionRate = BigDecimal("9.0"),
            active = false,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            createdBy = UUID.randomUUID(),
            updatedBy = UUID.randomUUID()
        )

        val result = domain.applyTo(entity)

        assertAll(
            { assertEquals(false, result.active) },
            { assertEquals(domain.snapshot().updatedAt, result.updatedAt) },
            { assertEquals(domain.snapshot().updatedBy, result.updatedBy) },
            { assertEquals(originalId, result.id) },
            { assertEquals("Convênio Novo", result.agreementName) },
            { assertEquals(0, BigDecimal("9.0").compareTo(result.minMonthlyRate)) },
            { assertEquals(originalId, result.id) },
            { assertEquals(originalCreatedAt, result.createdAt) }
        )
    }
}