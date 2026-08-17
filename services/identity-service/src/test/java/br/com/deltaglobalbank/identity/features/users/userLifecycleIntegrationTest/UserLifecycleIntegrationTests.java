package br.com.deltaglobalbank.identity.features.users.userLifecycleIntegrationTest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.KeyPair;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.TestcontainersConfiguration;
import br.com.deltaglobalbank.identity.domain.shared.PasswordHasher;
import br.com.deltaglobalbank.identity.domain.user.Password;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.SigningKeyEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaSigningKeyRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRepository;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.KeyGenerator;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.KeyManager;
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class UserLifecycleIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaTenantRepository jpaTenantRepository;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @Autowired
    private KeyGenerator keyGenerator;

    @Autowired
    private JpaSigningKeyRepository jpaSigningKeyRepository;

    @Autowired
    private KeyManager keyManager;

    private void seedTenant(UUID id, String status) {
        jpaTenantRepository.save(new TenantEntity(id, "acme", "acme", status, Instant.now(), Instant.now(), null));
    }

    private void seedSigningKey() {
        KeyPair keyPair = keyGenerator.generateRsaKeyPair();
        jpaSigningKeyRepository.save(new SigningKeyEntity(
            UUID.randomUUID(), "key-" + Instant.now().getEpochSecond(), "RS256",
            keyGenerator.encodePublicKey(keyPair.getPublic()), keyGenerator.encodePrivateKey(keyPair.getPrivate()),
            "active", Instant.now(), Instant.now(), null));
        keyManager.refresh();
    }

    private void seedUser(UUID id, UUID tenantId, String status) {
        jpaUserRepository.save(new UserEntity(
            id, tenantId, "New User", "a@a.com", passwordHasher.hash(new Password("Secret123!")).value(),
            status, false, null, null, 0, null, Instant.now(), Instant.now(), null));
    }

    private JwtAuthenticationToken authWith(UUID userId, UUID tenantId, String... roles) {
        return new JwtAuthenticationToken(new AuthenticatedPrincipal(
            userId, tenantId, "user", List.of(roles), List.of(), false, UUID.randomUUID()));
    }

    @Test
    void suspendedUserCannotLogin() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        seedTenant(tenantId, "active");
        seedUser(userId, tenantId, "suspended");
        mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"a@a.com\",\"password\":\"Secret123!\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void reactivatedUserCanLogin() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        seedSigningKey();
        seedTenant(tenantId, "active");
        seedUser(userId, tenantId, "suspended");
        mockMvc.perform(post("/admin/users/{id}/activate", userId)
            .with(authentication(authWith(userId, tenantId, "identity.admin"))))
            .andExpect(status().isNoContent());
        mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"a@a.com\",\"password\":\"Secret123!\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void softDeletedUserDisappearsFromListing() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        seedTenant(tenantId, "active");
        seedUser(userId, tenantId, "active");
        mockMvc.perform(delete("/admin/users/{id}", userId)
            .with(authentication(authWith(userId, tenantId, "identity.admin"))))
            .andExpect(status().isNoContent());
        mockMvc.perform(get("/admin/users")
            .with(authentication(authWith(userId, tenantId, "identity.admin"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(0));
    }
}
