package br.com.deltaglobalbank.customers.features.customers.updateCustomer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditRepository;
import br.com.deltaglobalbank.customers.domain.customer.Customer;
import br.com.deltaglobalbank.customers.domain.customer.CustomerNotFound;
import br.com.deltaglobalbank.customers.domain.customer.CustomerRepository;
import br.com.deltaglobalbank.customers.domain.customer.SubaggregateDoesNotBelongToCustomer;
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
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaDocumentIssuerRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateCustomerUseCaseTests {

    @Mock
    CustomerRepository customerRepository;
    @Mock
    CustomerAuditRepository customerAuditRepository;
    @Mock
    JpaDocumentIssuerRepository jpaDocumentIssuerRepository;

    private UpdateCustomerUseCase useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID actor = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new UpdateCustomerUseCase(customerRepository, customerAuditRepository, jpaDocumentIssuerRepository);
        lenient().when(jpaDocumentIssuerRepository.findAll()).thenReturn(List.of());
    }

    private Customer aCustomer() {
        return Customer.create(
            UuidCreator.getTimeOrderedEpoch(), tenantId,
            new Cpf("11144477735"), new FullName("João da Silva"),
            new BirthDate(LocalDate.of(1980, 5, 15)), Gender.MALE,
            new MotherName("Maria da Silva"), MaritalStatus.MARRIED,
            new Email("joao@exemplo.com"), new Phone("+5511999998888"),
            new Address("01310100", "Av Paulista", "São Paulo", new Uf("SP")),
            actor, List.of(), List.of()
        );
    }

    private UpdateCustomerRequest aRequest(List<BankAccountRequest> accounts) {
        return new UpdateCustomerRequest(
            "João Souza", LocalDate.of(1980, 5, 15), "male",
            "brasileira", "Maria da Silva", "married",
            "joao@novo.com", new PhoneRequest("+5511988887777"),
            new AddressRequest("01310100", "Av Paulista", null, null, null, "São Paulo", "SP", null),
            List.of(), accounts
        );
    }

    @Test
    void throwsCustomerNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id, tenantId)).thenReturn(null);
        assertThrows(CustomerNotFound.class,
            () -> useCase.execute(new UpdateCustomerCommand(id, tenantId, actor, aRequest(List.of()))));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void updatesCustomerAndPersists() {
        Customer customer = aCustomer();
        when(customerRepository.findById(customer.id(), tenantId)).thenReturn(customer);
        when(customerRepository.save(any())).thenReturn(customer);
        UpdateCustomerResponse response = useCase.execute(new UpdateCustomerCommand(
            customer.id(), tenantId, actor,
            aRequest(List.of(new BankAccountRequest(
                null, "341", "1234", "56789", null, "checking", "disbursement", true)))
        ));
        assertEquals("João Souza", response.fullName());
        assertEquals(1, response.bankAccounts().size());
        verify(customerRepository, times(1)).save(customer);
        verify(customerAuditRepository, times(1)).saveAll(any());
    }

    @Test
    void throwsWhenAccountIdDoesNotBelong() {
        Customer customer = aCustomer();
        when(customerRepository.findById(customer.id(), tenantId)).thenReturn(customer);
        assertThrows(SubaggregateDoesNotBelongToCustomer.class, () -> useCase.execute(new UpdateCustomerCommand(
            customer.id(), tenantId, actor,
            aRequest(List.of(new BankAccountRequest(
                UUID.randomUUID(), "341", "1234", "56789", null, "checking", "disbursement", true)))
        )));
        verify(customerRepository, never()).save(any());
    }
}
