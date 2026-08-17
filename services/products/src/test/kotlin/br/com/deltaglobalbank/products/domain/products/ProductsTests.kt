package br.com.deltaglobalbank.products.domain.product

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class ProductTests {

    private fun newValidProduct(
        minMonthlyRate: BigDecimal = BigDecimal("1.5"),
        maxMonthlyRate: BigDecimal = BigDecimal("3.0"),
        minMonths: Int = 12,
        maxMonths: Int = 60,
        minAmount: BigDecimal = BigDecimal("1000.00"),
        maxAmount: BigDecimal = BigDecimal("50000.00"),
        commissionRate: BigDecimal? = BigDecimal("2.0"),
    ): Product = Product.newProduct(
        id = UUID.randomUUID(),
        tenantId = UUID.randomUUID(),
        type = ProductType.LENDING,
        agreementName = AgreementName("Convênio X"),
        displayName = DisplayName("Produto X"),
        minMonthlyRate = minMonthlyRate,
        maxMonthlyRate = maxMonthlyRate,
        minMonths = minMonths,
        maxMonths = maxMonths,
        minAmount = minAmount,
        maxAmount = maxAmount,
        commissionRate = commissionRate,
        createdBy = UUID.randomUUID(),
    )

    @Test
    fun `must create product as active with valid fields`() {
        val createdBy = UUID.randomUUID()
        val product = Product.newProduct(
            id = UUID.randomUUID(),
            tenantId = UUID.randomUUID(),
            type = ProductType.LENDING,
            agreementName = AgreementName("Convênio X"),
            displayName = DisplayName("Produto X"),
            minMonthlyRate = BigDecimal("1.5"),
            maxMonthlyRate = BigDecimal("3.0"),
            minMonths = 12,
            maxMonths = 60,
            minAmount = BigDecimal("1000.00"),
            maxAmount = BigDecimal("50000.00"),
            commissionRate = BigDecimal("2.0"),
            createdBy = createdBy,
        )

        val s = product.snapshot()
        assertAll(
            { assertTrue(s.active) },
            { assertEquals(createdBy, s.createdBy) },
            { assertEquals(createdBy, s.updatedBy) },
            { assertEquals(s.createdAt, s.updatedAt) }
        )
    }

    @Test
    fun `must reject negative min monthly rate`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            newValidProduct(minMonthlyRate = BigDecimal("-0.1"))
        }
        assertEquals("min_monthly_rate_negative", ex.message)
    }

    @Test
    fun `must reject max monthly rate below min`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            newValidProduct(minMonthlyRate = BigDecimal("3.0"), maxMonthlyRate = BigDecimal("2.0"))
        }
        assertEquals("max_monthly_rate_lt_min", ex.message)
    }

    @Test
    fun `must reject min months below 1`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            newValidProduct(minMonths = 0)
        }
        assertEquals("min_months_lt_1", ex.message)
    }

    @Test
    fun `must reject max months below min`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            newValidProduct(minMonths = 60, maxMonths = 12)
        }
        assertEquals("max_months_lt_min", ex.message)
    }

    @Test
    fun `must reject negative min amount`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            newValidProduct(minAmount = BigDecimal("-1.00"))
        }
        assertEquals("min_amount_negative", ex.message)
    }

    @Test
    fun `must reject max amount below min`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            newValidProduct(minAmount = BigDecimal("50000.00"), maxAmount = BigDecimal("1000.00"))
        }
        assertEquals("max_amount_lt_min", ex.message)
    }

    @Test
    fun `must reject negative commission rate`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            newValidProduct(commissionRate = BigDecimal("-0.5"))
        }
        assertEquals("commission_rate_negative", ex.message)
    }

    @Test
    fun `must accept max monthly rate equal to min`() {
        val product = newValidProduct(
            minMonthlyRate = BigDecimal("2.0"),
            maxMonthlyRate = BigDecimal("2.0")
        )
        assertEquals(BigDecimal("2.0"), product.snapshot().maxMonthlyRate)
    }

    @Test
    fun `must accept min months equal to 1`() {
        val product = newValidProduct(minMonths = 1, maxMonths = 1)
        assertEquals(1, product.snapshot().minMonths)
    }

    @Test
    fun `must accept null commission rate`() {
        val product = newValidProduct(commissionRate = null)
        assertNull(product.snapshot().commissionRate)
    }

    @Test
    fun `must be equal when ids match`() {
        val sharedId = UUID.randomUUID()

        val a = Product(
            id = sharedId, tenantId = UUID.randomUUID(), type = ProductType.LENDING,
            agreementName = AgreementName("A"), displayName = DisplayName("A"),
            minMonthlyRate = BigDecimal("1.0"), maxMonthlyRate = BigDecimal("2.0"),
            minMonths = 1, maxMonths = 12, minAmount = BigDecimal("100"), maxAmount = BigDecimal("200"),
            commissionRate = null, active = true,
            createdAt = Instant.now(), updatedAt = Instant.now(),
            createdBy = null, updatedBy = null
        )
        val b = Product(
            id = sharedId, tenantId = UUID.randomUUID(), type = ProductType.LENDING,
            agreementName = AgreementName("B"), displayName = DisplayName("B"),
            minMonthlyRate = BigDecimal("5.0"), maxMonthlyRate = BigDecimal("9.0"),
            minMonths = 3, maxMonths = 24, minAmount = BigDecimal("500"), maxAmount = BigDecimal("900"),
            commissionRate = BigDecimal("1.0"), active = false,
            createdAt = Instant.now(), updatedAt = Instant.now(),
            createdBy = null, updatedBy = null
        )
        val c = Product(
            id = UUID.randomUUID(), tenantId = UUID.randomUUID(), type = ProductType.LENDING,
            agreementName = AgreementName("A"), displayName = DisplayName("A"),
            minMonthlyRate = BigDecimal("1.0"), maxMonthlyRate = BigDecimal("2.0"),
            minMonths = 1, maxMonths = 12, minAmount = BigDecimal("100"), maxAmount = BigDecimal("200"),
            commissionRate = null, active = true,
            createdAt = Instant.now(), updatedAt = Instant.now(),
            createdBy = null, updatedBy = null
        )

        assertAll(
            { assertEquals(a, b) },
            { assertNotEquals(a, c) }
        )
    }

    @Test
    fun `must expose fields through snapshot`() {
        val product = newValidProduct(minMonths = 6, maxMonths = 48)
        val s = product.snapshot()
        assertAll(
            { assertEquals(ProductType.LENDING, s.type) },
            { assertEquals("Convênio X", s.agreementName.value) },
            { assertEquals(6, s.minMonths) },
            { assertEquals(48, s.maxMonths) },
            { assertTrue(s.active) }
        )
    }

    @Test
    fun `must reject blank agreement name`() {
        assertAll(
            { assertThrows(IllegalArgumentException::class.java) { AgreementName("") } },
            { assertThrows(IllegalArgumentException::class.java) { AgreementName("   ") } }
        )
    }

    @Test
    fun `must reject blank display name`() {
        assertAll(
            { assertThrows(IllegalArgumentException::class.java) { DisplayName("") } },
            { assertThrows(IllegalArgumentException::class.java) { DisplayName("   ") } }
        )
    }

    @Test
    fun `must reject unknown product type from database value`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            ProductType.fromDatabaseValue("XPTO")
        }
        assertTrue(ex.message!!.contains("invalid_product_type"))
    }

    @Test
    fun `must resolve product type case-insensitively from database value`() {
        assertAll(
            { assertEquals(ProductType.LENDING, ProductType.fromDatabaseValue("lending")) },
            { assertEquals(ProductType.LENDING, ProductType.fromDatabaseValue("LENDING")) },
            { assertEquals(ProductType.LENDING, ProductType.fromDatabaseValue("Lending")) }
        )
    }
}