package br.com.deltaglobalbank.customers.features.customers.listCustomer

import br.com.deltaglobalbank.customers.domain.customer.Customer
import br.com.deltaglobalbank.customers.domain.customer.CustomerRepository
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.*
import br.com.deltaglobalbank.customers.domain.shared.valueobjects.Uf
import com.github.f4b6a3.uuid.UuidCreator
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.util.UUID

@ExtendWith(MockKExtension::class)
class ListCustomerUseCaseTests {
    @MockK lateinit var customerRepository: CustomerRepository
    private lateinit var useCase: ListCustomerUseCase
    private val tenantId = UUID.randomUUID()

    @BeforeEach fun setUp() { useCase = ListCustomerUseCase(customerRepository) }

    private fun aCustomer() = Customer.create(
        id = UuidCreator.getTimeOrderedEpoch(), tenantId = tenantId,
        cpf = Cpf("11144477735"), fullName = FullName("João da Silva"),
        birthDate = BirthDate(LocalDate.of(1980, 5, 15)), gender = Gender.MALE,
        motherName = MotherName("Maria da Silva"), maritalStatus = MaritalStatus.MARRIED,
        email = Email("joao@exemplo.com"), phoneNumber = Phone("+5511999998888"),
        address = Address("01310100", "Av Paulista", "São Paulo", Uf("SP")),
        createdBy = UUID.randomUUID(), bankAccounts = emptyList(), documents = emptyList()
    )

    @Test fun `maps content and returns pagination metadata`() {
        val customer = aCustomer()
        every { customerRepository.findPage(tenantId, any()) } returns
                PageImpl(listOf(customer), PageRequest.of(0, 20), 1)

        val r = useCase.execute(ListCustomerQuery(tenantId, page = 0, size = 20))

        assertEquals(1, r.items.size)
        assertEquals("11144477735", r.items.first().cpf)
        assertEquals("João da Silva", r.items.first().fullName)
        assertEquals("active", r.items.first().status)
        assertEquals(1L, r.totalElements)
        assertEquals(0, r.page)
        assertEquals(20, r.size)
    }

    @Test fun `clamps size to max 100`() {
        val slot = slot<Pageable>()
        every { customerRepository.findPage(tenantId, capture(slot)) } returns PageImpl(emptyList())
        useCase.execute(ListCustomerQuery(tenantId, page = 0, size = 500))
        assertEquals(100, slot.captured.pageSize)
    }

    @Test fun `uses default size 20 when null`() {
        val slot = slot<Pageable>()
        every { customerRepository.findPage(tenantId, capture(slot)) } returns PageImpl(emptyList())
        useCase.execute(ListCustomerQuery(tenantId, page = 0, size = null))
        assertEquals(20, slot.captured.pageSize)
    }

    @Test fun `clamps size below 1 to 1`() {
        val slot = slot<Pageable>()
        every { customerRepository.findPage(tenantId, capture(slot)) } returns PageImpl(emptyList())
        useCase.execute(ListCustomerQuery(tenantId, page = 0, size = 0))
        assertEquals(1, slot.captured.pageSize)
    }

    @Test fun `coerces negative page to zero`() {
        val slot = slot<Pageable>()
        every { customerRepository.findPage(tenantId, capture(slot)) } returns PageImpl(emptyList())
        useCase.execute(ListCustomerQuery(tenantId, page = -5, size = 20))
        assertEquals(0, slot.captured.pageNumber)
    }

    @Test fun `sorts by createdAt desc`() {
        val slot = slot<Pageable>()
        every { customerRepository.findPage(tenantId, capture(slot)) } returns PageImpl(emptyList())
        useCase.execute(ListCustomerQuery(tenantId, page = 0, size = 20))
        val order = slot.captured.sort.getOrderFor("createdAt")
        assertEquals(org.springframework.data.domain.Sort.Direction.DESC, order?.direction)
    }
}