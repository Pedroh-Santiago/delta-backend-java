package br.com.deltaglobalbank.customers.features.customers.changeCustomerStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditEntry;
import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditRepository;
import br.com.deltaglobalbank.customers.domain.customer.Customer;
import br.com.deltaglobalbank.customers.domain.customer.CustomerNotFound;
import br.com.deltaglobalbank.customers.domain.customer.CustomerRepository;
import br.com.deltaglobalbank.customers.domain.customer.StatusUnchanged;
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
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ChangeCustomerStatusUseCaseTests {

    private final CustomerRepository customerRepository = mock(CustomerRepository.class);
    private final CustomerAuditRepository customerAuditRepository = mock(CustomerAuditRepository.class);

    private ChangeCustomerStatusUseCase useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID actor = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ChangeCustomerStatusUseCase(customerRepository, customerAuditRepository);
    }

    private Customer activeCustomer() {
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

    private ChangeCustomerStatusCommand command(UUID customerId, String status) {
        return new ChangeCustomerStatusCommand(customerId, tenantId, actor, new ChangeCustomerStatusRequest(status));
    }

    @Test
    void inactivatesActiveCustomerAndAuditsOldAndNewValues() {
        Customer customer = activeCustomer();
        when(customerRepository.findById(customer.id(), tenantId)).thenReturn(customer);
        when(customerRepository.save(any())).thenReturn(customer);

        ChangeCustomerStatusResponse response = useCase.execute(command(customer.id(), "inactive"));

        assertEquals("inactive", response.status());
        assertEquals(actor, response.updatedBy());
        verify(customerRepository).save(customer);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CustomerAuditEntry>> audits = ArgumentCaptor.forClass(List.class);
        verify(customerAuditRepository).saveAll(audits.capture());
        CustomerAuditEntry entry = audits.getValue().get(0);
        assertEquals("active", entry.oldValue());
        assertEquals("inactive", entry.newValue());
        assertEquals(actor, entry.changedBy());
    }

    @Test
    void reactivatesInactiveCustomerAndAudits() {
        Customer customer = activeCustomer();
        customer.inactivate(actor);
        when(customerRepository.findById(customer.id(), tenantId)).thenReturn(customer);
        when(customerRepository.save(any())).thenReturn(customer);

        ChangeCustomerStatusResponse response = useCase.execute(command(customer.id(), "active"));

        assertEquals("active", response.status());
        verify(customerRepository).save(customer);
        verify(customerAuditRepository).saveAll(any());
    }

    @Test
    void throwsStatusUnchangedOnRedundantTransitionAndDoesNotPersist() {
        Customer customer = activeCustomer();
        when(customerRepository.findById(customer.id(), tenantId)).thenReturn(customer);

        assertThrows(StatusUnchanged.class, () -> useCase.execute(command(customer.id(), "active")));

        verify(customerRepository, never()).save(any());
        verify(customerAuditRepository, never()).saveAll(any());
    }

    @Test
    void throwsIllegalArgumentOnInvalidStatusBeforeTouchingTheRepository() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command(UUID.randomUUID(), "suspended")));

        verify(customerRepository, never()).findById(any(), any());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void throwsCustomerNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id, tenantId)).thenReturn(null);

        assertThrows(CustomerNotFound.class, () -> useCase.execute(command(id, "inactive")));

        verify(customerRepository, never()).save(any());
    }
}
