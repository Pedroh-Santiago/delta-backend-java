package br.com.deltaglobalbank.products.features.lending.createLendingProduct

import br.com.deltaglobalbank.products.TestcontainersConfiguration
import br.com.deltaglobalbank.products.infrastructure.persistence.repositories.JpaProductRepository
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.util.UUID
import kotlin.test.Test

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
@Transactional
class CreateLendingProductTests {

    @Autowired
    lateinit var mockMvc: MockMvc
    @Autowired
    lateinit var jpaProductRepository: JpaProductRepository

    private val tenantId = UUID.randomUUID()
    private val subject = UUID.randomUUID()

    private fun principal(vararg roles: String) = principalForTenant(tenantId, subject, *roles)

    private fun principalForTenant(tenant: UUID, subj: UUID = UUID.randomUUID(), vararg roles: String) =
        JwtAuthenticationToken(
            AuthenticatedPrincipal(
                subject = subj,
                tenantId = tenant,
                principalType = "user",
                roles = roles.toList(),
                modules = emptyList(),
                mustChangePassword = false,
                jti = UUID.randomUUID(),
            )
        )

    private val validPayload =
        """
        {
            "agreementName": "INSS",
            "displayName": "Consignado INSS Premium",
            "minMonthlyRate": 1.5,
            "maxMonthlyRate": 3.0,
            "minMonths": 12,
            "maxMonths": 60,
            "minAmount": 1000.00,
            "maxAmount": 50000.00,
            "commissionRate": 2.0
        }
        """.trimIndent()

