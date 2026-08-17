package br.com.deltaglobalbank.identity.features.tenants.listTenants;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.TestcontainersConfiguration;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantRepository;
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class ListTenantsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaTenantRepository jpaTenantRepository;

    private JwtAuthenticationToken auth(String... roles) {
        return new JwtAuthenticationToken(new AuthenticatedPrincipal(
            UUID.randomUUID(), UUID.randomUUID(), "user", List.of(roles), List.of(), false, UUID.randomUUID()));
    }

    private UUID seedTenant(String name, String slug) {
        return seedTenant(name, slug, "active");
    }

    private UUID seedTenant(String name, String slug, String status) {
        UUID id = UUID.randomUUID();
        jpaTenantRepository.save(new TenantEntity(id, name, slug, status, Instant.now(), Instant.now(), null));
        return id;
    }

    @Test
    void platformAdminListsTenants() throws Exception {
        seedTenant("Acme Corp", "acme");
        seedTenant("Beta Inc", "beta", "suspended");

        mockMvc.perform(
            get("/admin/tenants")
                .with(authentication(auth("platform.admin")))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.items[0].name").exists())
            .andExpect(jsonPath("$.items[0].slug").exists())
            .andExpect(jsonPath("$.items[0].status").exists());
    }

    @Test
    void identityAdminCannotListTenants() throws Exception {
        mockMvc.perform(
            get("/admin/tenants")
                .with(authentication(auth("identity.admin")))
        ).andExpect(status().isForbidden());
    }

    @Test
    void paginationWorks() throws Exception {
        seedTenant("Tenant A", "tenant-a");
        seedTenant("Tenant B", "tenant-b");

        mockMvc.perform(
            get("/admin/tenants?page=0&size=1")
                .with(authentication(auth("platform.admin")))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.items.length()").value(1));
    }

    @Test
    void softDeletedTenantIsNotListed() throws Exception {
        UUID id = seedTenant("Deleted Corp", "deleted-corp");
        seedTenant("Active Corp", "active-corp");

        jpaTenantRepository.deleteById(id);

        mockMvc.perform(
            get("/admin/tenants")
                .with(authentication(auth("platform.admin")))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.items[0].slug").value("active-corp"));
    }
}
