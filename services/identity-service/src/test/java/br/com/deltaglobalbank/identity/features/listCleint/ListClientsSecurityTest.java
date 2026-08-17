package br.com.deltaglobalbank.identity.features.listCleint;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.TestcontainersConfiguration;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiClientEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiKeyEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiClientRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiKeyRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantRepository;
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class ListClientsSecurityTest {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @Autowired
    private JpaTenantRepository jpaTenantRepository;

    @Autowired
    private JpaApiClientRepository jpaApiClientRepository;

    @Autowired
    private JpaApiKeyRepository jpaApiKeyRepository;

    private JwtAuthenticationToken auth(UUID tenantId, String... roles) {
        return new JwtAuthenticationToken(new AuthenticatedPrincipal(
            UUID.randomUUID(), tenantId, "user", List.of(roles), List.of(), false, UUID.randomUUID()));
    }

    private UUID seedTenant(String slug) {
        UUID id = UUID.randomUUID();
        jpaTenantRepository.save(new TenantEntity(id, slug, slug, "active", Instant.now(), Instant.now(), null));
        return id;
    }

    private void seedClient(UUID tenantId, String name) {
        jpaApiClientRepository.save(new ApiClientEntity(
            UUID.randomUUID(), tenantId, name, null, "active", Instant.now(), Instant.now(), null));
    }

    private void seedKey(UUID apiClientId, String name, boolean revoked) {
        String unique = UUID.randomUUID().toString();
        jpaApiKeyRepository.save(new ApiKeyEntity(
            UUID.randomUUID(), apiClientId, name, "hash-" + unique, "dgb_" + unique.substring(0, 8),
            "fp-" + unique, null, null, revoked ? Instant.now() : null, Instant.now(), Instant.now(), null));
    }

    @Test
    void shouldReturn403WhenIdentityAdminCallsCrossTenantEndpoint() throws Exception {
        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(
            get("/admin/tenants/" + tenantId + "/api-clients")
                .with(authentication(auth(UUID.randomUUID(), "identity.admin")))
        ).andExpect(status().isForbidden());
    }

    @Test
    void identityAdminShouldListOnlyClientsFromItsOwnTenant() throws Exception {
        UUID tenantA = seedTenant("tenant-a");
        UUID tenantB = seedTenant("tenant-b");
        seedClient(tenantA, "client-a1");
        seedClient(tenantA, "client-a2");
        seedClient(tenantB, "client-b1");

        mockMvc.perform(
            get("/admin/api-clients")
                .with(authentication(auth(tenantA, "identity.admin")))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    void platformAdminShouldListClientsOfAnyTenant() throws Exception {
        UUID tenantB = seedTenant("tenant-b-platform");
        seedClient(tenantB, "client-b1");
        seedClient(tenantB, "client-b2");

        mockMvc.perform(
            get("/admin/tenants/" + tenantB + "/api-clients")
                .with(authentication(auth(UUID.randomUUID(), "platform.admin")))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    void shouldReturn401WhenRequestHasNoAuthentication() throws Exception {
        mockMvc.perform(
            get("/admin/api-clients")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void activeKeysCountShouldCountOnlyActiveKeys() throws Exception {
        UUID tenantId = seedTenant("tenant-keys");

        UUID clientId = UUID.randomUUID();
        jpaApiClientRepository.save(new ApiClientEntity(
            clientId, tenantId, "client-keys", null, "active", Instant.now(), Instant.now(), null));

        seedKey(clientId, "k-active", false);
        seedKey(clientId, "k-revoked", true);

        mockMvc.perform(
            get("/admin/api-clients")
                .with(authentication(auth(tenantId, "identity.admin")))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].activeKeysCount").value(1));
    }

    @Test
    void shouldReturn401WhenTokenIsInvalid() throws Exception {
        mockMvc.perform(
            get("/admin/api-clients")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.invalido")
        ).andExpect(status().isUnauthorized());
    }
}
