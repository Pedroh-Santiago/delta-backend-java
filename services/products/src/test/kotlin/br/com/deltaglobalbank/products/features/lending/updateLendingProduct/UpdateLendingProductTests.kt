package br.com.deltaglobalbank.products.features.lending.updateLendingProduct

import br.com.deltaglobalbank.products.TestcontainersConfiguration
import br.com.deltaglobalbank.products.domain.product.*
import br.com.deltaglobalbank.products.infrastructure.persistence.mappers.toEntity
import br.com.deltaglobalbank.products.infrastructure.persistence.repositories.JpaProductRepository
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.put
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.test.Test

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
@Transactional
class UpdateLendingProductTests {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var jpaProductRepository: JpaProductRepository

    private val tenantId = UUID.randomUUID()
    private val subject = UUID.randomUUID()

    private fun principal(vararg roles: String) = principalForTenant(tenantId, subject, *roles)

    private fun principalForTenant(tenant: UUID, subj: UUID = UUID.randomUUID(), vararg roles: String) =
        JwtAuthenticationToken(
            AuthenticatedPrincipal(
                subject = subj, tenantId = tenant, principalType = "user",
                roles = roles.toList(), modules = emptyList(),
                mustChangePassword = false, jti = UUID.randomUUID(),
            )
        )

    private fun seedProduct(
        tenant: UUID = tenantId,
        agreementName: String = "INSS",
        active: Boolean = true,
        createdBy: UUID? = UUID.randomUUID(),
    ): UUID {
        val id = UUID.randomUUID()
        val now = Instant.now()
        val product = Product(
            id = id, tenantId = tenant, type = ProductType.LENDING,
            agreementName = AgreementName(agreementName), displayName = DisplayName("Produto"),
            minMonthlyRate = BigDecimal("1.5"), maxMonthlyRate = BigDecimal("3.0"),
            minMonths = 12, maxMonths = 60,
            minAmount = BigDecimal("1000.00"), maxAmount = BigDecimal("50000.00"),
            commissionRate = BigDecimal("2.0"), active = active,
            createdAt = now, updatedAt = now, createdBy = createdBy, updatedBy = createdBy,
        )
        jpaProductRepository.save(product.toEntity())
        return id
    }

    private fun payload(
        agreementName: String = "INSS",
        active: Boolean = true,
        minMonthlyRate: String = "2.0",
        maxMonthlyRate: String = "4.0",
        minMonths: Int = 6,
        maxMonths: Int = 48,
        minAmount: String = "2000.00",
        maxAmount: String = "60000.00",
        commissionRate: String? = "1.0",
        displayName: String? = "Consignado Custom",
    ): String {
        val displayLine = displayName?.let { "\"displayName\": \"$it\"," } ?: ""
        val commissionLine = commissionRate?.let { "\"commissionRate\": $it" } ?: "\"commissionRate\": null"
        return """
        {
            "agreementName": "$agreementName",
            $displayLine
            "minMonthlyRate": $minMonthlyRate,
            "maxMonthlyRate": $maxMonthlyRate,
            "minMonths": $minMonths,
            "maxMonths": $maxMonths,
            "minAmount": $minAmount,
            "maxAmount": $maxAmount,
            "active": $active,
            $commissionLine
        }
        """.trimIndent()
    }

