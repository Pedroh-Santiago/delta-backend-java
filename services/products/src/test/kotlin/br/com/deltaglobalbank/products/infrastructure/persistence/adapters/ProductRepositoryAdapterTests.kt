package br.com.deltaglobalbank.products.infrastructure.persistence.adapters

import br.com.deltaglobalbank.products.TestcontainersConfiguration
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import br.com.deltaglobalbank.products.domain.product.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ProductRepositoryAdapter::class, TestcontainersConfiguration::class)
@ActiveProfiles("test")
class ProductRepositoryAdapterTests {

    @Autowired
    lateinit var adapter: ProductRepositoryAdapter

    private fun buildProduct(
        tenantId: UUID = UUID.randomUUID(),
        type: ProductType = ProductType.LENDING,
        agreementName: String = "Convênio X",
        active: Boolean = true,
    ): Product {
        return Product(
            id = UUID.randomUUID(),
            tenantId = tenantId,
            type = type,
            agreementName = AgreementName(agreementName),
            displayName = DisplayName("Produto X"),
            minMonthlyRate = BigDecimal("1.5"),
            maxMonthlyRate = BigDecimal("3.0"),
            minMonths = 12, maxMonths = 60,
            minAmount = BigDecimal("1000.00"), maxAmount = BigDecimal("50000.00"),
            commissionRate = BigDecimal("2.0"),
            active = active,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            createdBy = UUID.randomUUID(),
            updatedBy = UUID.randomUUID(),
        )
    }

    @Test
    fun `must save and retrieve product by id and tenant`() {
        val tenantId = UUID.randomUUID()
        val product = buildProduct(tenantId = tenantId)

        adapter.save(product)
        val found = adapter.findById(product.id, tenantId)

        assertNotNull(found)
        assertEquals(product.id, found!!.id)
        assertEquals("Convênio X", found.snapshot().agreementName.value)
    }

    @Test
    fun `must return null when product not found`() {
        val found = adapter.findById(UUID.randomUUID(), UUID.randomUUID())
        assertNull(found)
    }

    @Test
    fun `must not find product from another tenant`() {
        val ownerTenant = UUID.randomUUID()
        val product = buildProduct(tenantId = ownerTenant)
        adapter.save(product)

        val found = adapter.findById(product.id, UUID.randomUUID())
        assertNull(found)
    }

    @Test
    fun `must detect active product with same type and agreement`() {
        val tenantId = UUID.randomUUID()
        adapter.save(buildProduct(tenantId = tenantId, agreementName = "Convênio Y"))

        val exists = adapter.existsActiveByTypeAndAgreement(tenantId, ProductType.LENDING, "Convênio Y")
        assertTrue(exists)
    }

    @Test
    fun `must be case insensitive on agreement name`() {
        val tenantId = UUID.randomUUID()
        adapter.save(buildProduct(tenantId = tenantId, agreementName = "Convênio Z"))

        val exists = adapter.existsActiveByTypeAndAgreement(tenantId, ProductType.LENDING, "convênio z")
        assertTrue(exists)
    }

    @Test
    fun `must isolate exists check by tenant`() {
        val tenantA = UUID.randomUUID()
        adapter.save(buildProduct(tenantId = tenantA, agreementName = "Convênio T"))

        val exists = adapter.existsActiveByTypeAndAgreement(UUID.randomUUID(), ProductType.LENDING, "Convênio T")
        assertFalse(exists)
    }

    @Test
    fun `must allow same agreement in different tenants`() {
        adapter.save(buildProduct(tenantId = UUID.randomUUID(), agreementName = "INSS"))

        assertDoesNotThrow {
            adapter.save(buildProduct(tenantId = UUID.randomUUID(), agreementName = "INSS"))  // outro tenant
        }
    }

    @Test
    fun `must round-trip all fields through persistence`() {
        val tenantId = UUID.randomUUID()
        val product = buildProduct(tenantId = tenantId, agreementName = "INSS")
        adapter.save(product)

        val found = adapter.findById(product.id, tenantId)!!
        val original = product.snapshot()
        val persisted = found.snapshot()
        assertAll(
            { assertEquals(original.id, persisted.id) },
            { assertEquals(original.tenantId, persisted.tenantId) },
            { assertEquals(original.type, persisted.type) },
            { assertEquals(original.agreementName.value, persisted.agreementName.value) },
            { assertEquals(original.minMonthlyRate, persisted.minMonthlyRate) },
            { assertEquals(original.minMonths, persisted.minMonths) },
            { assertEquals(original.commissionRate, persisted.commissionRate) },
            { assertEquals(original.active, persisted.active) }
        )
    }
}