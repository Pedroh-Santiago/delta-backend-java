package br.com.deltaglobalbank.customers.features.customers.deleteCustomer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import br.com.deltaglobalbank.customers.domain.document.DocumentType;
import br.com.deltaglobalbank.customers.domain.document.PersonalDocument;
import br.com.deltaglobalbank.customers.domain.shared.valueobjects.Uf;
import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.CustomerAuditEntity;
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaBankAccountRepository;
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaCustomerAuditRepository;
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaDocumentIssuerRepository;
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaPersonalDocumentRepository;
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
class DeleteCustomerTests {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    JpaBankAccountRepository jpaBankAccountRepository;
    @Autowired
    JpaPersonalDocumentRepository jpaPersonalDocumentRepository;
    @Autowired
    JpaCustomerAuditRepository jpaCustomerAuditRepository;
    @Autowired
    JpaDocumentIssuerRepository jpaDocumentIssuerRepository;

    private final UUID tenantId = UUID.randomUUID();

    private JwtAuthenticationToken principal(String... roles) {
        return new JwtAuthenticationToken(
            new AuthenticatedPrincipal(UUID.randomUUID(), tenantId, "user", List.of(roles), List.of(), false, UUID.randomUUID())
        );
    }

    private JwtAuthenticationToken principalForTenant(UUID tenant, String... roles) {
        return new JwtAuthenticationToken(
            new AuthenticatedPrincipal(UUID.randomUUID(), tenant, "user", List.of(roles), List.of(), false, UUID.randomUUID())
        );
    }

    private Customer seedCustomer() {
        UUID issuerId = jpaDocumentIssuerRepository.findByName("SSP").getId();
        Customer customer = Customer.create(
            UuidCreator.getTimeOrderedEpoch(),
            tenantId,
            new Cpf("11144477735"),
            new FullName("João da Silva"),
            new BirthDate(LocalDate.of(1980, 5, 15)),
            Gender.MALE,
            new MotherName("Maria da Silva"),
            MaritalStatus.MARRIED,
            new Email("joao@exemplo.com"),
            new Phone("+5511999998888"),
            new Address("01310100", "Av. Paulista", "SP", new Uf("SP")),
            UUID.randomUUID(),
            List.of(),
            List.of()
        );
        customer.addBankAccount(
            BankAccount.create(
                UuidCreator.getTimeOrderedEpoch(),
                customer.id(),
                new BankCode("341"),
                new Agency("1234"),
                "56789",
                "0",
                BankAccountType.CHECKING,
                BankAccountPurpose.DISBURSEMENT,
                true
            ),
            UUID.randomUUID()
        );
        customer.replaceDocuments(
            List.of(
                PersonalDocument.create(
                    UuidCreator.getTimeOrderedEpoch(),
                    customer.id(),
                    DocumentType.RG,
                    "12345",
                    issuerId,
                    new Uf("SP"),
                    null,
                    LocalDate.of(2010, 1, 15)
                )
            ),
            UUID.randomUUID()
        );
        return customerRepository.save(customer);
    }

    @Test
    void deletesCustomerAndCascadesToChildren() throws Exception {
        Customer customer = seedCustomer();

        mockMvc.perform(delete("/customers/" + customer.id())
                .with(authentication(principal("customers.admin"))))
            .andExpect(status().isNoContent());

        assertNull(customerRepository.findById(customer.id(), tenantId));
        assertTrue(jpaBankAccountRepository.findAllByCustomerId(customer.id()).isEmpty());
        assertTrue(jpaPersonalDocumentRepository.findAllByCustomerId(customer.id()).isEmpty());

        List<CustomerAuditEntity> audits = jpaCustomerAuditRepository.findAll().stream()
            .filter(it -> it.getCustomerId().equals(customer.id()) && it.getAction().equals("customer_deleted"))
            .toList();
        assertEquals(1, audits.size());
    }

    @Test
    void returns404ForCustomerFromAnotherTenant() throws Exception {
        Customer customer = seedCustomer();

        mockMvc.perform(delete("/customers/" + customer.id())
                .with(authentication(principalForTenant(UUID.randomUUID(), "customers.admin"))))
            .andExpect(status().isNotFound());

        assertNotNull(customerRepository.findById(customer.id(), tenantId));
        assertFalse(jpaBankAccountRepository.findAllByCustomerId(customer.id()).isEmpty());
    }

    @Test
    void operatorCannotDelete() throws Exception {
        Customer customer = seedCustomer();
        mockMvc.perform(delete("/customers/" + customer.id())
                .with(authentication(principal("customers.operator"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void returns401WithoutToken() throws Exception {
        mockMvc.perform(delete("/customers/" + UUID.randomUUID()))
            .andExpect(status().isUnauthorized());
    }
}
