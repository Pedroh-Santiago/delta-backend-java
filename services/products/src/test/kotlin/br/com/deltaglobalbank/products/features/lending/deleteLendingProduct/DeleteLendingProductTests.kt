package br.com.deltaglobalbank.products.features.lending.deleteLendingProduct

import br.com.deltaglobalbank.products.TestcontainersConfiguration
import br.com.deltaglobalbank.products.domain.product.*
import br.com.deltaglobalbank.products.infrastructure.persistence.mappers.toEntity
import br.com.deltaglobalbank.products.infrastructure.persistence.repositories.JpaProductRepository
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
@Transactional
class DeleteLendingProductTests {

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
        createdBy: UUID? = null,
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

    @Test
    fun `soft deletes without removing the row`() {
        val id = seedProduct()
        mockMvc.delete("/products/lending/$id") {
            with(authentication(principal("products.lending.delete")))
        }.andExpect { status { isNoContent() } }

        // a linha AINDA existe, só inativa
        val entity = jpaProductRepository.findById(id).orElse(null)
        assertNotNull(entity)
        assertEquals(false, entity!!.active)
    }

    @Test
    fun `updates updated_at and updated_by on delete`() {
        val id = seedProduct(createdBy = UUID.randomUUID())
        val before = jpaProductRepository.findById(id).orElse(null)!!
        val originalUpdatedAt = before.updatedAt

        mockMvc.delete("/products/lending/$id") {
            with(authentication(principal("products.lending.delete")))
        }.andExpect { status { isNoContent() } }

        val after = jpaProductRepository.findById(id).orElse(null)!!
        assertTrue(after.updatedAt.isAfter(originalUpdatedAt) || after.updatedAt != originalUpdatedAt)
        assertEquals(subject, after.updatedBy)
    }

    @Test
    fun `does not physically remove the row`() {
        val id = seedProduct()
        val countBefore = jpaProductRepository.count()
        mockMvc.delete("/products/lending/$id") {
            with(authentication(principal("products.lending.delete")))
        }.andExpect { status { isNoContent() } }
        assertEquals(countBefore, jpaProductRepository.count())
    }

    @Test
    fun `returns 204 no content with empty body`() {
        val id = seedProduct()
        mockMvc.delete("/products/lending/$id") {
            with(authentication(principal("products.lending.delete")))
        }.andExpect {
            status { isNoContent() }
            content { string("") }
        }
    }

    @Test
    fun `disappears from active listing after delete`() {
        val id = seedProduct(agreementName = "INSS")
        mockMvc.delete("/products/lending/$id") {
            with(authentication(principal("products.lending.delete")))
        }.andExpect { status { isNoContent() } }

        mockMvc.get("/products/lending?active=true") {
            with(authentication(principal("products.lending.read")))
        }.andExpect {
            status { isOk() }
            jsonPath("$.items.length()") { value(0) }
        }
    }

    @Test
    fun `appears in inactive listing after delete`() {
        val id = seedProduct(agreementName = "INSS")
        mockMvc.delete("/products/lending/$id") {
            with(authentication(principal("products.lending.delete")))
        }.andExpect { status { isNoContent() } }

        mockMvc.get("/products/lending?active=false") {
            with(authentication(principal("products.lending.read")))
        }.andExpect {
            status { isOk() }
            jsonPath("$.items.length()") { value(1) }
            jsonPath("$.items[0].id") { value(id.toString()) }
        }
    }

    @Test
    fun `detail still accessible by id after delete`() {
        val id = seedProduct()
        mockMvc.delete("/products/lending/$id") {
            with(authentication(principal("products.lending.delete")))
        }.andExpect { status { isNoContent() } }

        mockMvc.get("/products/lending/$id") {
            with(authentication(principal("products.lending.read")))
        }.andExpect {
            status { isOk() }
            jsonPath("$.active") { value(false) }
        }
    }

    @Test
    fun `unfiltered listing still shows the inactive product`() {
        val id = seedProduct()
        mockMvc.delete("/products/lending/$id") {
            with(authentication(principal("products.lending.delete")))
        }.andExpect { status { isNoContent() } }

        mockMvc.get("/products/lending") {
            with(authentication(principal("products.lending.read")))
        }.andExpect {
            status { isOk() }
            jsonPath("$.items.length()") { value(1) }
            jsonPath("$.items[0].active") { value(false) }
        }
    }

