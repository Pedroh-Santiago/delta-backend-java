package br.com.deltaglobalbank.products.features.lending.getLendingProduct

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
class GetLendingProductTests {

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
        tenant: UUID,
        agreementName: String = "INSS",
        active: Boolean = true,
    ): UUID {
        val product = Product.newProduct(
            id = UUID.randomUUID(), tenantId = tenant, type = ProductType.LENDING,
            agreementName = AgreementName(agreementName), displayName = DisplayName("Produto"),
            minMonthlyRate = BigDecimal("1.5"), maxMonthlyRate = BigDecimal("3.0"),
            minMonths = 12, maxMonths = 60,
            minAmount = BigDecimal("1000.00"), maxAmount = BigDecimal("50000.00"),
            commissionRate = BigDecimal("2.0"), createdBy = UUID.randomUUID(),
        )
        jpaProductRepository.save(product.toEntity())
        return product.id
    }

    @Test
    fun `returns 200 with full product for own tenant`() {
        val id = seedProduct(tenantId)
        mockMvc.get("/products/lending/$id") {
            with(authentication(principal("products.lending.read")))
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(id.toString()) }
            jsonPath("$.type") { value("LENDING") }
            jsonPath("$.agreementName") { value("INSS") }
        }
    }

    @Test
    fun `returns 404 for nonexistent product`() {
        mockMvc.get("/products/lending/${UUID.randomUUID()}") {
            with(authentication(principal("products.lending.read")))
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.error") { value("product_not_found") }
        }
    }

    @Test
    fun `returns 404 for product from another tenant`() {
        val tenantB = UUID.randomUUID()
        val idInB = seedProduct(tenantB)   // produto no tenant B
        mockMvc.get("/products/lending/$idInB") {
            with(authentication(principal("products.lending.read")))
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `returns 401 without token`() {
        mockMvc.get("/products/lending/${UUID.randomUUID()}")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `returns 403 for insufficient role`() {
        val id = seedProduct(tenantId)
        mockMvc.get("/products/lending/$id") {
            with(authentication(principal("customers.admin")))
        }.andExpect { status { isForbidden() } }
    }

    @Test
    fun `admin role also accesses`() {
        val id = seedProduct(tenantId)
        mockMvc.get("/products/lending/$id") {
            with(authentication(principal("products.lending.admin")))
        }.andExpect { status { isOk() } }
    }

    @Test
    fun `platform admin gets product via admin route`() {
        val tenantA = UUID.randomUUID()
        val id = seedProduct(tenantA)
        mockMvc.get("/products/tenants/$tenantA/lending/$id") {
            with(authentication(principal("platform.admin")))
        }.andExpect { status { isOk() } }
    }

    @Test
    fun `admin route returns 404 when path tenant is not the owner`() {
        val tenantA = UUID.randomUUID()
        val tenantB = UUID.randomUUID()
        val idInB = seedProduct(tenantB)
        mockMvc.get("/products/tenants/$tenantA/lending/$idInB") {
            with(authentication(principal("platform.admin")))
        }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `admin route returns 403 for non-platform-admin`() {
        val tenantA = UUID.randomUUID()
        val id = seedProduct(tenantA)
        mockMvc.get("/products/tenants/$tenantA/lending/$id") {
            with(authentication(principal("products.lending.read")))
        }.andExpect { status { isForbidden() } }
    }

    @Test
    fun `admin route returns 401 without token`() {
        mockMvc.get("/products/tenants/${UUID.randomUUID()}/lending/${UUID.randomUUID()}")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `platform admin accesses tenant different from own jwt`() {
        val ownTenant = UUID.randomUUID()
        val targetTenant = UUID.randomUUID()
        val id = seedProduct(targetTenant)
        mockMvc.get("/products/tenants/$targetTenant/lending/$id") {
            with(authentication(principalForTenant(ownTenant, "platform.admin")))
        }.andExpect { status { isOk() } }
    }
}