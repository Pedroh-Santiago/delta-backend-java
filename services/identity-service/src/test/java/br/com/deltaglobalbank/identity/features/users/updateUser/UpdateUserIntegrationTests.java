package br.com.deltaglobalbank.identity.features.users.updateUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
class UpdateUserIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaTenantRepository jpaTenantRepository;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    private JwtAuthenticationToken auth(UUID tenant, String... roles) {
        return new JwtAuthenticationToken(new AuthenticatedPrincipal(
            UUID.randomUUID(), tenant, "user", List.of(roles), List.of(), false, UUID.randomUUID()
        ));
    }

    private UUID seedTenant(String slug) {
        UUID tenantId = UUID.randomUUID();
        jpaTenantRepository.save(new TenantEntity(tenantId, slug, slug, "active", Instant.now(), Instant.now(), null));
        return tenantId;
    }

    private UUID seedUser(UUID tenantId, String email, String name, Instant updatedAt) {
        UUID userId = UUID.randomUUID();
        jpaUserRepository.save(new UserEntity(
            userId, tenantId, name, email, "hash", "active", false,
            null, null, 0, null, Instant.now(), updatedAt, null
        ));
        return userId;
    }

    private UUID seedUser(UUID tenantId, String email) {
        return seedUser(tenantId, email, "Nome Antigo", Instant.now());
    }

    private String body(String name, String email) {
        return "{\"fullName\":\"" + name + "\",\"email\":\"" + email + "\"}";
    }

    @Test
    void updatesNameAndEmailAndPersists() throws Exception {
        UUID tenant = seedTenant("t-a");
        UUID userId = seedUser(tenant, "antigo@empresa.com");

        mockMvc.perform(
            put("/admin/users/{userId}", userId)
                .with(authentication(auth(tenant, "identity.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("João da Silva Santos", "joao.santos@empresa.com.br"))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fullName").value("João da Silva Santos"))
            .andExpect(jsonPath("$.email").value("joao.santos@empresa.com.br"));

        UserEntity saved = jpaUserRepository.findById(userId).get();
        assertEquals("joao.santos@empresa.com.br", saved.getEmail());
        assertEquals("João da Silva Santos", saved.getFullName());
    }

    @Test
    void duplicateEmailInSameTenantReturns409() throws Exception {
        UUID tenant = seedTenant("t-a");
        seedUser(tenant, "existente@empresa.com");
        UUID target = seedUser(tenant, "alvo@empresa.com");

        mockMvc.perform(
            put("/admin/users/{userId}", target)
                .with(authentication(auth(tenant, "identity.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("Alvo", "existente@empresa.com"))
        )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("email_already_exists"));
    }

    @Test
    void duplicateEmailInAnotherTenantReturns409() throws Exception {
        UUID tenantA = seedTenant("t-a");
        UUID tenantB = seedTenant("t-b");
        seedUser(tenantA, "compartilhado@empresa.com");
        UUID target = seedUser(tenantB, "alvo@empresa.com");

        mockMvc.perform(
            put("/admin/tenants/{tenantId}/users/{userId}", tenantB, target)
                .with(authentication(auth(tenantB, "platform.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("Alvo", "compartilhado@empresa.com"))
        ).andExpect(status().isConflict());
    }

    @Test
    void keepingOwnEmailDoesNotTrigger409() throws Exception {
        UUID tenant = seedTenant("t-a");
        UUID userId = seedUser(tenant, "meu@empresa.com");

        mockMvc.perform(
            put("/admin/users/{userId}", userId)
                .with(authentication(auth(tenant, "identity.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("Nome Novo", "meu@empresa.com"))
        ).andExpect(status().isOk());
    }

    @Test
    void identityAdminEditingAnotherTenantReturns404() throws Exception {
        UUID tenantA = seedTenant("t-a");
        UUID tenantB = seedTenant("t-b");
        UUID userInB = seedUser(tenantB, "b@empresa.com");

        mockMvc.perform(
            put("/admin/users/{userId}", userInB)
                .with(authentication(auth(tenantA, "identity.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("X", "x@empresa.com"))
        )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("user_not_found"));
    }

    @Test
    void idempotentPutDoesNotChangeUpdatedAt() throws Exception {
        UUID tenant = seedTenant("t-a");
        Instant fixed = Instant.parse("2020-01-01T00:00:00Z");
        UUID userId = seedUser(tenant, "joao@empresa.com", "João Silva", fixed);

        mockMvc.perform(
            put("/admin/users/{userId}", userId)
                .with(authentication(auth(tenant, "identity.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("João Silva", "joao@empresa.com"))
        ).andExpect(status().isOk());

        assertEquals(fixed, jpaUserRepository.findById(userId).get().getUpdatedAt());
    }

    @Test
    void invalidEmailReturns400ValidationError() throws Exception {
        UUID tenant = seedTenant("t-a");
        UUID userId = seedUser(tenant, "joao@empresa.com");

        mockMvc.perform(
            put("/admin/users/{userId}", userId)
                .with(authentication(auth(tenant, "identity.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("João", "isso-nao-e-email"))
        )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("validation_error"));
    }
}
