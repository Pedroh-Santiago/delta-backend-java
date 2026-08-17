package br.com.deltaglobalbank.customers.features.customers.updateCustomer

import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditRepository
import br.com.deltaglobalbank.customers.domain.customer.Customer
import br.com.deltaglobalbank.customers.domain.customer.CustomerNotFound
import br.com.deltaglobalbank.customers.domain.customer.CustomerRepository
import br.com.deltaglobalbank.customers.domain.customer.SubaggregateDoesNotBelongToCustomer
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.*
import br.com.deltaglobalbank.customers.domain.shared.valueobjects.Uf
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaDocumentIssuerRepository
import com.github.f4b6a3.uuid.UuidCreator
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import java.util.UUID

@ExtendWith(MockKExtension::class)
class UpdateCustomerUseCaseTests {
    @MockK lateinit var customerRepository: CustomerRepository
    @MockK(relaxed = true) lateinit var customerAuditRepository: CustomerAuditRepository
    @MockK lateinit var jpaDocumentIssuerRepository: JpaDocumentIssuerRepository

    private lateinit var useCase: UpdateCustomerUseCase

    private val tenantId = UUID.randomUUID()
    private val actor = UUID.randomUUID()

    @BeforeEach fun setUp() {
        useCase = UpdateCustomerUseCase(customerRepository, customerAuditRepository, jpaDocumentIssuerRepository)
        every { jpaDocumentIssuerRepository.findAll() } returns emptyList()
    }

    private fun Customer() = Customer.create(
        id = UuidCreator.getTimeOrderedEpoch(), tenantId = tenantId,
        cpf = Cpf("11144477735"), fullName = FullName("João da Silva"),
        birthDate = BirthDate(LocalDate.of(1980, 5, 15)), gender = Gender.MALE,
        motherName = MotherName("Maria da Silva"), maritalStatus = MaritalStatus.MARRIED,
        email = Email("joao@exemplo.com"), phoneNumber = Phone("+5511999998888"),
        address = Address("01310100", "Av Paulista", "São Paulo", Uf("SP")),
        createdBy = actor, bankAccounts = emptyList(), documents = emptyList()
    )

    private fun Request(accounts: List<BankAccountRequest> = emptyList()) = UpdateCustomerRequest(
        fullName = "João Souza", birthDate = LocalDate.of(1980, 5, 15), gender = "male",
        nationality = "brasileira", motherName = "Maria da Silva", maritalStatus = "married",
        email = "joao@novo.com", phone = PhoneRequest("+5511988887777"),
        address = AddressRequest(cep = "01310100", street = "Av Paulista", city = "São Paulo", state = "SP"),
        documents = emptyList(), bankAccounts = accounts,
    )

    @Test fun `throws CustomerNotFound when missing`() {
        val id = UUID.randomUUID()
        every { customerRepository.findById(id, tenantId) } returns null
        assertThrows(CustomerNotFound::class.java) {
            useCase.execute(UpdateCustomerCommand(id, tenantId, actor, Request()))
        }
        verify(exactly = 0) { customerRepository.save(any()) }
    }

    @Test fun `updates customer and persists`() {
        val customer = Customer()
        every { customerRepository.findById(customer.id, tenantId) } returns customer
        every { customerRepository.save(any()) } returns customer
        val response = useCase.execute(UpdateCustomerCommand(
            customer.id, tenantId, actor,
            Request(listOf(BankAccountRequest(
                bankCode = "341", agency = "1234", accountNumber = "56789",
                accountType = "checking", purpose = "disbursement", isPrimary = true)))
        ))
        assertEquals("João Souza", response.fullName)
        assertEquals(1, response.bankAccounts.size)
        verify { customerRepository.save(customer) }
        verify { customerAuditRepository.saveAll(any()) }
    }

    @Test fun `throws when account id does not belong`() {
        val customer = Customer()
        every { customerRepository.findById(customer.id, tenantId) } returns customer
        assertThrows(SubaggregateDoesNotBelongToCustomer::class.java) {
            useCase.execute(UpdateCustomerCommand(
                customer.id, tenantId, actor,
                Request(listOf(BankAccountRequest(
                    id = UUID.randomUUID(), bankCode = "341", agency = "1234", accountNumber = "56789",
                    accountType = "checking", purpose = "disbursement", isPrimary = true)))
            ))
        }
        verify(exactly = 0) { customerRepository.save(any()) }
    }
}