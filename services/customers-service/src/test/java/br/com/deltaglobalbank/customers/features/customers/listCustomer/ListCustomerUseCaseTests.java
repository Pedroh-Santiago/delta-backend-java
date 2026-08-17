package br.com.deltaglobalbank.customers.features.customers.listCustomer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class ListCustomerUseCaseTests {

    @Mock
    CustomerRepository customerRepository;
    private ListCustomerUseCase useCase;
    private final UUID tenantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ListCustomerUseCase(customerRepository);
    }

    private Customer aCustomer() {
        return Customer.create(
            UuidCreator.getTimeOrderedEpoch(), tenantId,
            new Cpf("11144477735"), new FullName("João da Silva"),
            new BirthDate(LocalDate.of(1980, 5, 15)), Gender.MALE,
            new MotherName("Maria da Silva"), MaritalStatus.MARRIED,
            new Email("joao@exemplo.com"), new Phone("+5511999998888"),
            new Address("01310100", "Av Paulista", "São Paulo", new Uf("SP")),
            UUID.randomUUID(), List.of(), List.of()
        );
    }

    @Test
    void mapsContentAndReturnsPaginationMetadata() {
        Customer customer = aCustomer();
        when(customerRepository.findPage(eq(tenantId), any()))
            .thenReturn(new PageImpl<>(List.of(customer), PageRequest.of(0, 20), 1));

        ListCustomerResponse r = useCase.execute(new ListCustomerQuery(tenantId, 0, 20));

        assertEquals(1, r.items().size());
        assertEquals("11144477735", r.items().get(0).cpf());
        assertEquals("João da Silva", r.items().get(0).fullName());
        assertEquals("active", r.items().get(0).status());
        assertEquals(1L, r.totalElements());
        assertEquals(0, r.page());
        assertEquals(20, r.size());
    }

    @Test
    void clampsSizeToMax100() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        when(customerRepository.findPage(eq(tenantId), captor.capture())).thenReturn(new PageImpl<>(List.of()));
        useCase.execute(new ListCustomerQuery(tenantId, 0, 500));
        assertEquals(100, captor.getValue().getPageSize());
    }

    @Test
    void usesDefaultSize20WhenNull() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        when(customerRepository.findPage(eq(tenantId), captor.capture())).thenReturn(new PageImpl<>(List.of()));
        useCase.execute(new ListCustomerQuery(tenantId, 0, null));
        assertEquals(20, captor.getValue().getPageSize());
    }

    @Test
    void clampsSizeBelow1To1() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        when(customerRepository.findPage(eq(tenantId), captor.capture())).thenReturn(new PageImpl<>(List.of()));
        useCase.execute(new ListCustomerQuery(tenantId, 0, 0));
        assertEquals(1, captor.getValue().getPageSize());
    }

    @Test
    void coercesNegativePageToZero() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        when(customerRepository.findPage(eq(tenantId), captor.capture())).thenReturn(new PageImpl<>(List.of()));
        useCase.execute(new ListCustomerQuery(tenantId, -5, 20));
        assertEquals(0, captor.getValue().getPageNumber());
    }

    @Test
    void sortsByCreatedAtDesc() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        when(customerRepository.findPage(eq(tenantId), captor.capture())).thenReturn(new PageImpl<>(List.of()));
        useCase.execute(new ListCustomerQuery(tenantId, 0, 20));
        Sort.Order order = captor.getValue().getSort().getOrderFor("createdAt");
        assertEquals(Sort.Direction.DESC, order != null ? order.getDirection() : null);
    }
}
