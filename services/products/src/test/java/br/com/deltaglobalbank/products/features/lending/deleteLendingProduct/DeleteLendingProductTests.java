package br.com.deltaglobalbank.products.features.lending.deleteLendingProduct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class DeleteLendingProductTests {

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
        return seedProduct(tenantId, "INSS", true, null);
    }

    @Test
    void softDeletesWithoutRemovingTheRow() throws Exception {
        UUID id = seedProduct();
        mockMvc.perform(delete("/products/lending/" + id)
                .with(authentication(principal("products.lending.delete"))))
            .andExpect(status().isNoContent());

        ProductEntity entity = jpaProductRepository.findById(id).orElse(null);
        assertNotNull(entity);
        assertEquals(false, entity.isActive());
    }

    @Test
    void updatesUpdatedAtAndUpdatedByOnDelete() throws Exception {
        UUID id = seedProduct(tenantId, "INSS", true, UUID.randomUUID());
        ProductEntity before = jpaProductRepository.findById(id).orElse(null);
        Instant originalUpdatedAt = before.getUpdatedAt();

        mockMvc.perform(delete("/products/lending/" + id)
                .with(authentication(principal("products.lending.delete"))))
            .andExpect(status().isNoContent());

        ProductEntity after = jpaProductRepository.findById(id).orElse(null);
        assertTrue(after.getUpdatedAt().isAfter(originalUpdatedAt) || !after.getUpdatedAt().equals(originalUpdatedAt));
        assertEquals(subject, after.getUpdatedBy());
    }

    @Test
    void doesNotPhysicallyRemoveTheRow() throws Exception {
        UUID id = seedProduct();
        long countBefore = jpaProductRepository.count();
        mockMvc.perform(delete("/products/lending/" + id)
                .with(authentication(principal("products.lending.delete"))))
            .andExpect(status().isNoContent());
        assertEquals(countBefore, jpaProductRepository.count());
    }

    @Test
    void returns204NoContentWithEmptyBody() throws Exception {
        UUID id = seedProduct();
        mockMvc.perform(delete("/products/lending/" + id)
                .with(authentication(principal("products.lending.delete"))))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));
    }

    @Test
    void disappearsFromActiveListingAfterDelete() throws Exception {
        UUID id = seedProduct(tenantId, "INSS", true, null);
        mockMvc.perform(delete("/products/lending/" + id)
                .with(authentication(principal("products.lending.delete"))))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/products/lending?active=true")
                .with(authentication(principal("products.lending.read"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void appearsInInactiveListingAfterDelete() throws Exception {
        UUID id = seedProduct(tenantId, "INSS", true, null);
        mockMvc.perform(delete("/products/lending/" + id)
                .with(authentication(principal("products.lending.delete"))))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/products/lending?active=false")
                .with(authentication(principal("products.lending.read"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].id").value(id.toString()));
    }

    @Test
    void detailStillAccessibleByIdAfterDelete() throws Exception {
        UUID id = seedProduct();
        mockMvc.perform(delete("/products/lending/" + id)
                .with(authentication(principal("products.lending.delete"))))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/products/lending/" + id)
                .with(authentication(principal("products.lending.read"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void unfilteredListingStillShowsTheInactiveProduct() throws Exception {
        UUID id = seedProduct();
        mockMvc.perform(delete("/products/lending/" + id)
                .with(authentication(principal("products.lending.delete"))))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/products/lending")
                .with(authentication(principal("products.lending.read"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].active").value(false));
    }

    @Test
    void deletingTwiceReturns204BothTimes() throws Exception {
        UUID id = seedProduct();
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(delete("/products/lending/" + id)
                    .with(authentication(principal("products.lending.delete"))))
                .andExpect(status().isNoContent());
        }
    }

    @Test
    void deletingAnAlreadyInactiveProductReturns204() throws Exception {
        UUID id = seedProduct(tenantId, "INSS", false, null);
        mockMvc.perform(delete("/products/lending/" + id)
                .with(authentication(principal("products.lending.delete"))))
            .andExpect(status().isNoContent());

        ProductEntity entity = jpaProductRepository.findById(id).orElse(null);
        assertEquals(false, entity.isActive());
    }

    @Test
    void returns404ForNonexistentProduct() throws Exception {
        mockMvc.perform(delete("/products/lending/" + UUID.randomUUID())
                .with(authentication(principal("products.lending.delete"))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("product_not_found"));
    }

    @Test
    void returns404ForProductFromAnotherTenant() throws Exception {
        UUID idInB = seedProduct(UUID.randomUUID(), "INSS", true, null);
        mockMvc.perform(delete("/products/lending/" + idInB)
                .with(authentication(principal("products.lending.delete"))))
            .andExpect(status().isNotFound());
    }

    @Test
    void returns401WithoutToken() throws Exception {
        mockMvc.perform(delete("/products/lending/" + UUID.randomUUID()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void returns403ForInsufficientRole() throws Exception {
        UUID id = seedProduct();
        mockMvc.perform(delete("/products/lending/" + id)
                .with(authentication(principal("products.lending.read"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void deleteRoleAllowsDeletion() throws Exception {
        UUID id = seedProduct();
        mockMvc.perform(delete("/products/lending/" + id)
                .with(authentication(principal("products.lending.delete"))))
            .andExpect(status().isNoContent());
    }

    @Test
    void adminRoleAllowsDeletion() throws Exception {
        UUID id = seedProduct();
        mockMvc.perform(delete("/products/lending/" + id)
                .with(authentication(principal("products.lending.admin"))))
            .andExpect(status().isNoContent());
    }

    @Test
    void returns403ForRoleFromAnotherModule() throws Exception {
        UUID id = seedProduct();
        mockMvc.perform(delete("/products/lending/" + id)
                .with(authentication(principal("customers.admin"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void deletingOwnProductLeavesOtherTenantProductIntact() throws Exception {
        UUID idA = seedProduct(tenantId, "A", true, null);
        UUID idB = seedProduct(UUID.randomUUID(), "B", true, null);

        mockMvc.perform(delete("/products/lending/" + idA)
                .with(authentication(principal("products.lending.delete"))))
            .andExpect(status().isNoContent());

        ProductEntity entityB = jpaProductRepository.findById(idB).orElse(null);
        assertEquals(true, entityB.isActive());
    }

    @Test
    void cannotDeleteAnotherTenantProductAndItStaysUnchanged() throws Exception {
        UUID idB = seedProduct(UUID.randomUUID(), "INSS", true, null);

        mockMvc.perform(delete("/products/lending/" + idB)
                .with(authentication(principal("products.lending.delete"))))
            .andExpect(status().isNotFound());

        ProductEntity entityB = jpaProductRepository.findById(idB).orElse(null);
        assertEquals(true, entityB.isActive());
    }

    @Test
    void platformAdminDeactivatesProductOfATenantViaPath() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID id = seedProduct(tenantA, "INSS", true, null);
        mockMvc.perform(delete("/products/tenants/" + tenantA + "/lending/" + id)
                .with(authentication(principal("platform.admin"))))
            .andExpect(status().isNoContent());

        ProductEntity entity = jpaProductRepository.findById(id).orElse(null);
        assertEquals(false, entity.isActive());
    }

    @Test
    void adminRouteReturns404WhenProductNotInPathTenant() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID idInB = seedProduct(UUID.randomUUID(), "INSS", true, null);
        mockMvc.perform(delete("/products/tenants/" + tenantA + "/lending/" + idInB)
                .with(authentication(principal("platform.admin"))))
            .andExpect(status().isNotFound());
    }

    @Test
    void adminRouteReturns403ForNonPlatformAdmin() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID id = seedProduct(tenantA, "INSS", true, null);
        mockMvc.perform(delete("/products/tenants/" + tenantA + "/lending/" + id)
                .with(authentication(principal("products.lending.delete"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminRouteReturns401WithoutToken() throws Exception {
        mockMvc.perform(delete("/products/tenants/" + UUID.randomUUID() + "/lending/" + UUID.randomUUID()))
            .andExpect(status().isUnauthorized());
    }
}
