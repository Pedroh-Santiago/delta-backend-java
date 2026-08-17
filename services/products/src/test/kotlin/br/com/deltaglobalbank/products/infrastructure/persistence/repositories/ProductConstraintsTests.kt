package br.com.deltaglobalbank.products.infrastructure.persistence

import br.com.deltaglobalbank.products.TestcontainersConfiguration
import br.com.deltaglobalbank.products.infrastructure.persistence.entities.ProductEntity
import br.com.deltaglobalbank.products.infrastructure.persistence.repositories.JpaProductRepository
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.test.Test

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration::class)
@ActiveProfiles("test")
class ProductConstraintsTests {

    @Autowired
    lateinit var jpa: JpaProductRepository

    private fun malformedEntity(
        minMonthlyRate: BigDecimal = BigDecimal("1.5"),
        maxMonthlyRate: BigDecimal = BigDecimal("3.0"),
        minMonths: Int = 12,
        maxMonths: Int = 60,
        minAmount: BigDecimal = BigDecimal("1000"),
        maxAmount: BigDecimal = BigDecimal("50000"),
        commissionRate: BigDecimal? = BigDecimal("2.0"),
    ) = ProductEntity(
        id = UUID.randomUUID(), tenantId = UUID.randomUUID(), type = "LENDING",
        agreementName = "INSS", displayName = "P",
        minMonthlyRate = minMonthlyRate, maxMonthlyRate = maxMonthlyRate,
        minMonths = minMonths, maxMonths = maxMonths,
        minAmount = minAmount, maxAmount = maxAmount,
        commissionRate = commissionRate, active = true,
        createdAt = Instant.now(), updatedAt = Instant.now(),
        createdBy = null, updatedBy = null
    )

    @Test
    fun `must reject negative min monthly rate at db`() {
        assertThrows(DataIntegrityViolationException::class.java) {
            jpa.saveAndFlush(malformedEntity(minMonthlyRate = BigDecimal("-1")))
        }
    }
    @Test fun `must reject max monthly rate below min at db`() {
        assertThrows(DataIntegrityViolationException::class.java) {
            jpa.saveAndFlush(malformedEntity(minMonthlyRate = BigDecimal("3"), maxMonthlyRate = BigDecimal("2")))
        }
    }
    @Test fun `must reject min months below 1 at db`() {
        assertThrows(DataIntegrityViolationException::class.java) {
            jpa.saveAndFlush(malformedEntity(minMonths = 0))
        }
    }
    @Test fun `must reject max months below min at db`() {
        assertThrows(DataIntegrityViolationException::class.java) {
            jpa.saveAndFlush(malformedEntity(minMonths = 60, maxMonths = 12))
        }
    }
    @Test fun `must reject negative min amount at db`() {
        assertThrows(DataIntegrityViolationException::class.java) {
            jpa.saveAndFlush(malformedEntity(minAmount = BigDecimal("-1")))
        }
    }
    @Test fun `must reject max amount below min at db`() {
        assertThrows(DataIntegrityViolationException::class.java) {
            jpa.saveAndFlush(malformedEntity(minAmount = BigDecimal("50000"), maxAmount = BigDecimal("1000")))
        }
    }
    @Test fun `must reject negative commission rate at db`() {
        assertThrows(DataIntegrityViolationException::class.java) {
            jpa.saveAndFlush(malformedEntity(commissionRate = BigDecimal("-1")))
        }
    }
    @Test fun `must allow null commission rate at db`() {
        assertDoesNotThrow {
            jpa.saveAndFlush(malformedEntity(commissionRate = null))
        }
    }
}