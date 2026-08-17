package br.com.deltaglobalbank.customers.infrastructure.grpc

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
import br.com.deltaglobalbank.customers.domain.shared.valueobjects.Uf
import br.com.deltaglobalbank.customers.grpc.BatchGetCustomersRequest
import br.com.deltaglobalbank.customers.grpc.BatchGetCustomersResponse
import br.com.deltaglobalbank.customers.grpc.GetCustomerByCpfRequest
import br.com.deltaglobalbank.customers.grpc.GetCustomerRequest
import br.com.deltaglobalbank.customers.grpc.GetCustomerResponse
import br.com.deltaglobalbank.customers.grpc.SearchCustomersRequest
import br.com.deltaglobalbank.customers.grpc.SearchCustomersResponse
import com.github.f4b6a3.uuid.UuidCreator
import io.grpc.Status
import io.grpc.stub.StreamObserver
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration::class)
@Transactional
class CustomersInternalServiceTests {

    @Autowired
    lateinit var service: CustomersInternalServiceImpl
    @Autowired lateinit var customerRepository: CustomerRepository

    private val tenantId = UUID.randomUUID()
    private val actor = UUID.randomUUID()

    private class CaptureObserver<T> : StreamObserver<T> {
        var value: T? = null
        var error: Throwable? = null
        var completed = false
        override fun onNext(v: T) { value = v }
        override fun onError(t: Throwable) { error = t }
        override fun onCompleted() { completed = true }
    }

    private fun seedCustomer(cpf: String = "11144477735" ,withPrimary: Boolean = true): Customer {
        val customer = Customer.create(
            id = UuidCreator.getTimeOrderedEpoch(),
            tenantId = tenantId,
            cpf = Cpf(cpf),
            fullName = FullName("João da Silva"),
            birthDate = BirthDate(LocalDate.of(1980, 5, 15)),
            gender = Gender.MALE,
            motherName = MotherName("Maria da Silva"),
            maritalStatus = MaritalStatus.MARRIED,
            email = Email("joao@exemplo.com"),
            phoneNumber = Phone("+5511999998888"),
            address = Address(cep = "01310100", street = "Av. Paulista", city = "São Paulo", state = Uf("SP")),
            createdBy = actor,
            bankAccounts = emptyList(),
            documents = emptyList()
        )
        if (withPrimary) {
            customer.addBankAccount(
                BankAccount.create(
                    id = UuidCreator.getTimeOrderedEpoch(), customerId = customer.id,
                    bankCode = BankCode("341"), agency = Agency("1234"),
                    accountNumber = "56789", accountDigit = "0",
                    accountType = BankAccountType.CHECKING, purpose = BankAccountPurpose.DISBURSEMENT,
                    isPrimary = true,
                ),
                actor,
            )
        }
        return customerRepository.save(customer)
    }

    @Test
    fun `getCustomer returns summary with primary disbursement account`() {
        val customer = seedCustomer(withPrimary = true)
        val observer = CaptureObserver<GetCustomerResponse>()

        service.getCustomer(
            GetCustomerRequest.newBuilder()
                .setCustomerId(customer.id.toString())
                .setTenantId(tenantId.toString())
                .build(),
            observer,
        )

        val summary = observer.value!!.customer
        assertEquals("11144477735", summary.cpf)
        assertEquals("João da Silva", summary.fullName)
        assertTrue(summary.hasPrimaryDisbursementAccount())
        assertEquals("341", summary.primaryDisbursementAccount.bankCode)
    }

    @Test
    fun `getCustomer omits primary account when absent`() {
        val customer = seedCustomer(withPrimary = false)
        val observer = CaptureObserver<GetCustomerResponse>()
        service.getCustomer(
            GetCustomerRequest.newBuilder()
                .setCustomerId(customer.id.toString())
                .setTenantId(tenantId.toString())
                .build(),
            observer,
        )
        assertFalse(observer.value!!.customer.hasPrimaryDisbursementAccount())
    }

