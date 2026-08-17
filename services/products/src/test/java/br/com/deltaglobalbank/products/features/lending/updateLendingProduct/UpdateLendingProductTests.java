package br.com.deltaglobalbank.products.features.lending.updateLendingProduct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.products.TestcontainersConfiguration;
import br.com.deltaglobalbank.products.domain.product.AgreementName;
import br.com.deltaglobalbank.products.domain.product.DisplayName;
import br.com.deltaglobalbank.products.domain.product.Product;
import br.com.deltaglobalbank.products.domain.product.ProductType;
import br.com.deltaglobalbank.products.infrastructure.persistence.entities.ProductEntity;
import br.com.deltaglobalbank.products.infrastructure.persistence.mappers.ProductMapper;
import br.com.deltaglobalbank.products.infrastructure.persistence.repositories.JpaProductRepository;
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
class UpdateLendingProductTests {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    JpaProductRepository jpaProductRepository;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID subject = UUID.randomUUID();

    private JwtAuthenticationToken principal(String... roles) {
        return principalForTenant(tenantId, subject, roles);
    }

    private JwtAuthenticationToken principalForTenant(UUID tenant, UUID subj, String... roles) {
        return new JwtAuthenticationToken(
            new AuthenticatedPrincipal(
                subj, tenant, "user",
                List.of(roles), List.of(),
                false, UUID.randomUUID()
            )
        );
    }

    private UUID seedProduct(UUID tenant, String agreementName, boolean active, UUID createdBy) {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Product product = new Product(
            id, tenant, ProductType.LENDING,
            new AgreementName(agreementName), new DisplayName("Produto"),
            new BigDecimal("1.5"), new BigDecimal("3.0"),
            12, 60,
            new BigDecimal("1000.00"), new BigDecimal("50000.00"),
            new BigDecimal("2.0"), active,
            now, now, createdBy, createdBy
        );
        jpaProductRepository.save(ProductMapper.toEntity(product));
        return id;
    }

    private UUID seedProduct() {
        return seedProduct(tenantId, "INSS", true, UUID.randomUUID());
    }

    private String payload(
        String agreementName,
        boolean active,
        String minMonthlyRate,
        String maxMonthlyRate,
        int minMonths,
        int maxMonths,
        String minAmount,
        String maxAmount,
        String commissionRate,
        String displayName
    ) {
        String displayLine = displayName != null ? "\"displayName\": \"" + displayName + "\"," : "";
        String commissionLine = commissionRate != null
            ? "\"commissionRate\": " + commissionRate
            : "\"commissionRate\": null";
        return "{\n"
            + "    \"agreementName\": \"" + agreementName + "\",\n"
            + "    " + displayLine + "\n"
            + "    \"minMonthlyRate\": " + minMonthlyRate + ",\n"
            + "    \"maxMonthlyRate\": " + maxMonthlyRate + ",\n"
            + "    \"minMonths\": " + minMonths + ",\n"
            + "    \"maxMonths\": " + maxMonths + ",\n"
            + "    \"minAmount\": " + minAmount + ",\n"
            + "    \"maxAmount\": " + maxAmount + ",\n"
            + "    \"active\": " + active + ",\n"
            + "    " + commissionLine + "\n"
            + "}";
    }

    private String payload() {
        return payload("INSS", true, "2.0", "4.0", 6, 48, "2000.00", "60000.00", "1.0", "Consignado Custom");
    }

    @Test
    void updatesRateTermAndAmountOfOwnTenantProduct() throws Exception {
        UUID id = seedProduct();
        mockMvc.perform(put("/products/lending/" + id)
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload("INSS", true, "2.5", "4.0", 24, 48, "2000.00", "60000.00", "1.0", "Consignado Custom")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.minMonthlyRate").value(2.5))
            .andExpect(jsonPath("$.minMonths").value(24));
        ProductEntity saved = jpaProductRepository.findById(id).orElse(null);
        assertEquals(0, new BigDecimal("2.5").compareTo(saved.getMinMonthlyRate()));
        assertEquals(24, saved.getMinMonths());
    }

    @Test
    void updatesUpdatedAtAndUpdatedBy() throws Exception {
        UUID id = seedProduct();
        Instant before = jpaProductRepository.findById(id).orElse(null).getUpdatedAt();
        mockMvc.perform(put("/products/lending/" + id)
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload()))
            .andExpect(status().isOk());
        ProductEntity after = jpaProductRepository.findById(id).orElse(null);
        assertNotEquals(before, after.getUpdatedAt());
        assertEquals(subject, after.getUpdatedBy());
    }

    @Test
    void preservesCreatedAtAndCreatedBy() throws Exception {
        UUID creator = UUID.randomUUID();
        UUID id = seedProduct(tenantId, "INSS", true, creator);
        ProductEntity before = jpaProductRepository.findById(id).orElse(null);
        Instant originalCreatedAt = before.getCreatedAt();

        mockMvc.perform(put("/products/lending/" + id)
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload()))
            .andExpect(status().isOk());

