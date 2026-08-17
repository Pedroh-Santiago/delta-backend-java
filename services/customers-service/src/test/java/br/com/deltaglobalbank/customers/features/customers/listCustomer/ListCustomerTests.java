package br.com.deltaglobalbank.customers.features.customers.listCustomer;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.customers.TestcontainersConfiguration;
import br.com.deltaglobalbank.customers.domain.customer.Customer;
import br.com.deltaglobalbank.customers.domain.customer.CustomerRepository;
import br.com.deltaglobalbank.customers.domain.customer.CustomerStatus;
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
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken;
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
@ActiveProfiles("test")
class ListCustomerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID actor = UUID.randomUUID();

    private JwtAuthenticationToken principal(String role, UUID tenant) {
        return new JwtAuthenticationToken(new AuthenticatedPrincipal(
            actor, tenant, "user", List.of(role), List.of(), false, UUID.randomUUID()));
    }

    private JwtAuthenticationToken principal(String role) {
        return principal(role, tenantId);
    }

    private Customer seedCustomer(String cpf, String fullName) {
        return seedCustomer(cpf, fullName, "São Paulo", "SP", CustomerStatus.ACTIVE);
    }

    private Customer seedCustomer(String cpf, String fullName, String city, String state) {
        return seedCustomer(cpf, fullName, city, state, CustomerStatus.ACTIVE);
    }

    private Customer seedCustomer(String cpf, String fullName, CustomerStatus status) {
        return seedCustomer(cpf, fullName, "São Paulo", "SP", status);
    }

    private Customer seedCustomer(String cpf, String fullName, String city, String state, CustomerStatus status) {
        Customer c = Customer.create(
            UuidCreator.getTimeOrderedEpoch(), tenantId,
            new Cpf(cpf), new FullName(fullName),
            new BirthDate(LocalDate.of(1980, 5, 15)), Gender.MALE,
            new MotherName("Maria da Silva"), MaritalStatus.MARRIED,
            new Email("cliente@exemplo.com"), new Phone("+5511999998888"),
            new Address("01310100", "Av Paulista", city, new Uf(state)),
            UUID.randomUUID(), List.of(), List.of()
        );
        if (status == CustomerStatus.INACTIVE) {
            c.inactivate(UUID.randomUUID());
        }
        return customerRepository.save(c);
    }

    @Test
    void responseIncludesPhoneNumberCityAndState() throws Exception {
        Customer c = seedCustomer("11144477735", "João da Silva", "Recife", "PE");

        mockMvc.perform(get("/customers").with(authentication(principal("customers.viewer"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[?(@.id == '" + c.id() + "')].phoneNumber").value("+5511999998888"))
            .andExpect(jsonPath("$.items[?(@.id == '" + c.id() + "')].city").value("Recife"))
            .andExpect(jsonPath("$.items[?(@.id == '" + c.id() + "')].state").value("PE"));
    }

    @Test
    void filtersByStatusActive() throws Exception {
        Customer activeCustomer = seedCustomer("11144477735", "João Ativo", CustomerStatus.ACTIVE);
        Customer inactiveCustomer = seedCustomer("19630438046", "Maria Inativa", CustomerStatus.INACTIVE);

        mockMvc.perform(get("/customers")
            .param("status", "active")
            .with(authentication(principal("customers.viewer"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[?(@.id == '" + activeCustomer.id() + "')]").exists())
            .andExpect(jsonPath("$.items[?(@.id == '" + inactiveCustomer.id() + "')]").doesNotExist());
    }

    @Test
    void filtersByStatusInactive() throws Exception {
        Customer activeCustomer = seedCustomer("11144477735", "João Ativo", CustomerStatus.ACTIVE);
        Customer inactiveCustomer = seedCustomer("19630438046", "Maria Inativa", CustomerStatus.INACTIVE);

        mockMvc.perform(get("/customers")
            .param("status", "inactive")
            .with(authentication(principal("customers.viewer"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[?(@.id == '" + inactiveCustomer.id() + "')]").exists())
            .andExpect(jsonPath("$.items[?(@.id == '" + activeCustomer.id() + "')]").doesNotExist());
    }

    @Test
    void returns400ForAnInvalidStatusFilter() throws Exception {
        mockMvc.perform(get("/customers")
            .param("status", "suspended")
            .with(authentication(principal("customers.viewer"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("invalid_status_filter"));
    }

    @Test
    void filtersByPartialCpf() throws Exception {
        Customer target = seedCustomer("11144477735", "João da Silva");
        Customer other = seedCustomer("19630438046", "Maria Testando");

        mockMvc.perform(get("/customers")
            .param("cpf", "444777")
            .with(authentication(principal("customers.viewer"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[?(@.id == '" + target.id() + "')]").exists())
            .andExpect(jsonPath("$.items[?(@.id == '" + other.id() + "')]").doesNotExist());
    }

    @Test
    void filtersByPartialNameCaseInsensitively() throws Exception {
        Customer target = seedCustomer("11144477735", "João da Silva Souza");
        Customer other = seedCustomer("19630438046", "Maria Testando");

        mockMvc.perform(get("/customers")
            .param("fullName", "SILVA")
            .with(authentication(principal("customers.viewer"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[?(@.id == '" + target.id() + "')]").exists())
            .andExpect(jsonPath("$.items[?(@.id == '" + other.id() + "')]").doesNotExist());
    }

    @Test
    void combinesStatusCpfAndNameFilters() throws Exception {
        Customer target = seedCustomer("11144477735", "João da Silva", CustomerStatus.ACTIVE);
        seedCustomer("19630438046", "João da Silva", CustomerStatus.INACTIVE);

        mockMvc.perform(get("/customers")
            .param("status", "active")
            .param("cpf", "111444")
            .param("fullName", "joão")
            .with(authentication(principal("customers.viewer"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.items[0].id").value(target.id().toString()));
    }

    @Test
    void doesNotLeakCustomersFromAnotherTenant() throws Exception {
        seedCustomer("11144477735", "João da Silva");

        mockMvc.perform(get("/customers")
            .with(authentication(principal("customers.viewer", UUID.randomUUID()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void returns401WithoutToken() throws Exception {
        mockMvc.perform(get("/customers")).andExpect(status().isUnauthorized());
    }
}