    @Test
    fun `creates lending product and returns 201`() {
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("products.lending.create")))
            contentType = MediaType.APPLICATION_JSON
            content = validPayload
        }.andExpect {
            status { isCreated() }
            jsonPath("$.type") { value("LENDING") }
            jsonPath("$.active") { value(true) }
            jsonPath("$.tenantId") { value(tenantId.toString()) }
        }

        val saved = jpaProductRepository
            .existsByTenantIdAndTypeAndAgreementNameIgnoreCaseAndActiveTrue(tenantId, "LENDING", "INSS")
        Assertions.assertEquals(true, saved)
    }

    @Test
    fun `uses default display name when omitted`() {
        val noDisplayName = validPayload.replace("\"displayName\": \"Consignado INSS Premium\",", "")
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("products.lending.create")))
            contentType = MediaType.APPLICATION_JSON
            content = noDisplayName
        }.andExpect {
            status { isCreated() }
            jsonPath("$.displayName") { value("Consignado INSS") }
        }
    }

    @Test
    fun `accepts null commission rate`() {
        val noCommission = validPayload.replace("\"commissionRate\": 2.0", "\"commissionRate\": null")
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("products.lending.create")))
            contentType = MediaType.APPLICATION_JSON
            content = noCommission
        }.andExpect {
            status { isCreated() }
            jsonPath("$.commissionRate") { value(null) }
        }
    }

    @Test
    fun `accepts the admin role as well`() {
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("products.lending.admin")))
            contentType = MediaType.APPLICATION_JSON
            content = validPayload
        }.andExpect {
            status { isCreated() }
        }
    }

    @Test
    fun `persists LENDING type even if body sends another type`() {
        val withExtraType = validPayload.replace(
            "\"agreementName\": \"INSS\",",
            "\"agreementName\": \"INSS\",\n            \"type\": \"CREDIT_CARD\","
        )
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("products.lending.create")))
            contentType = MediaType.APPLICATION_JSON
            content = withExtraType
        }.andExpect {
            status { isCreated() }
            jsonPath("$.type") { value("LENDING") }
        }
    }

    @Test
    fun `accepts valid boundary values`() {
        val boundary = validPayload
            .replace("\"minMonthlyRate\": 1.5", "\"minMonthlyRate\": 2.0")
            .replace("\"maxMonthlyRate\": 3.0", "\"maxMonthlyRate\": 2.0")
            .replace("\"minMonths\": 12", "\"minMonths\": 1")
            .replace("\"maxMonths\": 60", "\"maxMonths\": 1")
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("products.lending.create")))
            contentType = MediaType.APPLICATION_JSON
            content = boundary
        }.andExpect {
            status { isCreated() }
        }
    }

    @Test
    fun `returns 400 on blank agreement name`() {
        val blank = validPayload.replace("\"agreementName\": \"INSS\"", "\"agreementName\": \"\"")
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("products.lending.create")))
            contentType = MediaType.APPLICATION_JSON
            content = blank
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `returns 400 when min monthly rate is missing`() {
        val missing = validPayload.replace("\"minMonthlyRate\": 1.5,", "")
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("products.lending.create")))
            contentType = MediaType.APPLICATION_JSON
            content = missing
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `returns 400 on negative min monthly rate`() {
        val negative = validPayload.replace("\"minMonthlyRate\": 1.5", "\"minMonthlyRate\": -1.0")
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("products.lending.create")))
            contentType = MediaType.APPLICATION_JSON
            content = negative
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `returns 400 on non-positive min months`() {
        val zero = validPayload.replace("\"minMonths\": 12", "\"minMonths\": 0")
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("products.lending.create")))
            contentType = MediaType.APPLICATION_JSON
            content = zero
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `returns 400 when max monthly rate below min`() {
        val invalid = validPayload
            .replace("\"minMonthlyRate\": 1.5", "\"minMonthlyRate\": 3.0")
            .replace("\"maxMonthlyRate\": 3.0", "\"maxMonthlyRate\": 2.0")
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("products.lending.create")))
            contentType = MediaType.APPLICATION_JSON
            content = invalid
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("max_monthly_rate_lt_min") }
        }
    }

    @Test
    fun `returns 400 when max months below min`() {
        val invalid = validPayload
            .replace("\"minMonths\": 12", "\"minMonths\": 60")
            .replace("\"maxMonths\": 60", "\"maxMonths\": 12")
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("products.lending.create")))
            contentType = MediaType.APPLICATION_JSON
            content = invalid
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("max_months_lt_min") }
        }
    }

    @Test
    fun `returns 400 when max amount below min`() {
        val invalid = validPayload
            .replace("\"minAmount\": 1000.00", "\"minAmount\": 50000.00")
            .replace("\"maxAmount\": 50000.00", "\"maxAmount\": 1000.00")
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("products.lending.create")))
            contentType = MediaType.APPLICATION_JSON
            content = invalid
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("max_amount_lt_min") }
        }
    }

    @Test
    fun `returns 400 on negative commission rate`() {
        val negative = validPayload.replace("\"commissionRate\": 2.0", "\"commissionRate\": -1.0")
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("products.lending.create")))
            contentType = MediaType.APPLICATION_JSON
            content = negative
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `returns 400 on malformed json`() {
        val malformed = validPayload.replace("\"minMonths\": 12,", "\"minMonths\": 12,,")  // vírgula sobrando
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("products.lending.create")))
            contentType = MediaType.APPLICATION_JSON
            content = malformed
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("malformed_request") }
        }
    }

    @Test
    fun `returns 400 on wrong field type`() {
        val wrongType = validPayload.replace("\"minMonths\": 12", "\"minMonths\": \"abc\"")
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("products.lending.create")))
            contentType = MediaType.APPLICATION_JSON
            content = wrongType
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `returns 401 without token`() {
        mockMvc.post("/products/lending") {
            contentType = MediaType.APPLICATION_JSON
            content = validPayload
        }.andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `returns 403 for insufficient role`() {
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("products.lending.viewer")))
            contentType = MediaType.APPLICATION_JSON
            content = validPayload
        }.andExpect { status { isForbidden() } }
    }

    @Test
    fun `returns 403 for no roles`() {
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal()))
            contentType = MediaType.APPLICATION_JSON
            content = validPayload
        }.andExpect { status { isForbidden() } }
    }

    @Test
    fun `returns 403 for role from another module`() {
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("customers.admin")))
            contentType = MediaType.APPLICATION_JSON
            content = validPayload
        }.andExpect { status { isForbidden() } }
    }

    @Test
    fun `returns 409 on duplicate active product`() {
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("products.lending.create")))
            contentType = MediaType.APPLICATION_JSON
            content = validPayload
        }.andExpect { status { isCreated() } }

        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("products.lending.create")))
            contentType = MediaType.APPLICATION_JSON
            content = validPayload
        }.andExpect {
            status { isConflict() }
            jsonPath("$.error") { value("duplicate_active_product") }
        }
    }

    @Test
    fun `enforces uniqueness case-insensitively`() {
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("products.lending.create")))
            contentType = MediaType.APPLICATION_JSON
            content = validPayload   // "INSS"
        }.andExpect { status { isCreated() } }

        val lowerCase = validPayload.replace("\"agreementName\": \"INSS\"", "\"agreementName\": \"inss\"")
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("products.lending.create")))
            contentType = MediaType.APPLICATION_JSON
            content = lowerCase   // "inss"
        }.andExpect { status { isConflict() } }
    }

    @Test
    fun `allows same agreement in different tenants`() {
        val tenantA = UUID.randomUUID()
        val tenantB = UUID.randomUUID()

        mockMvc.post("/products/lending") {
            with(
                SecurityMockMvcRequestPostProcessors.authentication(
                    principalForTenant(
                        tenantA,
                        roles = arrayOf("products.lending.create")
                    )
                )
            )
            contentType = MediaType.APPLICATION_JSON
            content = validPayload
        }.andExpect { status { isCreated() } }

        mockMvc.post("/products/lending") {
            with(
                SecurityMockMvcRequestPostProcessors.authentication(
                    principalForTenant(
                        tenantB,
                        roles = arrayOf("products.lending.create")
                    )
                )
            )
            contentType = MediaType.APPLICATION_JSON
            content = validPayload
        }.andExpect { status { isCreated() } }
    }

    @Test
    fun `ignores tenant id from body and uses token tenant`() {
        val fakeTenant = UUID.randomUUID()
        val withFakeTenant = validPayload.replace(
            "\"agreementName\": \"INSS\",",
            "\"agreementName\": \"INSS\",\n            \"tenantId\": \"$fakeTenant\","
        )
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("products.lending.create")))
            contentType = MediaType.APPLICATION_JSON
            content = withFakeTenant
        }.andExpect {
            status { isCreated() }
            jsonPath("$.tenantId") { value(tenantId.toString()) }
        }

        Assertions.assertEquals(
            true, jpaProductRepository
                .existsByTenantIdAndTypeAndAgreementNameIgnoreCaseAndActiveTrue(tenantId, "LENDING", "INSS")
        )
        Assertions.assertEquals(
            false, jpaProductRepository
                .existsByTenantIdAndTypeAndAgreementNameIgnoreCaseAndActiveTrue(fakeTenant, "LENDING", "INSS")
        )
    }

    @Test
    fun `response contains all contract fields`() {
        mockMvc.post("/products/lending") {
            with(SecurityMockMvcRequestPostProcessors.authentication(principal("products.lending.create")))
            contentType = MediaType.APPLICATION_JSON
            content = validPayload
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { exists() }
            jsonPath("$.type") { value("LENDING") }
            jsonPath("$.agreementName") { value("INSS") }
            jsonPath("$.minMonthlyRate") { value(1.5) }
            jsonPath("$.maxMonthlyRate") { value(3.0) }
            jsonPath("$.minMonths") { value(12) }
            jsonPath("$.maxMonths") { value(60) }
            jsonPath("$.active") { value(true) }
            jsonPath("$.createdAt") { exists() }
        }
    }
}