package br.com.deltaglobalbank.products.features.lending.listLendingProduct

import br.com.deltaglobalbank.products.TestcontainersConfiguration
import br.com.deltaglobalbank.products.domain.product.*
import br.com.deltaglobalbank.products.infrastructure.persistence.mappers.toEntity
import br.com.deltaglobalbank.products.infrastructure.persistence.repositories.JpaProductRepository
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.Test

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
@Transactional
class ListLendingProductsTests {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var jpaProductRepository: JpaProductRepository

    private val tenantId = UUID.randomUUID()

    private fun principal(vararg roles: String) = principalForTenant(tenantId, *roles)

    private fun principalForTenant(tenant: UUID, vararg roles: String) = JwtAuthenticationToken(
        AuthenticatedPrincipal(
            subject = UUID.randomUUID(), tenantId = tenant, principalType = "user",
            roles = roles.toList(), modules = emptyList(),
            mustChangePassword = false, jti = UUID.randomUUID(),
        )
    )

    private fun seedProduct(
        tenant: UUID = tenantId,
        agreementName: String = "INSS",
        active: Boolean = true,
    ): UUID {
        val id = UUID.randomUUID()
        val now = java.time.Instant.now()
        val product = Product(
            id = id, tenantId = tenant, type = ProductType.LENDING,
            agreementName = AgreementName(agreementName), displayName = DisplayName("Produto"),
            minMonthlyRate = BigDecimal("1.5"), maxMonthlyRate = BigDecimal("3.0"),
            minMonths = 12, maxMonths = 60,
            minAmount = BigDecimal("1000.00"), maxAmount = BigDecimal("50000.00"),
            commissionRate = BigDecimal("2.0"), active = active,
            createdAt = now, updatedAt = now, createdBy = null, updatedBy = null,
        )
        jpaProductRepository.save(product.toEntity())
        return id
    }

    @Test
    fun `returns all products with pagination wrapper`() {
        repeat(3) { seedProduct(agreementName = "INSS-$it") }
        mockMvc.get("/products/lending") {
            with(authentication(principal("products.lending.read")))
        }.andExpect {
            status { isOk() }
            jsonPath("$.items.length()") { value(3) }
            jsonPath("$.page") { value(0) }
            jsonPath("$.totalElements") { value(3) }
        }
    }

    @Test
    fun `paginates with page and size params`() {
        repeat(3) { seedProduct(agreementName = "INSS-$it") }
        mockMvc.get("/products/lending?page=0&size=2") {
            with(authentication(principal("products.lending.read")))
        }.andExpect {
            status { isOk() }
            jsonPath("$.items.length()") { value(2) }
            jsonPath("$.page") { value(0) }
            jsonPath("$.size") { value(2) }
            jsonPath("$.totalElements") { value(3) }
            jsonPath("$.totalPages") { value(2) }
        }
    }

    @Test
    fun `orders by createdAt descending`() {
        val firstId = seedProduct(agreementName = "AAA")
        Thread.sleep(10)
        val lastId = seedProduct(agreementName = "BBB")
        mockMvc.get("/products/lending") {
            with(authentication(principal("products.lending.read")))
        }.andExpect {
            status { isOk() }
            jsonPath("$.items[0].id") { value(lastId.toString()) }
        }
    }

    @Test
    fun `coerces size above max to 100`() {
        seedProduct()
        mockMvc.get("/products/lending?size=9999") {
            with(authentication(principal("products.lending.read")))
        }.andExpect {
            status { isOk() }
            jsonPath("$.size") { value(100) }
        }
    }

    @Test
    fun `uses default size 20 when omitted`() {
        seedProduct()
        mockMvc.get("/products/lending") {
            with(authentication(principal("products.lending.read")))
        }.andExpect {
            status { isOk() }
            jsonPath("$.size") { value(20) }
        }
    }

