package br.com.deltaglobalbank.customers.features.customers.updateCustomer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaBankAccountRepository;
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaCustomerAuditRepository;
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaDocumentIssuerRepository;
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
class UpdateCustomerTests {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    JpaDocumentIssuerRepository jpaDocumentIssuerRepository;
    @Autowired
    JpaCustomerAuditRepository jpaCustomerAuditRepository;
    @Autowired
    JpaBankAccountRepository jpaBankAccountRepository;

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
        c.addBankAccount(
            BankAccount.create(UuidCreator.getTimeOrderedEpoch(), c.id(), new BankCode("341"), new Agency("1234"),
                "56789", "0", BankAccountType.CHECKING, BankAccountPurpose.DISBURSEMENT, true),
            UUID.randomUUID()
        );
        c.replaceDocuments(
            List.of(PersonalDocument.create(UuidCreator.getTimeOrderedEpoch(), c.id(),
                DocumentType.RG, "12345", issuerId, new Uf("SP"), null, LocalDate.of(2010, 1, 15))),
            UUID.randomUUID()
        );
        return customerRepository.save(c);
    }

    private String body(String cpfLine, String accounts, String documents) {
        return """
            { %s "fullName": "João Souza", "birthDate": "1980-05-15", "gender": "male",
              "nationality": "brasileira", "motherName": "Maria da Silva", "maritalStatus": "married",
              "email": "joao@novo.com", "phone": { "phoneNumber": "+5511988887777" },
              "address": { "cep": "01310100", "street": "Av Paulista", "city": "São Paulo", "state": "SP", "country": "BR" },
              "documents": %s, "bankAccounts": %s }
            """.formatted(cpfLine, documents, accounts);
    }

    private String body(String accounts) {
        return body("", accounts, "[]");
    }

    @Test
    void updatesExistingAccountByIdAndAuditsFullNameChange() throws Exception {
        Customer c = seedCustomer();
        UUID acctId = c.bankAccounts().get(0).id();
        mockMvc.perform(put("/customers/" + c.id())
                .with(authentication(principal("customers.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("[{\"id\":\"" + acctId + "\",\"bankCode\":\"341\",\"agency\":\"9999\",\"accountNumber\":\"56789\",\"accountDigit\":\"0\",\"accountType\":\"checking\",\"purpose\":\"disbursement\",\"isPrimary\":true}]")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fullName").value("João Souza"))
            .andExpect(jsonPath("$.bankAccounts[0].agency").value("9999"));
        assertTrue(jpaCustomerAuditRepository.findAll().stream()
            .anyMatch(it -> it.getCustomerId().equals(c.id()) && it.getAction().equals("full_name_changed")));
    }

    @Test
    void createsAccountWithoutIdAndKeepsOmittedOnes() throws Exception {
        Customer c = seedCustomer();
        UUID acctId = c.bankAccounts().get(0).id();
        mockMvc.perform(put("/customers/" + c.id())
                .with(authentication(principal("customers.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("[" +
                    "{\"id\":\"" + acctId + "\",\"bankCode\":\"341\",\"agency\":\"1234\",\"accountNumber\":\"56789\",\"accountDigit\":\"0\",\"accountType\":\"checking\",\"purpose\":\"disbursement\",\"isPrimary\":true}," +
                    "{\"bankCode\":\"001\",\"agency\":\"5678\",\"accountNumber\":\"12345\",\"accountDigit\":\"6\",\"accountType\":\"checking\",\"purpose\":\"payoff\",\"isPrimary\":true}]")))
            .andExpect(status().isOk());
        assertTrue(jpaBankAccountRepository.findAllByCustomerId(c.id()).size() == 2);
        assertTrue(jpaCustomerAuditRepository.findAll().stream()
            .anyMatch(it -> it.getCustomerId().equals(c.id()) && it.getAction().equals("bank_account_added")));
    }

    @Test
    void keepsOmittedAccountNoSoftDeleteOnOmit() throws Exception {
        Customer c = seedCustomer();
        mockMvc.perform(put("/customers/" + c.id())
                .with(authentication(principal("customers.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("[]")))
            .andExpect(status().isOk());
        assertTrue(jpaBankAccountRepository.findAllByCustomerId(c.id()).size() == 1);
    }

    @Test
    void ignoresCpfInPayload() throws Exception {
        Customer c = seedCustomer();
        UUID acctId = c.bankAccounts().get(0).id();
        mockMvc.perform(put("/customers/" + c.id())
                .with(authentication(principal("customers.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("\"cpf\": \"529.982.247-25\", ",
                    "[{\"id\":\"" + acctId + "\",\"bankCode\":\"341\",\"agency\":\"1234\",\"accountNumber\":\"56789\",\"accountDigit\":\"0\",\"accountType\":\"checking\",\"purpose\":\"disbursement\",\"isPrimary\":true}]",
                    "[]")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cpf").value("11144477735"));
    }

    @Test
    void returns400OnMultiplePrimaryPerPurpose() throws Exception {
        Customer c = seedCustomer();
        mockMvc.perform(put("/customers/" + c.id())
                .with(authentication(principal("customers.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("[" +
                    "{\"bankCode\":\"341\",\"agency\":\"1111\",\"accountNumber\":\"1\",\"accountType\":\"checking\",\"purpose\":\"disbursement\",\"isPrimary\":true}," +
                    "{\"bankCode\":\"001\",\"agency\":\"2222\",\"accountNumber\":\"2\",\"accountType\":\"checking\",\"purpose\":\"disbursement\",\"isPrimary\":true}]")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("multiple_primary_per_purpose"));
    }

    @Test
    void returns404ForAnotherTenant() throws Exception {
        Customer c = seedCustomer();
        mockMvc.perform(put("/customers/" + c.id())
                .with(authentication(principal(UUID.randomUUID(), "customers.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("[]")))
            .andExpect(status().isNotFound());
    }
}