    @Test
    fun `deleting twice returns 204 both times`() {
        val id = seedProduct()
        repeat(2) {
            mockMvc.delete("/products/lending/$id") {
                with(authentication(principal("products.lending.delete")))
            }.andExpect { status { isNoContent() } }
        }
    }

    @Test
    fun `deleting an already inactive product returns 204`() {
        val id = seedProduct(active = false)
        mockMvc.delete("/products/lending/$id") {
            with(authentication(principal("products.lending.delete")))
        }.andExpect { status { isNoContent() } }

        val entity = jpaProductRepository.findById(id).orElse(null)!!
        assertEquals(false, entity.active)
    }

    @Test
    fun `returns 404 for nonexistent product`() {
        mockMvc.delete("/products/lending/${UUID.randomUUID()}") {
            with(authentication(principal("products.lending.delete")))
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.error") { value("product_not_found") }
        }
    }

    @Test
    fun `returns 404 for product from another tenant`() {
        val idInB = seedProduct(tenant = UUID.randomUUID())
        mockMvc.delete("/products/lending/$idInB") {
            with(authentication(principal("products.lending.delete")))
        }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `returns 401 without token`() {
        mockMvc.delete("/products/lending/${UUID.randomUUID()}")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `returns 403 for insufficient role`() {
        val id = seedProduct()
        mockMvc.delete("/products/lending/$id") {
            with(authentication(principal("products.lending.read")))
        }.andExpect { status { isForbidden() } }
    }

    @Test
    fun `delete role allows deletion`() {
        val id = seedProduct()
        mockMvc.delete("/products/lending/$id") {
            with(authentication(principal("products.lending.delete")))
        }.andExpect { status { isNoContent() } }
    }

    @Test
    fun `admin role allows deletion`() {
        val id = seedProduct()
        mockMvc.delete("/products/lending/$id") {
            with(authentication(principal("products.lending.admin")))
        }.andExpect { status { isNoContent() } }
    }

    @Test
    fun `returns 403 for role from another module`() {
        val id = seedProduct()
        mockMvc.delete("/products/lending/$id") {
            with(authentication(principal("customers.admin")))
        }.andExpect { status { isForbidden() } }
    }

    @Test
    fun `deleting own product leaves other tenant product intact`() {
        val idA = seedProduct(tenant = tenantId, agreementName = "A")
        val idB = seedProduct(tenant = UUID.randomUUID(), agreementName = "B")

        mockMvc.delete("/products/lending/$idA") {
            with(authentication(principal("products.lending.delete")))
        }.andExpect { status { isNoContent() } }

        // o produto do B continua ativo
        val entityB = jpaProductRepository.findById(idB).orElse(null)!!
        assertEquals(true, entityB.active)
    }

    @Test
    fun `cannot delete another tenant product and it stays unchanged`() {
        val idB = seedProduct(tenant = UUID.randomUUID())

        mockMvc.delete("/products/lending/$idB") {
            with(authentication(principal("products.lending.delete")))
        }.andExpect { status { isNotFound() } }

        val entityB = jpaProductRepository.findById(idB).orElse(null)!!
        assertEquals(true, entityB.active)
    }

    @Test
    fun `platform admin deactivates product of a tenant via path`() {
        val tenantA = UUID.randomUUID()
        val id = seedProduct(tenant = tenantA)
        mockMvc.delete("/products/tenants/$tenantA/lending/$id") {
            with(authentication(principal("platform.admin")))
        }.andExpect { status { isNoContent() } }

        val entity = jpaProductRepository.findById(id).orElse(null)!!
        assertEquals(false, entity.active)
    }

    @Test
    fun `admin route returns 404 when product not in path tenant`() {
        val tenantA = UUID.randomUUID()
        val idInB = seedProduct(tenant = UUID.randomUUID())
        mockMvc.delete("/products/tenants/$tenantA/lending/$idInB") {
            with(authentication(principal("platform.admin")))
        }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `admin route returns 403 for non-platform-admin`() {
        val tenantA = UUID.randomUUID()
        val id = seedProduct(tenant = tenantA)
        mockMvc.delete("/products/tenants/$tenantA/lending/$id") {
            with(authentication(principal("products.lending.delete")))
        }.andExpect { status { isForbidden() } }
    }

    @Test
    fun `admin route returns 401 without token`() {
        mockMvc.delete("/products/tenants/${UUID.randomUUID()}/lending/${UUID.randomUUID()}")
            .andExpect { status { isUnauthorized() } }
    }
}