package br.com.deltaglobalbank.customers.infrastructure.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.customers.TestcontainersConfiguration;
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccount;
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccountPurpose;
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccountType;
import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.Agency;
import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.BankCode;
import br.com.deltaglobalbank.customers.domain.customer.Customer;
import br.com.deltaglobalbank.customers.domain.customer.CustomerRepository;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Address;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.BirthDate;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Cpf;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Email;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.FullName;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Gender;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.MaritalStatus;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.MotherName;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Phone;
import br.com.deltaglobalbank.customers.domain.shared.valueobjects.Uf;
import br.com.deltaglobalbank.customers.grpc.BatchGetCustomersRequest;
import br.com.deltaglobalbank.customers.grpc.BatchGetCustomersResponse;
import br.com.deltaglobalbank.customers.grpc.GetCustomerByCpfRequest;
import br.com.deltaglobalbank.customers.grpc.GetCustomerRequest;
import br.com.deltaglobalbank.customers.grpc.GetCustomerResponse;
import br.com.deltaglobalbank.customers.grpc.SearchCustomersRequest;
import br.com.deltaglobalbank.customers.grpc.SearchCustomersResponse;
import com.github.f4b6a3.uuid.UuidCreator;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
class CustomersInternalServiceTests {

    @Autowired
    CustomersInternalServiceImpl service;
    @Autowired
    CustomerRepository customerRepository;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID actor = UUID.randomUUID();

    private static class CaptureObserver<T> implements StreamObserver<T> {
        T value;
        Throwable error;
        boolean completed = false;

        @Override
        public void onNext(T v) {
            value = v;
        }

        @Override
        public void onError(Throwable t) {
            error = t;
        }

        @Override
        public void onCompleted() {
            completed = true;
        }
    }

    private Customer seedCustomer(String cpf, boolean withPrimary) {
        Customer customer = Customer.create(
            UuidCreator.getTimeOrderedEpoch(),
            tenantId,
            new Cpf(cpf),
            new FullName("João da Silva"),
            new BirthDate(LocalDate.of(1980, 5, 15)),
            Gender.MALE,
            new MotherName("Maria da Silva"),
            MaritalStatus.MARRIED,
            new Email("joao@exemplo.com"),
            new Phone("+5511999998888"),
            new Address("01310100", "Av. Paulista", "São Paulo", new Uf("SP")),
            actor,
            List.of(),
            List.of()
        );
        if (withPrimary) {
            customer.addBankAccount(
                BankAccount.create(
                    UuidCreator.getTimeOrderedEpoch(), customer.id(),
                    new BankCode("341"), new Agency("1234"),
                    "56789", "0",
                    BankAccountType.CHECKING, BankAccountPurpose.DISBURSEMENT,
                    true
                ),
                actor
            );
        }
        return customerRepository.save(customer);
    }

    private Customer seedCustomer(String cpf) {
        return seedCustomer(cpf, true);
    }

    private Customer seedCustomer() {
        return seedCustomer("11144477735", true);
    }

    @Test
    void getCustomerReturnsSummaryWithPrimaryDisbursementAccount() {
        Customer customer = seedCustomer("11144477735", true);
        CaptureObserver<GetCustomerResponse> observer = new CaptureObserver<>();

        service.getCustomer(
            GetCustomerRequest.newBuilder()
                .setCustomerId(customer.id().toString())
                .setTenantId(tenantId.toString())
                .build(),
            observer
        );

        var summary = observer.value.getCustomer();
        assertEquals("11144477735", summary.getCpf());
        assertEquals("João da Silva", summary.getFullName());
        assertTrue(summary.hasPrimaryDisbursementAccount());
        assertEquals("341", summary.getPrimaryDisbursementAccount().getBankCode());
    }

    @Test
    void getCustomerOmitsPrimaryAccountWhenAbsent() {
        Customer customer = seedCustomer("11144477735", false);
        CaptureObserver<GetCustomerResponse> observer = new CaptureObserver<>();
        service.getCustomer(
            GetCustomerRequest.newBuilder()
                .setCustomerId(customer.id().toString())
                .setTenantId(tenantId.toString())
                .build(),
            observer
        );
        assertFalse(observer.value.getCustomer().hasPrimaryDisbursementAccount());
    }

