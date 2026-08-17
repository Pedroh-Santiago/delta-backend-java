package br.com.deltaglobalbank.products.features.lending.createLendingProduct;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.products.TestcontainersConfiguration;
import br.com.deltaglobalbank.products.infrastructure.persistence.repositories.JpaProductRepository;
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
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
class CreateLendingProductTests {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    JpaProductRepository jpaProductRepository;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID subject = UUID.randomUUID();

    private JwtAuthenticationToken principal(String... roles) {
        return principalForTenant(tenantId, subject, roles);
    }

    private JwtAuthenticationToken principalForTenant(UUID tenant, String... roles) {
        return principalForTenant(tenant, UUID.randomUUID(), roles);
    }

    private JwtAuthenticationToken principalForTenant(UUID tenant, UUID subj, String... roles) {
        return new JwtAuthenticationToken(
            new AuthenticatedPrincipal(
                subj,
                tenant,
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
            "agreementName": "INSS",
            "displayName": "Consignado INSS Premium",
            "minMonthlyRate": 1.5,
            "maxMonthlyRate": 3.0,
            "minMonths": 12,
            "maxMonths": 60,
            "minAmount": 1000.00,
            "maxAmount": 50000.00,
            "commissionRate": 2.0
        }""";

    @Test
    void createsLendingProductAndReturns201() throws Exception {
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("products.lending.create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.type").value("LENDING"))
            .andExpect(jsonPath("$.active").value(true))
            .andExpect(jsonPath("$.tenantId").value(tenantId.toString()));

        boolean saved = jpaProductRepository
            .existsByTenantIdAndTypeAndAgreementNameIgnoreCaseAndActiveTrue(tenantId, "LENDING", "INSS");
        Assertions.assertEquals(true, saved);
    }

    @Test
    void usesDefaultDisplayNameWhenOmitted() throws Exception {
        String noDisplayName = validPayload.replace("\"displayName\": \"Consignado INSS Premium\",", "");
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("products.lending.create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(noDisplayName))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.displayName").value("Consignado INSS"));
    }

    @Test
    void acceptsNullCommissionRate() throws Exception {
        String noCommission = validPayload.replace("\"commissionRate\": 2.0", "\"commissionRate\": null");
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("products.lending.create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(noCommission))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.commissionRate").value((Object) null));
    }

    @Test
    void acceptsTheAdminRoleAsWell() throws Exception {
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("products.lending.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
            .andExpect(status().isCreated());
    }

    @Test
    void persistsLendingTypeEvenIfBodySendsAnotherType() throws Exception {
        String withExtraType = validPayload.replace(
            "\"agreementName\": \"INSS\",",
            "\"agreementName\": \"INSS\",\n            \"type\": \"CREDIT_CARD\",");
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("products.lending.create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(withExtraType))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.type").value("LENDING"));
    }

    @Test
    void acceptsValidBoundaryValues() throws Exception {
        String boundary = validPayload
            .replace("\"minMonthlyRate\": 1.5", "\"minMonthlyRate\": 2.0")
            .replace("\"maxMonthlyRate\": 3.0", "\"maxMonthlyRate\": 2.0")
            .replace("\"minMonths\": 12", "\"minMonths\": 1")
            .replace("\"maxMonths\": 60", "\"maxMonths\": 1");
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("products.lending.create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(boundary))
            .andExpect(status().isCreated());
    }

    @Test
    void returns400OnBlankAgreementName() throws Exception {
        String blank = validPayload.replace("\"agreementName\": \"INSS\"", "\"agreementName\": \"\"");
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("products.lending.create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(blank))
            .andExpect(status().isBadRequest());
    }

    @Test
    void returns400WhenMinMonthlyRateIsMissing() throws Exception {
        String missing = validPayload.replace("\"minMonthlyRate\": 1.5,", "");
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("products.lending.create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(missing))
            .andExpect(status().isBadRequest());
    }

    @Test
    void returns400OnNegativeMinMonthlyRate() throws Exception {
        String negative = validPayload.replace("\"minMonthlyRate\": 1.5", "\"minMonthlyRate\": -1.0");
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("products.lending.create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(negative))
            .andExpect(status().isBadRequest());
    }

    @Test
    void returns400OnNonPositiveMinMonths() throws Exception {
        String zero = validPayload.replace("\"minMonths\": 12", "\"minMonths\": 0");
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("products.lending.create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(zero))
            .andExpect(status().isBadRequest());
    }

    @Test
    void returns400WhenMaxMonthlyRateBelowMin() throws Exception {
        String invalid = validPayload
            .replace("\"minMonthlyRate\": 1.5", "\"minMonthlyRate\": 3.0")
            .replace("\"maxMonthlyRate\": 3.0", "\"maxMonthlyRate\": 2.0");
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("products.lending.create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalid))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("max_monthly_rate_lt_min"));
    }

    @Test
    void returns400WhenMaxMonthsBelowMin() throws Exception {
        String invalid = validPayload
            .replace("\"minMonths\": 12", "\"minMonths\": 60")
            .replace("\"maxMonths\": 60", "\"maxMonths\": 12");
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("products.lending.create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalid))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("max_months_lt_min"));
    }

    @Test
    void returns400WhenMaxAmountBelowMin() throws Exception {
        String invalid = validPayload
            .replace("\"minAmount\": 1000.00", "\"minAmount\": 50000.00")
            .replace("\"maxAmount\": 50000.00", "\"maxAmount\": 1000.00");
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("products.lending.create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalid))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("max_amount_lt_min"));
    }

    @Test
    void returns400OnNegativeCommissionRate() throws Exception {
        String negative = validPayload.replace("\"commissionRate\": 2.0", "\"commissionRate\": -1.0");
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("products.lending.create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(negative))
            .andExpect(status().isBadRequest());
    }

    @Test
    void returns400OnMalformedJson() throws Exception {
        String malformed = validPayload.replace("\"minMonths\": 12,", "\"minMonths\": 12,,");
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("products.lending.create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformed))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("malformed_request"));
    }

    @Test
    void returns400OnWrongFieldType() throws Exception {
        String wrongType = validPayload.replace("\"minMonths\": 12", "\"minMonths\": \"abc\"");
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("products.lending.create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(wrongType))
            .andExpect(status().isBadRequest());
    }

    @Test
    void returns401WithoutToken() throws Exception {
        mockMvc.perform(post("/products/lending")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void returns403ForInsufficientRole() throws Exception {
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("products.lending.viewer")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
            .andExpect(status().isForbidden());
    }

    @Test
    void returns403ForNoRoles() throws Exception {
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
            .andExpect(status().isForbidden());
    }

    @Test
    void returns403ForRoleFromAnotherModule() throws Exception {
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("customers.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
            .andExpect(status().isForbidden());
    }

    @Test
    void returns409OnDuplicateActiveProduct() throws Exception {
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("products.lending.create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("products.lending.create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("duplicate_active_product"));
    }

    @Test
    void enforcesUniquenessCaseInsensitively() throws Exception {
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("products.lending.create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
            .andExpect(status().isCreated());

        String lowerCase = validPayload.replace("\"agreementName\": \"INSS\"", "\"agreementName\": \"inss\"");
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("products.lending.create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(lowerCase))
            .andExpect(status().isConflict());
    }

    @Test
    void allowsSameAgreementInDifferentTenants() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();

        mockMvc.perform(post("/products/lending")
                .with(authentication(principalForTenant(tenantA, "products.lending.create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/products/lending")
                .with(authentication(principalForTenant(tenantB, "products.lending.create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
            .andExpect(status().isCreated());
    }

    @Test
    void ignoresTenantIdFromBodyAndUsesTokenTenant() throws Exception {
        UUID fakeTenant = UUID.randomUUID();
        String withFakeTenant = validPayload.replace(
            "\"agreementName\": \"INSS\",",
            "\"agreementName\": \"INSS\",\n            \"tenantId\": \"" + fakeTenant + "\",");
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("products.lending.create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(withFakeTenant))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tenantId").value(tenantId.toString()));

        Assertions.assertEquals(
            true, jpaProductRepository
                .existsByTenantIdAndTypeAndAgreementNameIgnoreCaseAndActiveTrue(tenantId, "LENDING", "INSS"));
        Assertions.assertEquals(
            false, jpaProductRepository
                .existsByTenantIdAndTypeAndAgreementNameIgnoreCaseAndActiveTrue(fakeTenant, "LENDING", "INSS"));
    }

    @Test
    void responseContainsAllContractFields() throws Exception {
        mockMvc.perform(post("/products/lending")
                .with(authentication(principal("products.lending.create")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.type").value("LENDING"))
            .andExpect(jsonPath("$.agreementName").value("INSS"))
            .andExpect(jsonPath("$.minMonthlyRate").value(1.5))
            .andExpect(jsonPath("$.maxMonthlyRate").value(3.0))
            .andExpect(jsonPath("$.minMonths").value(12))
            .andExpect(jsonPath("$.maxMonths").value(60))
            .andExpect(jsonPath("$.active").value(true))
            .andExpect(jsonPath("$.createdAt").exists());
    }
}
