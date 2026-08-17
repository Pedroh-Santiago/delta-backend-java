package br.com.deltaglobalbank.identity.features.users.listUsers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.TestcontainersConfiguration;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRepository;
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken;
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
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
class ListUsersCrossTenantIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaTenantRepository jpaTenantRepository;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    private JwtAuthenticationToken auth(String... roles) {
        return new JwtAuthenticationToken(new AuthenticatedPrincipal(
            UUID.randomUUID(), UUID.randomUUID(), "user", List.of(roles), List.of(), false, UUID.randomUUID()));
    }

    private UUID seedTenant(String name, String slug) {
        UUID id = UUID.randomUUID();
        jpaTenantRepository.save(new TenantEntity(id, name, slug, "active", Instant.now(), Instant.now(), null));
        return id;
    }

    private UUID seedUser(UUID tenantId, String email) {
        UUID id = UUID.randomUUID();
        jpaUserRepository.save(new UserEntity(
            id, tenantId, "New User", email, "hash", "active", false, null, null, 0, null,
            Instant.now(), Instant.now(), null));
        return id;
    }

    @Test
    void platformAdminListsUsersOfSpecificTenant() throws Exception {
        UUID tenantA = seedTenant("Tenant A", "tenant-a");
        UUID tenantB = seedTenant("Tenant B", "tenant-b");
        seedUser(tenantA, "a@example.com");
        seedUser(tenantB, "b@example.com");

        mockMvc.perform(
            get("/admin/tenants/{tenantId}/users", tenantA)
                .with(authentication(auth("platform.admin")))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].email").value("a@example.com"))
            .andExpect(jsonPath("$.items[0].tenantId").value(tenantA.toString()));
    }

    @Test
    void identityAdminCannotListUsersCrossTenant() throws Exception {
        UUID tenantId = seedTenant("Tenant A", "tenant-a");

        mockMvc.perform(
            get("/admin/tenants/{tenantId}/users", tenantId)
                .with(authentication(auth("identity.admin")))
        ).andExpect(status().isForbidden());
    }

    @Test
    void unknownTenantReturns404() throws Exception {
        mockMvc.perform(
            get("/admin/tenants/{tenantId}/users", UUID.randomUUID())
                .with(authentication(auth("platform.admin")))
        )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("tenant_not_found"));
    }

    @Test
    void paginationWorksForCrossTenantList() throws Exception {
        UUID tenantId = seedTenant("Tenant A", "tenant-a");
        seedUser(tenantId, "u1@example.com");
        seedUser(tenantId, "u2@example.com");

        mockMvc.perform(
            get("/admin/tenants/{tenantId}/users?page=0&size=1", tenantId)
                .with(authentication(auth("platform.admin")))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.items.length()").value(1));
    }
}
