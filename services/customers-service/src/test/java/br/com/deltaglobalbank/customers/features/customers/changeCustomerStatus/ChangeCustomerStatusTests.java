package br.com.deltaglobalbank.customers.features.customers.changeCustomerStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.CustomerAuditEntity;
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaCustomerAuditRepository;
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken;
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
@ActiveProfiles("test")
class ChangeCustomerStatusTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JpaCustomerAuditRepository jpaCustomerAuditRepository;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID actor = UUID.randomUUID();

    private JwtAuthenticationToken principal(String role, UUID tenant, UUID subject) {
        return new JwtAuthenticationToken(new AuthenticatedPrincipal(
            subject, tenant, "user", List.of(role), List.of(), false, UUID.randomUUID()));
    }

    private JwtAuthenticationToken principal(String role) {
        return principal(role, tenantId, actor);
    }

    private Customer seedCustomer(CustomerStatus status) {
        Customer c = Customer.create(
            UuidCreator.getTimeOrderedEpoch(), tenantId,
            new Cpf("11144477735"), new FullName("João da Silva"),
            new BirthDate(LocalDate.of(1980, 5, 15)), Gender.MALE,
            new MotherName("Maria da Silva"), MaritalStatus.MARRIED,
            new Email("joao@exemplo.com"), new Phone("+5511999998888"),
            new Address("01310100", "Av Paulista", "São Paulo", new Uf("SP")),
            UUID.randomUUID(), List.of(), List.of()
        );
        if (status == CustomerStatus.INACTIVE) {
            c.inactivate(UUID.randomUUID()); // auditoria não é persistida no seed
        }
        return customerRepository.save(c);
    }

    private Customer seedCustomer() {
        return seedCustomer(CustomerStatus.ACTIVE);
    }

    private List<CustomerAuditEntity> statusAudits(UUID customerId) {
        return jpaCustomerAuditRepository.findAll().stream()
            .filter(it -> it.getCustomerId().equals(customerId) && "status_changed".equals(it.getAction()))
            .toList();
    }

    @Test
    void activeToInactivePersistsAndWritesAudit() throws Exception {
        Customer c = seedCustomer(CustomerStatus.ACTIVE);

        mockMvc.perform(patch("/customers/{id}/status", c.id())
            .with(authentication(principal("customers.admin")))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"inactive\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("inactive"))
            .andExpect(jsonPath("$.updatedBy").value(actor.toString()));

        assertEquals("inactive", customerRepository.findById(c.id(), tenantId).snapshot().status().toDatabaseValue());
        List<CustomerAuditEntity> audits = statusAudits(c.id());
        assertEquals(1, audits.size());
        assertEquals("active", audits.get(0).getOldValue());
        assertEquals("inactive", audits.get(0).getNewValue());
        assertEquals(actor, audits.get(0).getChangedBy());
    }

    @Test
    void inactiveToActivePersistsAndWritesAudit() throws Exception {
        Customer c = seedCustomer(CustomerStatus.INACTIVE);

        mockMvc.perform(patch("/customers/{id}/status", c.id())
            .with(authentication(principal("customers.admin")))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"active\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("active"));

        assertEquals("active", customerRepository.findById(c.id(), tenantId).snapshot().status().toDatabaseValue());
        List<CustomerAuditEntity> audits = statusAudits(c.id());
        assertEquals(1, audits.size());
        assertEquals("inactive", audits.get(0).getOldValue());
        assertEquals("active", audits.get(0).getNewValue());
    }

    @Test
    void sendingTheCurrentStatusReturns409AndWritesNoAudit() throws Exception {
        Customer c = seedCustomer(CustomerStatus.ACTIVE);

        mockMvc.perform(patch("/customers/{id}/status", c.id())
            .with(authentication(principal("customers.admin")))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"active\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("status_unchanged"));

        assertTrue(statusAudits(c.id()).isEmpty());
    }

    @Test
    void inactiveCustomerStillAppearsInListing() throws Exception {
        Customer c = seedCustomer(CustomerStatus.ACTIVE);

        mockMvc.perform(patch("/customers/{id}/status", c.id())
            .with(authentication(principal("customers.admin")))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"inactive\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/customers").with(authentication(principal("customers.admin"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[?(@.id == '" + c.id() + "')].status").value("inactive"));
    }

    @Test
    void returns400OnInvalidStatus() throws Exception {
        Customer c = seedCustomer();

        mockMvc.perform(patch("/customers/{id}/status", c.id())
            .with(authentication(principal("customers.admin")))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"suspended\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("invalid_status"));
    }

    @Test
    void returns404ForCustomerFromAnotherTenant() throws Exception {
        Customer c = seedCustomer();

        mockMvc.perform(patch("/customers/{id}/status", c.id())
            .with(authentication(principal("customers.admin", UUID.randomUUID(), actor)))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"inactive\"}"))
            .andExpect(status().isNotFound());

        assertEquals("active", customerRepository.findById(c.id(), tenantId).snapshot().status().toDatabaseValue());
    }

    @Test
    void returns404ForSoftDeletedCustomer() throws Exception {
        Customer c = seedCustomer();

        mockMvc.perform(delete("/customers/{id}", c.id())
            .with(authentication(principal("customers.admin"))))
            .andExpect(status().isNoContent());

        mockMvc.perform(patch("/customers/{id}/status", c.id())
            .with(authentication(principal("customers.admin")))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"inactive\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void viewerCannotChangeStatus() throws Exception {
        Customer c = seedCustomer();

        mockMvc.perform(patch("/customers/{id}/status", c.id())
            .with(authentication(principal("customers.viewer")))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"inactive\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void returns401WithoutToken() throws Exception {
        mockMvc.perform(patch("/customers/{id}/status", UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"inactive\"}"))
            .andExpect(status().isUnauthorized());
    }
}