        ProductEntity after = jpaProductRepository.findById(id).orElse(null);
        assertEquals(originalCreatedAt, after.getCreatedAt());
        assertEquals(creator, after.getCreatedBy());
    }

    @Test
    void responseMatchesFullContract() throws Exception {
        UUID id = seedProduct();
        mockMvc.perform(put("/products/lending/" + id)
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.type").value("LENDING"))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void usesDefaultDisplayNameWhenOmitted() throws Exception {
        UUID id = seedProduct();
        mockMvc.perform(put("/products/lending/" + id)
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload("INSS", true, "2.0", "4.0", 6, 48, "2000.00", "60000.00", "1.0", null)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.displayName").value("Consignado INSS"));
    }

    @Test
    void adminRoleAlsoUpdates() throws Exception {
        UUID id = seedProduct();
        mockMvc.perform(put("/products/lending/" + id)
                .with(authentication(principal("products.lending.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload()))
            .andExpect(status().isOk());
    }

    @Test
    void keepsLendingTypeEvenIfBodySendsAnotherType() throws Exception {
        UUID id = seedProduct();
        String withType = payload().replace(
            "\"agreementName\": \"INSS\",",
            "\"agreementName\": \"INSS\",\n            \"type\": \"CREDIT_CARD\",");
        mockMvc.perform(put("/products/lending/" + id)
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(withType))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("LENDING"));
    }

    @Test
    void returns400WhenMaxMonthlyRateBelowMin() throws Exception {
        UUID id = seedProduct();
        mockMvc.perform(put("/products/lending/" + id)
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload("INSS", true, "3.0", "2.0", 6, 48, "2000.00", "60000.00", "1.0", "Consignado Custom")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("max_monthly_rate_lt_min"));
    }

    @Test
    void returns400WhenMaxMonthsBelowMin() throws Exception {
        UUID id = seedProduct();
        mockMvc.perform(put("/products/lending/" + id)
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload("INSS", true, "2.0", "4.0", 60, 12, "2000.00", "60000.00", "1.0", "Consignado Custom")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("max_months_lt_min"));
    }

    @Test
    void returns400WhenMaxAmountBelowMin() throws Exception {
        UUID id = seedProduct();
        mockMvc.perform(put("/products/lending/" + id)
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload("INSS", true, "2.0", "4.0", 6, 48, "50000.00", "1000.00", "1.0", "Consignado Custom")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("max_amount_lt_min"));
    }

    @Test
    void returns400OnNegativeMinMonthlyRate() throws Exception {
        UUID id = seedProduct();
        mockMvc.perform(put("/products/lending/" + id)
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload("INSS", true, "-1.0", "4.0", 6, 48, "2000.00", "60000.00", "1.0", "Consignado Custom")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void returns400OnZeroMinMonths() throws Exception {
        UUID id = seedProduct();
        mockMvc.perform(put("/products/lending/" + id)
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload("INSS", true, "2.0", "4.0", 0, 48, "2000.00", "60000.00", "1.0", "Consignado Custom")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void returns400OnBlankAgreementName() throws Exception {
        UUID id = seedProduct();
        mockMvc.perform(put("/products/lending/" + id)
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload("", true, "2.0", "4.0", 6, 48, "2000.00", "60000.00", "1.0", "Consignado Custom")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void returns400OnNegativeCommissionRate() throws Exception {
        UUID id = seedProduct();
        mockMvc.perform(put("/products/lending/" + id)
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload("INSS", true, "2.0", "4.0", 6, 48, "2000.00", "60000.00", "-1.0", "Consignado Custom")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void returns400WhenRequiredFieldIsMissing() throws Exception {
        UUID id = seedProduct();
        String missing = payload().replace("\"minMonthlyRate\": 2.0,", "");
        mockMvc.perform(put("/products/lending/" + id)
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(missing))
            .andExpect(status().isBadRequest());
    }

    @Test
    void allowsKeepingSameAgreementNameWhenEditingOnlyTheRate() throws Exception {
        UUID id = seedProduct(tenantId, "INSS", true, UUID.randomUUID());
        mockMvc.perform(put("/products/lending/" + id)
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload("INSS", true, "2.5", "4.0", 6, 48, "2000.00", "60000.00", "1.0", "Consignado Custom")))
            .andExpect(status().isOk());
    }

    @Test
    void returns409WhenChangingAgreementToOneAlreadyActive() throws Exception {
        seedProduct(tenantId, "SIAPE", true, UUID.randomUUID());
        UUID id = seedProduct(tenantId, "INSS", true, UUID.randomUUID());
        mockMvc.perform(put("/products/lending/" + id)
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload("SIAPE", true, "2.0", "4.0", 6, 48, "2000.00", "60000.00", "1.0", "Consignado Custom")))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("duplicate_active_product"));
    }

    @Test
    void allowsChangingAgreementToANameThatIsOnlyInactiveElsewhere() throws Exception {
        seedProduct(tenantId, "SIAPE", false, UUID.randomUUID());
        UUID id = seedProduct(tenantId, "INSS", true, UUID.randomUUID());
        mockMvc.perform(put("/products/lending/" + id)
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload("SIAPE", true, "2.0", "4.0", 6, 48, "2000.00", "60000.00", "1.0", "Consignado Custom")))
            .andExpect(status().isOk());
    }

    @Test
    void enforcesUniquenessCaseInsensitivelyOnUpdate() throws Exception {
        seedProduct(tenantId, "SIAPE", true, UUID.randomUUID());
        UUID id = seedProduct(tenantId, "INSS", true, UUID.randomUUID());
        mockMvc.perform(put("/products/lending/" + id)
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload("siape", true, "2.0", "4.0", 6, 48, "2000.00", "60000.00", "1.0", "Consignado Custom")))
            .andExpect(status().isConflict());
    }

    @Test
    void returns409WhenReactivatingWithACollidingName() throws Exception {
        seedProduct(tenantId, "INSS", true, UUID.randomUUID());
        UUID y = seedProduct(tenantId, "INSS", false, UUID.randomUUID());
        mockMvc.perform(put("/products/lending/" + y)
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload("INSS", true, "2.0", "4.0", 6, 48, "2000.00", "60000.00", "1.0", "Consignado Custom")))
            .andExpect(status().isConflict());
    }

    @Test
    void allowsSettingInactiveEvenWithACollidingName() throws Exception {
        seedProduct(tenantId, "INSS", true, UUID.randomUUID());
        UUID id = seedProduct(tenantId, "INSS", false, UUID.randomUUID());
        mockMvc.perform(put("/products/lending/" + id)
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload("INSS", false, "2.0", "4.0", 6, 48, "2000.00", "60000.00", "1.0", "Consignado Custom")))
            .andExpect(status().isOk());
    }

    @Test
    void returns404ForNonexistentProduct() throws Exception {
        mockMvc.perform(put("/products/lending/" + UUID.randomUUID())
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("product_not_found"));
    }

    @Test
    void returns404ForProductFromAnotherTenant() throws Exception {
        UUID idInB = seedProduct(UUID.randomUUID(), "INSS", true, UUID.randomUUID());
        mockMvc.perform(put("/products/lending/" + idInB)
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload()))
            .andExpect(status().isNotFound());
    }

    @Test
    void updatingOwnProductDoesNotAffectOtherTenantProduct() throws Exception {
        UUID idA = seedProduct(tenantId, "A", true, UUID.randomUUID());
        UUID idB = seedProduct(UUID.randomUUID(), "B", true, UUID.randomUUID());
        mockMvc.perform(put("/products/lending/" + idA)
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload("A", true, "2.9", "4.0", 6, 48, "2000.00", "60000.00", "1.0", "Consignado Custom")))
            .andExpect(status().isOk());

        ProductEntity entityB = jpaProductRepository.findById(idB).orElse(null);
        assertEquals(0, new BigDecimal("1.5").compareTo(entityB.getMinMonthlyRate()));
    }

    @Test
    void returns401WithoutToken() throws Exception {
        mockMvc.perform(put("/products/lending/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void returns403ForInsufficientRole() throws Exception {
        UUID id = seedProduct();
        mockMvc.perform(put("/products/lending/" + id)
                .with(authentication(principal("products.lending.read")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload()))
            .andExpect(status().isForbidden());
    }

    @Test
    void returns403ForRoleFromAnotherModule() throws Exception {
        UUID id = seedProduct();
        mockMvc.perform(put("/products/lending/" + id)
                .with(authentication(principal("customers.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload()))
            .andExpect(status().isForbidden());
    }

    @Test
    void platformAdminUpdatesProductViaAdminRoute() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID id = seedProduct(tenantA, "INSS", true, UUID.randomUUID());
        mockMvc.perform(put("/products/tenants/" + tenantA + "/lending/" + id)
                .with(authentication(principal("platform.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload()))
            .andExpect(status().isOk());
    }

    @Test
    void adminRouteReturns404WhenProductNotInPathTenant() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID idInB = seedProduct(UUID.randomUUID(), "INSS", true, UUID.randomUUID());
        mockMvc.perform(put("/products/tenants/" + tenantA + "/lending/" + idInB)
                .with(authentication(principal("platform.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload()))
            .andExpect(status().isNotFound());
    }

    @Test
    void adminRouteReturns403ForNonPlatformAdmin() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID id = seedProduct(tenantA, "INSS", true, UUID.randomUUID());
        mockMvc.perform(put("/products/tenants/" + tenantA + "/lending/" + id)
                .with(authentication(principal("products.lending.update")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload()))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminRouteReturns401WithoutToken() throws Exception {
        mockMvc.perform(put("/products/tenants/" + UUID.randomUUID() + "/lending/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload()))
            .andExpect(status().isUnauthorized());
    }
}
