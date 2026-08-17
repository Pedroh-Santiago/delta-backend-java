package br.com.deltaglobalbank.customers.features.customers.createCustomer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.customers.TestcontainersConfiguration;
import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.CustomerEntity;
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaBankAccountRepository;
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaCustomerRepository;
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaPersonalDocumentRepository;
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class CreateCustomerTests {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    JpaCustomerRepository jpaCustomerRepository;
    @Autowired
    JpaBankAccountRepository jpaBankAccountRepository;
    @Autowired
    JpaPersonalDocumentRepository jpaPersonalDocumentRepository;

    private final UUID tenantId = UUID.randomUUID();

    private JwtAuthenticationToken principal(String... roles) {
        return new JwtAuthenticationToken(
            new AuthenticatedPrincipal(
                UUID.randomUUID(),
                tenantId,
                "user",
                List.of(roles),
                List.of(),
                false,
                UUID.randomUUID()
            )
        );
    }

    private final String validPayload = """
        {
            "cpf":"111.444.777-35",
            "fullName":"João da Silva",
            "birthDate":"1980-05-15",
            "gender":"male",
            "nationality":"brasileira",
            "motherName":"Maria da Silva",
            "maritalStatus":"married",
            "email":"joao@exemplo.com",
            "phone":{
                "phoneNumber":"+5511999998888"
            },
            "address":{
                "cep":"01310100",
                "street":"Av. Paulista",
                "number":"1000",
                "complement":"Apto 101",
                "neighborhood":"Bela Vista",
                "city":"São Paulo",
                "state":"SP",
                "country":"BR"
            },
          "documents":[{
                "type":"rg",
                "number":"12.345.678-9",
                "issuer":"SSP",
                "issuerState":"SP",
                "issuedAt":"2010-01-15"
          }],
          "bankAccounts":[{
                "bankCode":"341",
                "agency":"1234",
                "accountNumber":"56789",
                "accountDigit":"0",
                "accountType":"checking",
                "purpose":"disbursement",
                "isPrimary":true
          }]
        }""";

    @Test
    void createsCustomerAndReturns201() throws Exception {
        mockMvc.perform(post("/customers")
                .with(authentication(principal("customers.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.cpf").value("11144477735"))
            .andExpect(jsonPath("$.tenantId").value(tenantId.toString()))
            .andExpect(jsonPath("$.status").value("active"));

        CustomerEntity saved = jpaCustomerRepository.findByCpfAndTenantId("11144477735", tenantId);
        assertNotNull(saved);
        assertEquals(1, jpaBankAccountRepository.findAllByCustomerId(saved.getId()).size());
        assertEquals(1, jpaPersonalDocumentRepository.findAllByCustomerId(saved.getId()).size());
    }

    @Test
    void returns409OnDuplicateCpf() throws Exception {
        mockMvc.perform(post("/customers")
                .with(authentication(principal("customers.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
            .andExpect(status().isCreated());
        mockMvc.perform(post("/customers")
                .with(authentication(principal("customers.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("cpf_already_exists"));
    }

    @Test
    void returns400OnInvalidCpf() throws Exception {
        String badCpf = validPayload.replace("111.444.777-35", "111.444.777-00");
        mockMvc.perform(post("/customers")
                .with(authentication(principal("customers.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(badCpf))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("invalid_cpf"));
    }

    @Test
    void returns403ForInsufficientRole() throws Exception {
        mockMvc.perform(post("/customers")
                .with(authentication(principal("customers.viewer")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
            .andExpect(status().isForbidden());
    }

    @Test
    void returns401WithoutToken() throws Exception {
        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void returns400OnMultiplePrimaryAccountsForSamePurpose() throws Exception {
        String twoPrimaryPayload = """
            {
                "cpf":"111.444.777-35",
                "fullName":"João da Silva",
                "birthDate":"1980-05-15",
                "gender":"male",
                "nationality":"brasileira",
                "motherName":"Maria da Silva",
                "maritalStatus":"married",
                "email":"joao@exemplo.com",
                "phone":{ "phoneNumber":"+5511999998888" },
                "address":{
                    "cep":"01310100",
                    "street":"Av. Paulista",
                    "number":"1000",
                    "complement":"Apto 101",
                    "neighborhood":"Bela Vista",
                    "city":"São Paulo",
                    "state":"SP",
                    "country":"BR"
                },
                "documents":[],
                "bankAccounts":[
                    {
                        "bankCode":"341",
                        "agency":"1234",
                        "accountNumber":"1",
                        "accountType":"checking",
                        "purpose":"disbursement",
                        "isPrimary":true
                    },
                    {
                        "bankCode":"341",
                        "agency":"1234",
                        "accountNumber":"2",
                        "accountType":"checking",
                        "purpose":"disbursement",
                        "isPrimary":true
                    }
                ]
            }""";
        mockMvc.perform(post("/customers")
                .with(authentication(principal("customers.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(twoPrimaryPayload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("multiple_primary_per_purpose"));
    }
}
