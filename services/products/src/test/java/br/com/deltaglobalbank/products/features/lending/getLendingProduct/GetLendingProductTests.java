package br.com.deltaglobalbank.products.features.lending.getLendingProduct;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.products.TestcontainersConfiguration;
import br.com.deltaglobalbank.products.domain.product.AgreementName;
import br.com.deltaglobalbank.products.domain.product.DisplayName;
import br.com.deltaglobalbank.products.domain.product.Product;
import br.com.deltaglobalbank.products.domain.product.ProductType;
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
class GetLendingProductTests {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    JpaProductRepository jpaProductRepository;

    private final UUID tenantId = UUID.randomUUID();

    private JwtAuthenticationToken principal(String... roles) {
        return principalForTenant(tenantId, roles);
    }

    private JwtAuthenticationToken principalForTenant(UUID tenant, String... roles) {
        return new JwtAuthenticationToken(
            new AuthenticatedPrincipal(
                UUID.randomUUID(), tenant, "user",
                List.of(roles), List.of(),
                false, UUID.randomUUID()
            )
        );
    }

    private UUID seedProduct(UUID tenant, String agreementName, boolean active) {
        Product product = Product.newProduct(
            UUID.randomUUID(), tenant, ProductType.LENDING,
            new AgreementName(agreementName), new DisplayName("Produto"),
            new BigDecimal("1.5"), new BigDecimal("3.0"),
            12, 60,
            new BigDecimal("1000.00"), new BigDecimal("50000.00"),
            new BigDecimal("2.0"), UUID.randomUUID()
        );
        jpaProductRepository.save(ProductMapper.toEntity(product));
        return product.getId();
    }

    private UUID seedProduct(UUID tenant) {
        return seedProduct(tenant, "INSS", true);
    }

    @Test
    void returns200WithFullProductForOwnTenant() throws Exception {
        UUID id = seedProduct(tenantId);
        mockMvc.perform(get("/products/lending/" + id)
                .with(authentication(principal("products.lending.read"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.type").value("LENDING"))
            .andExpect(jsonPath("$.agreementName").value("INSS"));
    }

    @Test
    void returns404ForNonexistentProduct() throws Exception {
        mockMvc.perform(get("/products/lending/" + UUID.randomUUID())
                .with(authentication(principal("products.lending.read"))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("product_not_found"));
    }

    @Test
    void returns404ForProductFromAnotherTenant() throws Exception {
        UUID tenantB = UUID.randomUUID();
        UUID idInB = seedProduct(tenantB);
        mockMvc.perform(get("/products/lending/" + idInB)
                .with(authentication(principal("products.lending.read"))))
            .andExpect(status().isNotFound());
    }

    @Test
    void returns401WithoutToken() throws Exception {
        mockMvc.perform(get("/products/lending/" + UUID.randomUUID()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void returns403ForInsufficientRole() throws Exception {
        UUID id = seedProduct(tenantId);
        mockMvc.perform(get("/products/lending/" + id)
                .with(authentication(principal("customers.admin"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminRoleAlsoAccesses() throws Exception {
        UUID id = seedProduct(tenantId);
        mockMvc.perform(get("/products/lending/" + id)
                .with(authentication(principal("products.lending.admin"))))
            .andExpect(status().isOk());
    }

    @Test
    void platformAdminGetsProductViaAdminRoute() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID id = seedProduct(tenantA);
        mockMvc.perform(get("/products/tenants/" + tenantA + "/lending/" + id)
                .with(authentication(principal("platform.admin"))))
            .andExpect(status().isOk());
    }

    @Test
    void adminRouteReturns404WhenPathTenantIsNotTheOwner() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID idInB = seedProduct(tenantB);
        mockMvc.perform(get("/products/tenants/" + tenantA + "/lending/" + idInB)
                .with(authentication(principal("platform.admin"))))
            .andExpect(status().isNotFound());
    }

    @Test
    void adminRouteReturns403ForNonPlatformAdmin() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID id = seedProduct(tenantA);
        mockMvc.perform(get("/products/tenants/" + tenantA + "/lending/" + id)
                .with(authentication(principal("products.lending.read"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminRouteReturns401WithoutToken() throws Exception {
        mockMvc.perform(get("/products/tenants/" + UUID.randomUUID() + "/lending/" + UUID.randomUUID()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void platformAdminAccessesTenantDifferentFromOwnJwt() throws Exception {
        UUID ownTenant = UUID.randomUUID();
        UUID targetTenant = UUID.randomUUID();
        UUID id = seedProduct(targetTenant);
        mockMvc.perform(get("/products/tenants/" + targetTenant + "/lending/" + id)
                .with(authentication(principalForTenant(ownTenant, "platform.admin"))))
            .andExpect(status().isOk());
    }
}
