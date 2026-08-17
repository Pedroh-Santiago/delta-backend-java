package br.com.deltaglobalbank.customers.features.customers.deleteCustomer

import br.com.deltaglobalbank.customers.TestcontainersConfiguration
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccount
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccountPurpose
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccountType
import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.Agency
import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.BankCode
import br.com.deltaglobalbank.customers.domain.customer.Customer
import br.com.deltaglobalbank.customers.domain.customer.CustomerRepository
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Address
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.BirthDate
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Cpf
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Email
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.FullName
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Gender
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.MaritalStatus
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.MotherName
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Phone
import br.com.deltaglobalbank.customers.domain.document.DocumentType
import br.com.deltaglobalbank.customers.domain.document.PersonalDocument
import br.com.deltaglobalbank.customers.domain.shared.valueobjects.Uf
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaBankAccountRepository
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaCustomerAuditRepository
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaDocumentIssuerRepository
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaPersonalDocumentRepository
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken
import com.github.f4b6a3.uuid.UuidCreator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
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
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
@Transactional
@ActiveProfiles("test")
class DeleteCustomerTests{

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var customerRepository: CustomerRepository
    @Autowired lateinit var jpaBankAccountRepository: JpaBankAccountRepository
    @Autowired lateinit var jpaPersonalDocumentRepository: JpaPersonalDocumentRepository
    @Autowired lateinit var jpaCustomerAuditRepository: JpaCustomerAuditRepository
    @Autowired lateinit var jpaDocumentIssuerRepository: JpaDocumentIssuerRepository

    private val tenantId = UUID.randomUUID()

    private fun principal(vararg roles: String) = JwtAuthenticationToken(
        AuthenticatedPrincipal(
            UUID.randomUUID(),
            tenantId,
            "user",
            roles.toList(),
            emptyList(),
            false,
            UUID.randomUUID()
        )
    )

    private fun principalForTenant(tenant: UUID, vararg roles: String) = JwtAuthenticationToken(
        AuthenticatedPrincipal(UUID.randomUUID(), tenant, "user", roles.toList(), emptyList(), false, UUID.randomUUID())
    )

    private fun seedCustomer(): Customer {
        val issuerId = jpaDocumentIssuerRepository.findByName("SSP")!!.id   // seed do Flyway V2
        val customer = Customer.create(
            id = UuidCreator.getTimeOrderedEpoch(),
            tenantId = tenantId,
            cpf = Cpf("11144477735"),
            fullName = FullName("João da Silva"),
            birthDate = BirthDate(LocalDate.of(1980, 5, 15)),
            gender = Gender.MALE,
            motherName = MotherName("Maria da Silva"),
            maritalStatus = MaritalStatus.MARRIED,
            email = Email("joao@exemplo.com"),
            phoneNumber = Phone("+5511999998888"),
            address = Address(cep = "01310100", street = "Av. Paulista", city = "SP", state = Uf("SP")),
            createdBy = UUID.randomUUID(),
            bankAccounts = emptyList(),
            documents = emptyList()
        )
        customer.addBankAccount(
            BankAccount.create(
                UuidCreator.getTimeOrderedEpoch(),
                customer.id,
                BankCode("341"),
                Agency("1234"),
                "56789",
                "0",
                BankAccountType.CHECKING,
                BankAccountPurpose.DISBURSEMENT,
                true
            ),
            UUID.randomUUID(),
        )
        customer.replaceDocuments(
            listOf(
                PersonalDocument.create(
                    UuidCreator.getTimeOrderedEpoch(),
                    customer.id,
                    DocumentType.RG,
                    "12345",
                    issuerId,
                    Uf("SP"),
                    null,
                    LocalDate.of(2010, 1, 15))),
            UUID.randomUUID(),
        )
        return customerRepository.save(customer)
    }

    @Test
    fun `deletes customer and cascades to children`() {
        val customer = seedCustomer()

        mockMvc.delete("/customers/${customer.id}") {
            with(authentication(principal("customers.admin")))
        }.andExpect { status { isNoContent() } }

        assertNull(customerRepository.findById(customer.id, tenantId))
        assertTrue(jpaBankAccountRepository.findAllByCustomerId(customer.id).isEmpty())
        assertTrue(jpaPersonalDocumentRepository.findAllByCustomerId(customer.id).isEmpty())

        val audits = jpaCustomerAuditRepository.findAll().filter {
            it.customerId == customer.id && it.action == "customer_deleted"
        }
        assertEquals(1, audits.size)
    }

    @Test
    fun `returns 404 for customer from another tenant`() {
        val customer = seedCustomer()
        val otherTenant = principal("customers.admin")

        mockMvc.delete("/customers/${customer.id}") {
            with(authentication(principalForTenant(UUID.randomUUID(), "customers.admin")))
        }.andExpect { status { isNotFound() } }

        assertNotNull(customerRepository.findById(customer.id, tenantId))
        assertFalse(jpaBankAccountRepository.findAllByCustomerId(customer.id).isEmpty())
    }

    @Test
    fun `operator cannot delete`() {
        val customer = seedCustomer()
        mockMvc.delete("/customers/${customer.id}") {
            with(authentication(principal("customers.operator")))
        }.andExpect { status { isForbidden() } }
    }

    @Test
    fun `returns 401 without token`() {
        mockMvc.delete("/customers/${UUID.randomUUID()}")
            .andExpect { status { isUnauthorized() } }
    }
}