    @Test
    fun `getCustomer returns NOT_FOUND for another tenant`() {
        val customer = seedCustomer()
        val observer = CaptureObserver<GetCustomerResponse>()
        service.getCustomer(
            GetCustomerRequest.newBuilder()
                .setCustomerId(customer.id.toString())
                .setTenantId(UUID.randomUUID().toString())
                .build(),
            observer,
        )
        assertNull(observer.value)
        assertEquals(Status.Code.NOT_FOUND, Status.fromThrowable(observer.error!!).code)
    }

    @Test
    fun `getCustomer returns INVALID_ARGUMENT for malformed id`() {
        val observer = CaptureObserver<GetCustomerResponse>()
        service.getCustomer(
            GetCustomerRequest.newBuilder().setCustomerId("not-a-uuid").setTenantId(tenantId.toString()).build(),
            observer,
        )
        assertEquals(Status.Code.INVALID_ARGUMENT, Status.fromThrowable(observer.error!!).code)
    }

    @Test
    fun `getCustomerByCpf returns summary and normalizes cpf`(){
        val customer = seedCustomer()
        val observer = CaptureObserver<GetCustomerResponse>()
        service.getCustomerByCpf(
            GetCustomerByCpfRequest.newBuilder()
                .setCpf("111.444.777-35")
                .setTenantId(tenantId.toString())
                .build(),
            observer,
        )
        assertEquals("11144477735", observer.value!!.customer.cpf)
    }

    @Test
    fun `getCustomerByCpf returns INVALID_ARGUMENT for invalid cpf`(){
        val customer = seedCustomer()
        val observer = CaptureObserver<GetCustomerResponse>()
        service.getCustomerByCpf(
            GetCustomerByCpfRequest.newBuilder()
                .setCpf("111.444.777-00")
                .setTenantId(tenantId.toString())
                .build(),
            observer,
        )
        assertEquals(Status.Code.INVALID_ARGUMENT, Status.fromThrowable(observer.error!!).code)
    }

    @Test
    fun `getCustomerByCpf returns NOT_FOUND for another tenant`() {
        seedCustomer()
        val observer = CaptureObserver<GetCustomerResponse>()
        service.getCustomerByCpf(
            GetCustomerByCpfRequest.newBuilder()
                .setCpf("11144477735")
                .setTenantId(UUID.randomUUID().toString())
                .build(),
            observer,
        )
        assertEquals(Status.Code.NOT_FOUND, Status.fromThrowable(observer.error!!).code)
    }

    @Test
    fun `batchGetCustomers returns the requested customers`() {
        val a = seedCustomer(cpf = "11144477735")
        val b = seedCustomer(cpf = "52998224725")
        val observer = CaptureObserver<BatchGetCustomersResponse>()
        service.batchGetCustomers(
            BatchGetCustomersRequest.newBuilder()
                .addCustomerIds(a.id.toString())
                .addCustomerIds(b.id.toString())
                .setTenantId(tenantId.toString())
                .build(),
            observer,
        )
        assertEquals(2, observer.value!!.customersList.size)
    }

    @Test
    fun `batchGetCustomers excludes customers from another tenant`() {
        val a = seedCustomer()
        val observer = CaptureObserver<BatchGetCustomersResponse>()
        service.batchGetCustomers(
            BatchGetCustomersRequest.newBuilder()
                .addCustomerIds(a.id.toString())
                .setTenantId(UUID.randomUUID().toString())
                .build(),
            observer,
        )
        assertTrue(observer.value!!.customersList.isEmpty())
    }

    @Test
    fun `searchCustomers returns matches by approximate name`() {
        seedCustomer()
        val observer = CaptureObserver<SearchCustomersResponse>()
        service.searchCustomers(
            SearchCustomersRequest.newBuilder()
                .setQuery("joão")
                .setTenantId(tenantId.toString())
                .setLimit(10)
                .build(),
            observer,
        )
        assertTrue(observer.value!!.customersList.isNotEmpty())
    }

    @Test
    fun `searchCustomers is scoped by tenant`() {
        seedCustomer()
        val observer = CaptureObserver<SearchCustomersResponse>()
        service.searchCustomers(
            SearchCustomersRequest.newBuilder()
                .setQuery("João")
                .setTenantId(UUID.randomUUID().toString())
                .setLimit(10)
                .build(),
            observer,
        )
        assertTrue(observer.value!!.customersList.isEmpty())
    }
}