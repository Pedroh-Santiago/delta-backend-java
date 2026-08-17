package br.com.deltaglobalbank.customers.features.customers.createCustomer

import br.com.deltaglobalbank.customers.TestcontainersConfiguration
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaBankAccountRepository
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaCustomerRepository
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaPersonalDocumentRepository
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.springframework.http.MediaType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
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
class CreateCustomerTests {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var jpaCustomerRepository: JpaCustomerRepository
    @Autowired lateinit var jpaBankAccountRepository: JpaBankAccountRepository
    @Autowired lateinit var jpaPersonalDocumentRepository: JpaPersonalDocumentRepository

    private val tenantId = UUID.randomUUID()

    private fun principal(vararg roles: String) = JwtAuthenticationToken(
        AuthenticatedPrincipal(
            subject = UUID.randomUUID(),
            tenantId = tenantId,
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
            "cpf":"111.444.777-35", 
            "fullName":"João da Silva", 
            "birthDate":"1980-05-15",
            "gender":"male", 
            "nationality":"brasileira", 
            "motherName":"Maria da Silva",
            "maritalStatus":"married", 
            "email":"joao@exemplo.com",
            "phone":{
                "phoneNumber":"+5511999998888"
            },
            "address":{
                "cep":"01310100",
                "street":"Av. Paulista",
                "number":"1000",
                "complement":"Apto 101",
                "neighborhood":"Bela Vista",
                "city":"São Paulo",
                "state":"SP",
                "country":"BR"
            },
          "documents":[{
                "type":"rg",
                "number":"12.345.678-9",
                "issuer":"SSP",
                "issuerState":"SP",
                "issuedAt":"2010-01-15"
          }],
          "bankAccounts":[{
                "bankCode":"341",
                "agency":"1234",
                "accountNumber":"56789",
                "accountDigit":"0",
                "accountType":"checking",
                "purpose":"disbursement",
                "isPrimary":true
          }] 
        }
    """.trimIndent()

    @Test
    fun `creates customer and returns 201`() {
        mockMvc.post("/customers") {
            with(authentication(principal("customers.admin")))
            contentType = MediaType.APPLICATION_JSON
            content = validPayload
        }.andExpect {
            status { isCreated() }
            jsonPath("$.cpf") { value("11144477735") }
            jsonPath("$.tenantId") { value(tenantId.toString()) }
            jsonPath("$.status") { value("active") }
        }
        val saved = jpaCustomerRepository.findByCpfAndTenantId("11144477735", tenantId)
        assertNotNull(saved)
        assertEquals(1, jpaBankAccountRepository.findAllByCustomerId(saved!!.id).size)
        assertEquals(1, jpaPersonalDocumentRepository.findAllByCustomerId(saved.id).size)   // ← nova linha 72
    }

    @Test
    fun `returns 409 on duplicate cpf`() {
        mockMvc.post("/customers") { with(authentication(principal("customers.admin"))); contentType = MediaType.APPLICATION_JSON; content = validPayload }
            .andExpect { status { isCreated() } }
        mockMvc.post("/customers") { with(authentication(principal("customers.admin"))); contentType = MediaType.APPLICATION_JSON; content = validPayload }
            .andExpect { status { isConflict() }; jsonPath("$.error") { value("cpf_already_exists") } }
    }

    @Test
    fun `returns 400 on invalid cpf`() {
        val badCpf = validPayload.replace("111.444.777-35", "111.444.777-00")
        mockMvc.post("/customers") { with(authentication(principal("customers.admin"))); contentType = MediaType.APPLICATION_JSON; content = badCpf }
            .andExpect { status { isBadRequest() }; jsonPath("$.error") { value("invalid_cpf") } }
    }

    @Test
    fun `returns 403 for insufficient role`() {
        mockMvc.post("/customers") { with(authentication(principal("customers.viewer"))); contentType = MediaType.APPLICATION_JSON; content = validPayload }
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun `returns 401 without token`() {
        mockMvc.post("/customers") { contentType = MediaType.APPLICATION_JSON; content = validPayload }
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `returns 400 on multiple primary accounts for same purpose`() {
         val twoPrimaryPayload = """
        {
            "cpf":"111.444.777-35",
            "fullName":"João da Silva",
            "birthDate":"1980-05-15",
            "gender":"male",
            "nationality":"brasileira",
            "motherName":"Maria da Silva",
            "maritalStatus":"married",
            "email":"joao@exemplo.com",
            "phone":{ "phoneNumber":"+5511999998888" },
            "address":{
                "cep":"01310100",
                "street":"Av. Paulista",
                "number":"1000",
                "complement":"Apto 101",
                "neighborhood":"Bela Vista",
                "city":"São Paulo",
                "state":"SP",
                "country":"BR"
            },
            "documents":[],
            "bankAccounts":[
                {
                    "bankCode":"341",
                    "agency":"1234",
                    "accountNumber":"1",
                    "accountType":"checking",
                    "purpose":"disbursement",
                    "isPrimary":true
                },
                {
                    "bankCode":"341",
                    "agency":"1234",
                    "accountNumber":"2",
                    "accountType":"checking",
                    "purpose":"disbursement",
                    "isPrimary":true
                }
            ]
        }
    """.trimIndent()
        mockMvc.post("/customers") { with(authentication(principal("customers.admin"))); contentType = MediaType.APPLICATION_JSON; content = twoPrimaryPayload }
            .andExpect { status { isBadRequest() }; jsonPath("$.error") { value("multiple_primary_per_purpose") } }
    }
}