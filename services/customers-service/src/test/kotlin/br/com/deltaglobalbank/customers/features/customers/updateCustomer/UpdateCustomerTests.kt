package br.com.deltaglobalbank.customers.features.customers.updateCustomer

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
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken
import com.github.f4b6a3.uuid.UuidCreator
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.put
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
@Transactional
@ActiveProfiles("test")
class UpdateCustomerTests {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var customerRepository: CustomerRepository
    @Autowired lateinit var jpaDocumentIssuerRepository: JpaDocumentIssuerRepository
    @Autowired lateinit var jpaCustomerAuditRepository: JpaCustomerAuditRepository
    @Autowired lateinit var jpaBankAccountRepository: JpaBankAccountRepository

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
        c.addBankAccount(
            BankAccount.create(UuidCreator.getTimeOrderedEpoch(), c.id, BankCode("341"), Agency("1234"),
                "56789", "0", BankAccountType.CHECKING, BankAccountPurpose.DISBURSEMENT, true),
            UUID.randomUUID(),
        )
        c.replaceDocuments(
            listOf(PersonalDocument.create(UuidCreator.getTimeOrderedEpoch(), c.id,
                DocumentType.RG, "12345", issuerId, Uf("SP"), null, LocalDate.of(2010, 1, 15))),
            UUID.randomUUID(),
        )
        return customerRepository.save(c)
    }

    private fun body(cpfLine: String = "", accounts: String, documents: String = "[]") = """
        { $cpfLine "fullName": "João Souza", "birthDate": "1980-05-15", "gender": "male",
          "nationality": "brasileira", "motherName": "Maria da Silva", "maritalStatus": "married",
          "email": "joao@novo.com", "phone": { "phoneNumber": "+5511988887777" },
          "address": { "cep": "01310100", "street": "Av Paulista", "city": "São Paulo", "state": "SP", "country": "BR" },
          "documents": $documents, "bankAccounts": $accounts }
    """.trimIndent()

    @Test fun `updates existing account by id and audits full name change`() {
        val c = seedCustomer()
        val acctId = c.bankAccounts.first().id
        mockMvc.put("/customers/${c.id}") {
            with(authentication(principal("customers.admin")))
            contentType = MediaType.APPLICATION_JSON
            content = body(accounts = """[{"id":"$acctId","bankCode":"341","agency":"9999","accountNumber":"56789","accountDigit":"0","accountType":"checking","purpose":"disbursement","isPrimary":true}]""")
        }.andExpect {
            status { isOk() }
            jsonPath("$.fullName") { value("João Souza") }
            jsonPath("$.bankAccounts[0].agency") { value("9999") }
        }
        assertTrue(jpaCustomerAuditRepository.findAll().any { it.customerId == c.id && it.action == "full_name_changed" })
    }

    @Test fun `creates account without id and keeps omitted ones`() {
        val c = seedCustomer()
        val acctId = c.bankAccounts.first().id
        mockMvc.put("/customers/${c.id}") {
            with(authentication(principal("customers.admin")))
            contentType = MediaType.APPLICATION_JSON
            content = body(accounts = """[
                {"id":"$acctId","bankCode":"341","agency":"1234","accountNumber":"56789","accountDigit":"0","accountType":"checking","purpose":"disbursement","isPrimary":true},
                {"bankCode":"001","agency":"5678","accountNumber":"12345","accountDigit":"6","accountType":"checking","purpose":"payoff","isPrimary":true}]""")
        }.andExpect { status { isOk() } }
        assertTrue(jpaBankAccountRepository.findAllByCustomerId(c.id).size == 2)
        assertTrue(jpaCustomerAuditRepository.findAll().any { it.customerId == c.id && it.action == "bank_account_added" })
    }

    @Test fun `keeps omitted account (no soft-delete on omit)`() {
        val c = seedCustomer()
        mockMvc.put("/customers/${c.id}") {
            with(authentication(principal("customers.admin")))
            contentType = MediaType.APPLICATION_JSON
            content = body(accounts = "[]")
        }.andExpect { status { isOk() } }
        assertTrue(jpaBankAccountRepository.findAllByCustomerId(c.id).size == 1)
    }

    @Test fun `ignores cpf in payload`() {
        val c = seedCustomer()
        val acctId = c.bankAccounts.first().id
        mockMvc.put("/customers/${c.id}") {
            with(authentication(principal("customers.admin")))
            contentType = MediaType.APPLICATION_JSON
            content = body(cpfLine = """ "cpf": "529.982.247-25", """,
                accounts = """[{"id":"$acctId","bankCode":"341","agency":"1234","accountNumber":"56789","accountDigit":"0","accountType":"checking","purpose":"disbursement","isPrimary":true}]""")
        }.andExpect { status { isOk() }; jsonPath("$.cpf") { value("11144477735") } }
    }

    @Test fun `returns 400 on multiple primary per purpose`() {
        val c = seedCustomer()
        mockMvc.put("/customers/${c.id}") {
            with(authentication(principal("customers.admin")))
            contentType = MediaType.APPLICATION_JSON
            content = body(accounts = """[
                {"bankCode":"341","agency":"1111","accountNumber":"1","accountType":"checking","purpose":"disbursement","isPrimary":true},
                {"bankCode":"001","agency":"2222","accountNumber":"2","accountType":"checking","purpose":"disbursement","isPrimary":true}]""")
        }.andExpect { status { isBadRequest() }; jsonPath("$.error") { value("multiple_primary_per_purpose") } }
    }

    @Test fun `returns 404 for another tenant`() {
        val c = seedCustomer()
        mockMvc.put("/customers/${c.id}") {
            with(authentication(principal("customers.admin", tenant = UUID.randomUUID())))
            contentType = MediaType.APPLICATION_JSON
            content = body(accounts = "[]")
        }.andExpect { status { isNotFound() } }
    }
}