    @Test
    fun `returns 401 without token`() {
        mockMvc.get("/products/lending")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `returns empty list for tenant without products`() {
        mockMvc.get("/products/lending") {
            with(authentication(principal("products.lending.read")))
        }.andExpect {
            status { isOk() }
            jsonPath("$.items.length()") { value(0) }
            jsonPath("$.totalElements") { value(0) }
        }
    }

    @Test
    fun `filters by agreement name`() {
        seedProduct(agreementName = "INSS")
        seedProduct(agreementName = "SIAPE")
        mockMvc.get("/products/lending?agreementName=INSS") {
            with(authentication(principal("products.lending.read")))
        }.andExpect {
            status { isOk() }
            jsonPath("$.items.length()") { value(1) }
            jsonPath("$.items[0].agreementName") { value("INSS") }
        }
    }

    @Test
    fun `filters by agreement name case-insensitively`() {
        seedProduct(agreementName = "INSS")
        mockMvc.get("/products/lending?agreementName=inss") {
            with(authentication(principal("products.lending.read")))
        }.andExpect {
            status { isOk() }
            jsonPath("$.items.length()") { value(1) }
        }
    }

    @Test
    fun `filters by agreement name substring`() {
        seedProduct(agreementName = "INSS")
        mockMvc.get("/products/lending?agreementName=NS") {
            with(authentication(principal("products.lending.read")))
        }.andExpect {
            status { isOk() }
            jsonPath("$.items.length()") { value(1) }
            jsonPath("$.items[0].agreementName") { value("INSS") }
        }
    }

    @Test
    fun `filters active true`() {
        seedProduct(agreementName = "ATIVO", active = true)
        seedProduct(agreementName = "INATIVO", active = false)
        mockMvc.get("/products/lending?active=true") {
            with(authentication(principal("products.lending.read")))
        }.andExpect {
            status { isOk() }
            jsonPath("$.items.length()") { value(1) }
            jsonPath("$.items[0].active") { value(true) }
        }
    }

    @Test
    fun `filters active false`() {
        seedProduct(agreementName = "ATIVO", active = true)
        seedProduct(agreementName = "INATIVO", active = false)
        mockMvc.get("/products/lending?active=false") {
            with(authentication(principal("products.lending.read")))
        }.andExpect {
            status { isOk() }
            jsonPath("$.items.length()") { value(1) }
            jsonPath("$.items[0].active") { value(false) }
        }
    }

    @Test
    fun `returns both active and inactive when active filter is absent`() {
        seedProduct(agreementName = "ATIVO", active = true)
        seedProduct(agreementName = "INATIVO", active = false)
        mockMvc.get("/products/lending") {
            with(authentication(principal("products.lending.read")))
        }.andExpect {
            status { isOk() }
            jsonPath("$.items.length()") { value(2) }
        }
    }

    @Test
    fun `combines agreement name and active filters`() {
        seedProduct(agreementName = "INSS", active = true)
        seedProduct(agreementName = "INSS", active = false)
        seedProduct(agreementName = "SIAPE", active = true)
        mockMvc.get("/products/lending?agreementName=INSS&active=true") {
            with(authentication(principal("products.lending.read")))
        }.andExpect {
            status { isOk() }
            jsonPath("$.items.length()") { value(1) }
            jsonPath("$.items[0].agreementName") { value("INSS") }
            jsonPath("$.items[0].active") { value(true) }
        }
    }

    @Test
    fun `lists only own tenant products`() {
        val tenantA = tenantId
        val tenantB = UUID.randomUUID()
        seedProduct(tenant = tenantA, agreementName = "A1")
        seedProduct(tenant = tenantA, agreementName = "A2")
        seedProduct(tenant = tenantB, agreementName = "B1")
        mockMvc.get("/products/lending") {
            with(authentication(principal("products.lending.read")))
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalElements") { value(2) }
        }
    }

    @Test
    fun `platform admin lists products of a specific tenant`() {
        val tenantA = UUID.randomUUID()
        seedProduct(tenant = tenantA, agreementName = "A1")
        seedProduct(tenant = tenantA, agreementName = "A2")
        seedProduct(tenant = UUID.randomUUID(), agreementName = "OTHER")
        mockMvc.get("/products/tenants/$tenantA/lending") {
            with(authentication(principal("platform.admin")))
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalElements") { value(2) }
        }
    }
}