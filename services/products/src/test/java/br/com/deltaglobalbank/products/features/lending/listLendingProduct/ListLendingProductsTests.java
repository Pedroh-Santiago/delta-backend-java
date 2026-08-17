package br.com.deltaglobalbank.products.features.lending.listLendingProduct;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class ListLendingProductsTests {

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
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Product product = new Product(
            id, tenant, ProductType.LENDING,
            new AgreementName(agreementName), new DisplayName("Produto"),
            new BigDecimal("1.5"), new BigDecimal("3.0"),
            12, 60,
            new BigDecimal("1000.00"), new BigDecimal("50000.00"),
            new BigDecimal("2.0"), active,
            now, now, null, null
        );
        jpaProductRepository.save(ProductMapper.toEntity(product));
        return id;
    }

    private UUID seedProduct() {
        return seedProduct(tenantId, "INSS", true);
    }

    private UUID seedProduct(String agreementName) {
        return seedProduct(tenantId, agreementName, true);
    }

    private UUID seedProduct(String agreementName, boolean active) {
        return seedProduct(tenantId, agreementName, active);
    }

    private UUID seedProduct(UUID tenant, String agreementName) {
        return seedProduct(tenant, agreementName, true);
    }

    @Test
    void returnsAllProductsWithPaginationWrapper() throws Exception {
        for (int i = 0; i < 3; i++) {
            seedProduct("INSS-" + i);
        }
        mockMvc.perform(get("/products/lending")
                .with(authentication(principal("products.lending.read"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(3))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void paginatesWithPageAndSizeParams() throws Exception {
        for (int i = 0; i < 3; i++) {
            seedProduct("INSS-" + i);
        }
        mockMvc.perform(get("/products/lending?page=0&size=2")
                .with(authentication(principal("products.lending.read"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(2))
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void ordersByCreatedAtDescending() throws Exception {
        UUID firstId = seedProduct("AAA");
        Thread.sleep(10);
        UUID lastId = seedProduct("BBB");
        mockMvc.perform(get("/products/lending")
                .with(authentication(principal("products.lending.read"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].id").value(lastId.toString()));
    }

    @Test
    void coercesSizeAboveMaxTo100() throws Exception {
        seedProduct();
        mockMvc.perform(get("/products/lending?size=9999")
                .with(authentication(principal("products.lending.read"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(100));
    }

    @Test
    void usesDefaultSize20WhenOmitted() throws Exception {
        seedProduct();
        mockMvc.perform(get("/products/lending")
                .with(authentication(principal("products.lending.read"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void returns401WithoutToken() throws Exception {
        mockMvc.perform(get("/products/lending"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsEmptyListForTenantWithoutProducts() throws Exception {
        mockMvc.perform(get("/products/lending")
                .with(authentication(principal("products.lending.read"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(0))
            .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void filtersByAgreementName() throws Exception {
        seedProduct("INSS");
        seedProduct("SIAPE");
        mockMvc.perform(get("/products/lending?agreementName=INSS")
                .with(authentication(principal("products.lending.read"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].agreementName").value("INSS"));
    }

    @Test
    void filtersByAgreementNameCaseInsensitively() throws Exception {
        seedProduct("INSS");
        mockMvc.perform(get("/products/lending?agreementName=inss")
                .with(authentication(principal("products.lending.read"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(1));
    }

    @Test
    void filtersByAgreementNameSubstring() throws Exception {
        seedProduct("INSS");
        mockMvc.perform(get("/products/lending?agreementName=NS")
                .with(authentication(principal("products.lending.read"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].agreementName").value("INSS"));
    }

    @Test
    void filtersActiveTrue() throws Exception {
        seedProduct("ATIVO", true);
        seedProduct("INATIVO", false);
        mockMvc.perform(get("/products/lending?active=true")
                .with(authentication(principal("products.lending.read"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].active").value(true));
    }

    @Test
    void filtersActiveFalse() throws Exception {
        seedProduct("ATIVO", true);
        seedProduct("INATIVO", false);
        mockMvc.perform(get("/products/lending?active=false")
                .with(authentication(principal("products.lending.read"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].active").value(false));
    }

    @Test
    void returnsBothActiveAndInactiveWhenActiveFilterIsAbsent() throws Exception {
        seedProduct("ATIVO", true);
        seedProduct("INATIVO", false);
        mockMvc.perform(get("/products/lending")
                .with(authentication(principal("products.lending.read"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    void combinesAgreementNameAndActiveFilters() throws Exception {
        seedProduct("INSS", true);
        seedProduct("INSS", false);
        seedProduct("SIAPE", true);
        mockMvc.perform(get("/products/lending?agreementName=INSS&active=true")
                .with(authentication(principal("products.lending.read"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].agreementName").value("INSS"))
            .andExpect(jsonPath("$.items[0].active").value(true));
    }

    @Test
    void listsOnlyOwnTenantProducts() throws Exception {
        UUID tenantA = tenantId;
        UUID tenantB = UUID.randomUUID();
        seedProduct(tenantA, "A1");
        seedProduct(tenantA, "A2");
        seedProduct(tenantB, "B1");
        mockMvc.perform(get("/products/lending")
                .with(authentication(principal("products.lending.read"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void platformAdminListsProductsOfASpecificTenant() throws Exception {
        UUID tenantA = UUID.randomUUID();
        seedProduct(tenantA, "A1");
        seedProduct(tenantA, "A2");
        seedProduct(UUID.randomUUID(), "OTHER");
        mockMvc.perform(get("/products/tenants/" + tenantA + "/lending")
                .with(authentication(principal("platform.admin"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2));
    }
}
