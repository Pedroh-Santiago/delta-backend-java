package br.com.deltaglobalbank.identity.features.auth.logout;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.TestcontainersConfiguration;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.RefreshTokenEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaRefreshTokenRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRepository;
import br.com.deltaglobalbank.identity.infrastructure.security.token.RefreshTokenGenerator;
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken;
import jakarta.servlet.http.Cookie;
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
class LogoutSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaRefreshTokenRepository jpaRefreshTokenRepository;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Autowired
    private JpaTenantRepository jpaTenantRepository;

    @Autowired
    private RefreshTokenGenerator refreshTokenGenerator;

    private JwtAuthenticationToken auth(UUID userId) {
        return new JwtAuthenticationToken(new AuthenticatedPrincipal(
            userId, UUID.randomUUID(), "user", List.of(), List.of(), false, UUID.randomUUID()));
    }

    private UUID seedRefreshToken(UUID userId, String rawToken) {
        UUID id = UUID.randomUUID();
        jpaRefreshTokenRepository.save(new RefreshTokenEntity(
            id, userId, refreshTokenGenerator.hash(rawToken), Instant.now().plusSeconds(3600),
            null, Instant.now(), null, null, null));
        return id;
    }

    private void seedTenant(UUID id) {
        jpaTenantRepository.save(new TenantEntity(id, "acme", "acme", "active", Instant.now(), Instant.now(), null));
    }

    private void seedUser(UUID id, UUID tenantId) {
        jpaUserRepository.save(new UserEntity(
            id, tenantId, "New User", "a@a.com", "x", "active",
            false, null, null, 0, null, Instant.now(), Instant.now(), null));
    }

    @Test
    void shouldRevokeRefreshTokenOnSingleSessionLogout() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        String rawToken = "raw-token-123";
        seedTenant(tenantId);
        seedUser(userId, tenantId);
        UUID tokenId = seedRefreshToken(userId, rawToken);

        mockMvc.perform(
            post("/auth/logout")
                .with(authentication(auth(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + rawToken + "\"}")
        ).andExpect(status().isNoContent());

        RefreshTokenEntity saved = jpaRefreshTokenRepository.findById(tokenId).orElseThrow();
        assertNotNull(saved.getRevokedAt());
    }

    @Test
    void shouldRevokeAllActiveTokensWhenAllSessionsIsTrue() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        seedTenant(tenantId);
        seedUser(userId, tenantId);
        UUID id1 = seedRefreshToken(userId, "raw-1");
        UUID id2 = seedRefreshToken(userId, "raw-2");

        mockMvc.perform(
            post("/auth/logout")
                .param("allSessions", "true")
                .with(authentication(auth(userId)))
        ).andExpect(status().isNoContent());

        assertNotNull(jpaRefreshTokenRepository.findById(id1).orElseThrow().getRevokedAt());
        assertNotNull(jpaRefreshTokenRepository.findById(id2).orElseThrow().getRevokedAt());
    }

    @Test
    void shouldNotRevokeARefreshTokenThatBelongsToAnotherUser() throws Exception {
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        seedTenant(tenantId);
        seedUser(userB, tenantId);
        String rawTokenB = "raw-token-b";
        UUID tokenIdB = seedRefreshToken(userB, rawTokenB);

        mockMvc.perform(
            post("/auth/logout")
                .with(authentication(auth(userA)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + rawTokenB + "\"}")
        ).andExpect(status().isNoContent());

        RefreshTokenEntity saved = jpaRefreshTokenRepository.findById(tokenIdB).orElseThrow();
        assertNull(saved.getRevokedAt());
    }

    @Test
    void shouldReturn400WhenSingleLogoutHasNoRefreshToken() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        seedTenant(tenantId);
        seedUser(userId, tenantId);

        mockMvc.perform(
            post("/auth/logout")
                .with(authentication(auth(userId)))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn204WhenRefreshTokenDoesNotExist() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(
            post("/auth/logout")
                .with(authentication(auth(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"no-refresh\"}")
        ).andExpect(status().isNoContent());
    }

    @Test
    void shouldRevokeTokenFromCookieAndClearCookieOnLogout() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        String rawToken = "raw-token-cookie";
        seedTenant(tenantId);
        seedUser(userId, tenantId);
        UUID tokenId = seedRefreshToken(userId, rawToken);

        mockMvc.perform(
            post("/auth/logout")
                .with(authentication(auth(userId)))
                .cookie(new Cookie("refreshToken", rawToken))
        ).andExpect(status().isNoContent())
            .andExpect(cookie().maxAge("refreshToken", 0));

        RefreshTokenEntity saved = jpaRefreshTokenRepository.findById(tokenId).orElseThrow();
        assertNotNull(saved.getRevokedAt());
    }
}