    @Test
    fun `updates rate term and amount of own tenant product`() {
        val id = seedProduct()
        mockMvc.put("/products/lending/$id") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = payload(minMonthlyRate = "2.5", minMonths = 24)
        }.andExpect {
            status { isOk() }
            jsonPath("$.minMonthlyRate") { value(2.5) }
            jsonPath("$.minMonths") { value(24) }
        }
        val saved = jpaProductRepository.findById(id).orElse(null)!!
        assertEquals(0, BigDecimal("2.5").compareTo(saved.minMonthlyRate))   // compareTo p/ escala
        assertEquals(24, saved.minMonths)
    }

    @Test
    fun `updates updated_at and updated_by`() {
        val id = seedProduct()
        val before = jpaProductRepository.findById(id).orElse(null)!!.updatedAt
        mockMvc.put("/products/lending/$id") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = payload()
        }.andExpect { status { isOk() } }
        val after = jpaProductRepository.findById(id).orElse(null)!!
        assertNotEquals(before, after.updatedAt)
        assertEquals(subject, after.updatedBy)
    }

    @Test
    fun `preserves created_at and created_by`() {
        val creator = UUID.randomUUID()
        val id = seedProduct(createdBy = creator)
        val before = jpaProductRepository.findById(id).orElse(null)!!
        val originalCreatedAt = before.createdAt

        mockMvc.put("/products/lending/$id") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = payload()
        }.andExpect { status { isOk() } }

        val after = jpaProductRepository.findById(id).orElse(null)!!
        assertEquals(originalCreatedAt, after.createdAt)
        assertEquals(creator, after.createdBy)
    }

    @Test
    fun `response matches full contract`() {
        val id = seedProduct()
        mockMvc.put("/products/lending/$id") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = payload()
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(id.toString()) }
            jsonPath("$.type") { value("LENDING") }
            jsonPath("$.createdAt") { exists() }
            jsonPath("$.updatedAt") { exists() }
        }
    }

    @Test
    fun `uses default display name when omitted`() {
        val id = seedProduct()
        mockMvc.put("/products/lending/$id") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = payload(agreementName = "INSS", displayName = null)
        }.andExpect {
            status { isOk() }
            jsonPath("$.displayName") { value("Consignado INSS") }
        }
    }

    @Test
    fun `admin role also updates`() {
        val id = seedProduct()
        mockMvc.put("/products/lending/$id") {
            with(authentication(principal("products.lending.admin")))
            contentType = MediaType.APPLICATION_JSON
            content = payload()
        }.andExpect { status { isOk() } }
    }

    @Test
    fun `keeps LENDING type even if body sends another type`() {
        val id = seedProduct()
        val withType = payload().replace(
            "\"agreementName\": \"INSS\",",
            "\"agreementName\": \"INSS\",\n            \"type\": \"CREDIT_CARD\","
        )
        mockMvc.put("/products/lending/$id") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = withType
        }.andExpect {
            status { isOk() }
            jsonPath("$.type") { value("LENDING") }
        }
    }

    @Test
    fun `returns 400 when max monthly rate below min`() {
        val id = seedProduct()
        mockMvc.put("/products/lending/$id") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = payload(minMonthlyRate = "3.0", maxMonthlyRate = "2.0")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("max_monthly_rate_lt_min") }
        }
    }

    @Test
    fun `returns 400 when max months below min`() {
        val id = seedProduct()
        mockMvc.put("/products/lending/$id") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = payload(minMonths = 60, maxMonths = 12)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("max_months_lt_min") }
        }
    }

    @Test
    fun `returns 400 when max amount below min`() {
        val id = seedProduct()
        mockMvc.put("/products/lending/$id") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = payload(minAmount = "50000.00", maxAmount = "1000.00")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("max_amount_lt_min") }
        }
    }

    @Test
    fun `returns 400 on negative min monthly rate`() {
        val id = seedProduct()
        mockMvc.put("/products/lending/$id") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = payload(minMonthlyRate = "-1.0")
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `returns 400 on zero min months`() {
        val id = seedProduct()
        mockMvc.put("/products/lending/$id") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = payload(minMonths = 0)
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `returns 400 on blank agreement name`() {
        val id = seedProduct()
        mockMvc.put("/products/lending/$id") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = payload(agreementName = "")
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `returns 400 on negative commission rate`() {
        val id = seedProduct()
        mockMvc.put("/products/lending/$id") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = payload(commissionRate = "-1.0")
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `returns 400 when required field is missing`() {
        val id = seedProduct()
        val missing = payload().replace("\"minMonthlyRate\": 2.0,", "")
        mockMvc.put("/products/lending/$id") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = missing
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `allows keeping same agreement name when editing only the rate`() {
        val id = seedProduct(agreementName = "INSS")
        mockMvc.put("/products/lending/$id") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = payload(agreementName = "INSS", minMonthlyRate = "2.5")
        }.andExpect { status { isOk() } }
    }

    @Test
    fun `returns 409 when changing agreement to one already active`() {
        seedProduct(agreementName = "SIAPE")
        val id = seedProduct(agreementName = "INSS")
        mockMvc.put("/products/lending/$id") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = payload(agreementName = "SIAPE")
        }.andExpect {
            status { isConflict() }
            jsonPath("$.error") { value("duplicate_active_product") }
        }
    }

    @Test
    fun `allows changing agreement to a name that is only inactive elsewhere`() {
        seedProduct(agreementName = "SIAPE", active = false)
        val id = seedProduct(agreementName = "INSS")
        mockMvc.put("/products/lending/$id") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = payload(agreementName = "SIAPE")
        }.andExpect { status { isOk() } }
    }

    @Test
    fun `enforces uniqueness case-insensitively on update`() {
        seedProduct(agreementName = "SIAPE")
        val id = seedProduct(agreementName = "INSS")
        mockMvc.put("/products/lending/$id") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = payload(agreementName = "siape")
        }.andExpect { status { isConflict() } }
    }

    @Test
    fun `returns 409 when reactivating with a colliding name`() {
        seedProduct(agreementName = "INSS", active = true)
        val y = seedProduct(agreementName = "INSS", active = false)
        mockMvc.put("/products/lending/$y") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = payload(agreementName = "INSS", active = true)
        }.andExpect {
            status { isConflict() }
        }
    }

    @Test
    fun `allows setting inactive even with a colliding name`() {
        seedProduct(agreementName = "INSS", active = true)
        val id = seedProduct(agreementName = "INSS", active = false)
        mockMvc.put("/products/lending/$id") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = payload(agreementName = "INSS", active = false)
        }.andExpect { status { isOk() } }
    }

    @Test
    fun `returns 404 for nonexistent product`() {
        mockMvc.put("/products/lending/${UUID.randomUUID()}") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = payload()
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.error") { value("product_not_found") }
        }
    }

    @Test
    fun `returns 404 for product from another tenant`() {
        val idInB = seedProduct(tenant = UUID.randomUUID())
        mockMvc.put("/products/lending/$idInB") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = payload()
        }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `updating own product does not affect other tenant product`() {
        val idA = seedProduct(tenant = tenantId, agreementName = "A")
        val idB = seedProduct(tenant = UUID.randomUUID(), agreementName = "B")
        mockMvc.put("/products/lending/$idA") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = payload(agreementName = "A", minMonthlyRate = "2.9")
        }.andExpect { status { isOk() } }

        val entityB = jpaProductRepository.findById(idB).orElse(null)!!
        assertEquals(0, BigDecimal("1.5").compareTo(entityB.minMonthlyRate))
    }

    @Test
    fun `returns 401 without token`() {
        mockMvc.put("/products/lending/${UUID.randomUUID()}") {
            contentType = MediaType.APPLICATION_JSON
            content = payload()
        }.andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `returns 403 for insufficient role`() {
        val id = seedProduct()
        mockMvc.put("/products/lending/$id") {
            with(authentication(principal("products.lending.read")))
            contentType = MediaType.APPLICATION_JSON
            content = payload()
        }.andExpect { status { isForbidden() } }
    }

    @Test
    fun `returns 403 for role from another module`() {
        val id = seedProduct()
        mockMvc.put("/products/lending/$id") {
            with(authentication(principal("customers.admin")))
            contentType = MediaType.APPLICATION_JSON
            content = payload()
        }.andExpect { status { isForbidden() } }
    }

    @Test
    fun `platform admin updates product via admin route`() {
        val tenantA = UUID.randomUUID()
        val id = seedProduct(tenant = tenantA)
        mockMvc.put("/products/tenants/$tenantA/lending/$id") {
            with(authentication(principal("platform.admin")))
            contentType = MediaType.APPLICATION_JSON
            content = payload()
        }.andExpect { status { isOk() } }
    }

    @Test
    fun `admin route returns 404 when product not in path tenant`() {
        val tenantA = UUID.randomUUID()
        val idInB = seedProduct(tenant = UUID.randomUUID())
        mockMvc.put("/products/tenants/$tenantA/lending/$idInB") {
            with(authentication(principal("platform.admin")))
            contentType = MediaType.APPLICATION_JSON
            content = payload()
        }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `admin route returns 403 for non-platform-admin`() {
        val tenantA = UUID.randomUUID()
        val id = seedProduct(tenant = tenantA)
        mockMvc.put("/products/tenants/$tenantA/lending/$id") {
            with(authentication(principal("products.lending.update")))
            contentType = MediaType.APPLICATION_JSON
            content = payload()
        }.andExpect { status { isForbidden() } }
    }

    @Test
    fun `admin route returns 401 without token`() {
        mockMvc.put("/products/tenants/${UUID.randomUUID()}/lending/${UUID.randomUUID()}") {
            contentType = MediaType.APPLICATION_JSON
            content = payload()
        }.andExpect { status { isUnauthorized() } }
    }
}