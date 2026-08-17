package br.com.deltaglobalbank.customers.features.customers.getCustomer;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaDocumentIssuerRepository;
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
class GetCustomerTests {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    JpaDocumentIssuerRepository jpaDocumentIssuerRepository;

    private final UUID tenantId = UUID.randomUUID();

    private JwtAuthenticationToken principal(String... roles) {
        return principal(tenantId, roles);
    }

    private JwtAuthenticationToken principal(UUID tenant, String... roles) {
        return new JwtAuthenticationToken(
            new AuthenticatedPrincipal(UUID.randomUUID(), tenant, "user", List.of(roles), List.of(), false, UUID.randomUUID())
        );
    }

    private Customer seedCustomer() {
        UUID issuerId = jpaDocumentIssuerRepository.findByName("SSP").getId();
        Customer c = Customer.create(
            UuidCreator.getTimeOrderedEpoch(), tenantId,
            new Cpf("11144477735"), new FullName("João da Silva"),
            new BirthDate(LocalDate.of(1980, 5, 15)), Gender.MALE,
            new MotherName("Maria da Silva"), MaritalStatus.MARRIED,
            new Email("joao@exemplo.com"), new Phone("+5511999998888"),
            new Address("01310100", "Av Paulista", "São Paulo", new Uf("SP")),
            UUID.randomUUID(), List.of(), List.of()
        );
        c.addBankAccount(BankAccount.create(UuidCreator.getTimeOrderedEpoch(), c.id(), new BankCode("341"), new Agency("1234"),
            "56789", "0", BankAccountType.CHECKING, BankAccountPurpose.DISBURSEMENT, true), UUID.randomUUID());
        c.replaceDocuments(List.of(PersonalDocument.create(UuidCreator.getTimeOrderedEpoch(), c.id(),
            DocumentType.RG, "12345", issuerId, new Uf("SP"), null, LocalDate.of(2010, 1, 15))), UUID.randomUUID());
        return customerRepository.save(c);
    }

    @Test
    void returnsFullCustomerWithNestedPhoneAddressAndSubAggregateIds() throws Exception {
        Customer c = seedCustomer();
        mockMvc.perform(get("/customers/" + c.id())
                .with(authentication(principal("customers.viewer"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cpf").value("11144477735"))
            .andExpect(jsonPath("$.phone.phoneNumber").value("+5511999998888"))
            .andExpect(jsonPath("$.address.cep").value("01310100"))
            .andExpect(jsonPath("$.documents[0].id").exists())
            .andExpect(jsonPath("$.bankAccounts[0].id").exists())
            .andExpect(jsonPath("$.bankAccounts[0].primaryDisbursementAccount").doesNotExist());
    }

    @Test
    void returns404ForAnotherTenant() throws Exception {
        Customer c = seedCustomer();
        mockMvc.perform(get("/customers/" + c.id())
                .with(authentication(principal(UUID.randomUUID(), "customers.viewer"))))
            .andExpect(status().isNotFound());
    }

    @Test
    void returns404ForSoftDeletedCustomer() throws Exception {
        Customer c = seedCustomer();
        customerRepository.delete(c);
        mockMvc.perform(get("/customers/" + c.id())
                .with(authentication(principal("customers.viewer"))))
            .andExpect(status().isNotFound());
    }

    @Test
    void returns403ForUnauthorizedRole() throws Exception {
        Customer c = seedCustomer();
        mockMvc.perform(get("/customers/" + c.id())
                .with(authentication(principal("lending.viewer"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void returns401WithoutToken() throws Exception {
        mockMvc.perform(get("/customers/" + UUID.randomUUID()))
            .andExpect(status().isUnauthorized());
    }
}
