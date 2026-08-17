package br.com.deltaglobalbank.customers.features.customers.getCustomer

import br.com.deltaglobalbank.customers.TestcontainersConfiguration
import br.com.deltaglobalbank.customers.domain.bankAccount.*
import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.*
import br.com.deltaglobalbank.customers.domain.customer.Customer
import br.com.deltaglobalbank.customers.domain.customer.CustomerRepository
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.*
import br.com.deltaglobalbank.customers.domain.document.DocumentType
import br.com.deltaglobalbank.customers.domain.document.PersonalDocument
import br.com.deltaglobalbank.customers.domain.shared.valueobjects.Uf
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaDocumentIssuerRepository
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken
import com.github.f4b6a3.uuid.UuidCreator
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
@Transactional
@ActiveProfiles("test")
class GetCustomerTests {
    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var customerRepository: CustomerRepository
    @Autowired lateinit var jpaDocumentIssuerRepository: JpaDocumentIssuerRepository

    private val tenantId = UUID.randomUUID()

    private fun principal(vararg roles: String, tenant: UUID = tenantId) = JwtAuthenticationToken(
        AuthenticatedPrincipal(UUID.randomUUID(), tenant, "user", roles.toList(), emptyList(), false, UUID.randomUUID())
    )

    private fun seedCustomer(): Customer {
        val issuerId = jpaDocumentIssuerRepository.findByName("SSP")!!.id
        val c = Customer.create(
            id = UuidCreator.getTimeOrderedEpoch(), tenantId = tenantId,
            cpf = Cpf("11144477735"), fullName = FullName("João da Silva"),
            birthDate = BirthDate(LocalDate.of(1980, 5, 15)), gender = Gender.MALE,
            motherName = MotherName("Maria da Silva"), maritalStatus = MaritalStatus.MARRIED,
            email = Email("joao@exemplo.com"), phoneNumber = Phone("+5511999998888"),
            address = Address("01310100", "Av Paulista", "São Paulo", Uf("SP")),
            createdBy = UUID.randomUUID(), bankAccounts = emptyList(), documents = emptyList()
        )
        c.addBankAccount(BankAccount.create(UuidCreator.getTimeOrderedEpoch(), c.id, BankCode("341"), Agency("1234"),
            "56789", "0", BankAccountType.CHECKING, BankAccountPurpose.DISBURSEMENT, true), UUID.randomUUID())
        c.replaceDocuments(listOf(PersonalDocument.create(UuidCreator.getTimeOrderedEpoch(), c.id,
            DocumentType.RG, "12345", issuerId, Uf("SP"), null, LocalDate.of(2010, 1, 15))), UUID.randomUUID())
        return customerRepository.save(c)
    }

    @Test fun `returns full customer with nested phone address and sub-aggregate ids`() {
        val c = seedCustomer()
        mockMvc.get("/customers/${c.id}") { with(authentication(principal("customers.viewer"))) }
            .andExpect {
                status { isOk() }
                jsonPath("$.cpf") { value("11144477735") }
                jsonPath("$.phone.phoneNumber") { value("+5511999998888") }   // aninhado
                jsonPath("$.address.cep") { value("01310100") }                // aninhado
                jsonPath("$.documents[0].id") { exists() }
                jsonPath("$.bankAccounts[0].id") { exists() }
                jsonPath("$.bankAccounts[0].primaryDisbursementAccount") { doesNotExist() } // é summary do GET, não do gRPC
            }
    }

    @Test fun `returns 404 for another tenant`() {
        val c = seedCustomer()
        mockMvc.get("/customers/${c.id}") { with(authentication(principal("customers.viewer", tenant = UUID.randomUUID()))) }
            .andExpect { status { isNotFound() } }
    }

    @Test fun `returns 404 for soft-deleted customer`() {
        val c = seedCustomer()
        customerRepository.delete(c)   // soft delete
        mockMvc.get("/customers/${c.id}") { with(authentication(principal("customers.viewer"))) }
            .andExpect { status { isNotFound() } }
    }

    @Test fun `returns 403 for unauthorized role`() {
        val c = seedCustomer()
        mockMvc.get("/customers/${c.id}") { with(authentication(principal("lending.viewer"))) }
            .andExpect { status { isForbidden() } }
    }

    @Test fun `returns 401 without token`() {
        mockMvc.get("/customers/${UUID.randomUUID()}").andExpect { status { isUnauthorized() } }
    }
}