    @Test
    void getCustomerReturnsNotFoundForAnotherTenant() {
        Customer customer = seedCustomer();
        CaptureObserver<GetCustomerResponse> observer = new CaptureObserver<>();
        service.getCustomer(
            GetCustomerRequest.newBuilder()
                .setCustomerId(customer.id().toString())
                .setTenantId(UUID.randomUUID().toString())
                .build(),
            observer
        );
        assertNull(observer.value);
        assertEquals(Status.Code.NOT_FOUND, Status.fromThrowable(observer.error).getCode());
    }

    @Test
    void getCustomerReturnsInvalidArgumentForMalformedId() {
        CaptureObserver<GetCustomerResponse> observer = new CaptureObserver<>();
        service.getCustomer(
            GetCustomerRequest.newBuilder().setCustomerId("not-a-uuid").setTenantId(tenantId.toString()).build(),
            observer
        );
        assertEquals(Status.Code.INVALID_ARGUMENT, Status.fromThrowable(observer.error).getCode());
    }

    @Test
    void getCustomerByCpfReturnsSummaryAndNormalizesCpf() {
        seedCustomer();
        CaptureObserver<GetCustomerResponse> observer = new CaptureObserver<>();
        service.getCustomerByCpf(
            GetCustomerByCpfRequest.newBuilder()
                .setCpf("111.444.777-35")
                .setTenantId(tenantId.toString())
                .build(),
            observer
        );
        assertEquals("11144477735", observer.value.getCustomer().getCpf());
    }

    @Test
    void getCustomerByCpfReturnsInvalidArgumentForInvalidCpf() {
        seedCustomer();
        CaptureObserver<GetCustomerResponse> observer = new CaptureObserver<>();
        service.getCustomerByCpf(
            GetCustomerByCpfRequest.newBuilder()
                .setCpf("111.444.777-00")
                .setTenantId(tenantId.toString())
                .build(),
            observer
        );
        assertEquals(Status.Code.INVALID_ARGUMENT, Status.fromThrowable(observer.error).getCode());
    }

    @Test
    void getCustomerByCpfReturnsNotFoundForAnotherTenant() {
        seedCustomer();
        CaptureObserver<GetCustomerResponse> observer = new CaptureObserver<>();
        service.getCustomerByCpf(
            GetCustomerByCpfRequest.newBuilder()
                .setCpf("11144477735")
                .setTenantId(UUID.randomUUID().toString())
                .build(),
            observer
        );
        assertEquals(Status.Code.NOT_FOUND, Status.fromThrowable(observer.error).getCode());
    }

    @Test
    void batchGetCustomersReturnsTheRequestedCustomers() {
        Customer a = seedCustomer("11144477735");
        Customer b = seedCustomer("52998224725");
        CaptureObserver<BatchGetCustomersResponse> observer = new CaptureObserver<>();
        service.batchGetCustomers(
            BatchGetCustomersRequest.newBuilder()
                .addCustomerIds(a.id().toString())
                .addCustomerIds(b.id().toString())
                .setTenantId(tenantId.toString())
                .build(),
            observer
        );
        assertEquals(2, observer.value.getCustomersList().size());
    }

    @Test
    void batchGetCustomersExcludesCustomersFromAnotherTenant() {
        Customer a = seedCustomer();
        CaptureObserver<BatchGetCustomersResponse> observer = new CaptureObserver<>();
        service.batchGetCustomers(
            BatchGetCustomersRequest.newBuilder()
                .addCustomerIds(a.id().toString())
                .setTenantId(UUID.randomUUID().toString())
                .build(),
            observer
        );
        assertTrue(observer.value.getCustomersList().isEmpty());
    }

    @Test
    void searchCustomersReturnsMatchesByApproximateName() {
        seedCustomer();
        CaptureObserver<SearchCustomersResponse> observer = new CaptureObserver<>();
        service.searchCustomers(
            SearchCustomersRequest.newBuilder()
                .setQuery("joão")
                .setTenantId(tenantId.toString())
                .setLimit(10)
                .build(),
            observer
        );
        assertTrue(!observer.value.getCustomersList().isEmpty());
    }

    @Test
    void searchCustomersIsScopedByTenant() {
        seedCustomer();
        CaptureObserver<SearchCustomersResponse> observer = new CaptureObserver<>();
        service.searchCustomers(
            SearchCustomersRequest.newBuilder()
                .setQuery("João")
                .setTenantId(UUID.randomUUID().toString())
                .setLimit(10)
                .build(),
            observer
        );
        assertTrue(observer.value.getCustomersList().isEmpty());
    }
}
