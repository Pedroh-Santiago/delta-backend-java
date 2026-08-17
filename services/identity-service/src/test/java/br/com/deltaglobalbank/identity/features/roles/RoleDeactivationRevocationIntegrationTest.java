package br.com.deltaglobalbank.identity.features.roles;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserRoleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaRoleRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaSigningKeyRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.KeyGenerator;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.KeyManager;
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken;
import com.github.f4b6a3.uuid.UuidCreator;
import com.jayway.jsonpath.JsonPath;
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
class RoleDeactivationRevocationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaRoleRepository jpaRoleRepository;

    @Autowired
    private JpaTenantRepository jpaTenantRepository;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Autowired
    private JpaUserRoleRepository jpaUserRoleRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @Autowired
    private KeyGenerator keyGenerator;

    @Autowired
    private JpaSigningKeyRepository jpaSigningKeyRepository;

    @Autowired
    private KeyManager keyManager;

    private void seedSigningKey() {
        KeyPair keyPair = keyGenerator.generateRsaKeyPair();
        jpaSigningKeyRepository.save(new SigningKeyEntity(
            UUID.randomUUID(), "key-" + Instant.now().getEpochSecond(), "RS256",
            keyGenerator.encodePublicKey(keyPair.getPublic()), keyGenerator.encodePrivateKey(keyPair.getPrivate()),
            "active", Instant.now(), Instant.now(), null));
        keyManager.refresh();
    }

    private JwtAuthenticationToken platformAdminAuth() {
        return new JwtAuthenticationToken(new AuthenticatedPrincipal(
            UUID.randomUUID(), UUID.randomUUID(), "user", List.of("platform.admin"), List.of(), false, UUID.randomUUID()));
    }

    @Test
    void accessTokenIsRejectedAfterTheRoleDeactivationThatGrantedItIsRevoked() throws Exception {
        seedSigningKey();

        UUID tenantId = UuidCreator.getTimeOrderedEpoch();
        UUID userId = UuidCreator.getTimeOrderedEpoch();
        String email = "revoke-" + userId + "@delta.com";

        jpaTenantRepository.save(new TenantEntity(
            tenantId, "acme", "acme-" + tenantId, "active", Instant.now(), Instant.now(), null));
        jpaUserRepository.save(new UserEntity(
            userId, tenantId, "User", email,
            passwordHasher.hash(new Password("Secret123!")).value(), "active",
            false, null, null, 0, null, Instant.now(), Instant.now(), null));

        UUID roleId = jpaRoleRepository.findByCode("customers.viewer").getId();
        jpaUserRoleRepository.save(new UserRoleEntity(
            UuidCreator.getTimeOrderedEpoch(), userId, roleId, Instant.now(), null, null));

        String loginResponse = mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"Secret123!\"}")
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        String accessToken = JsonPath.read(loginResponse, "$.accessToken");

        mockMvc.perform(
            patch("/admin/roles/{roleId}/status", roleId)
                .with(authentication(platformAdminAuth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\":false}")
        ).andExpect(status().isOk());

        mockMvc.perform(
            post("/auth/change-password")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"Secret123!\",\"newPassword\":\"OutraSenha456!\"}")
        ).andExpect(status().isUnauthorized());
    }
}
