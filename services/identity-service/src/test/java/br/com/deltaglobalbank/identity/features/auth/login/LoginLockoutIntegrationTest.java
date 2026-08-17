package br.com.deltaglobalbank.identity.features.auth.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.KeyPair;
import java.time.Instant;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

// SEM @Transactional de propósito: o FailedLoginRecorder é REQUIRES_NEW e commita;
// um teste transacional não reverteria esses commits e ainda mascararia a persistência.
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class LoginLockoutIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Autowired
    private JpaTenantRepository jpaTenantRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @Autowired
    private KeyGenerator keyGenerator;

    @Autowired
    private JpaSigningKeyRepository jpaSigningKeyRepository;

    @Autowired
    private KeyManager keyManager;

    private final UUID tenantId = UUID.randomUUID();
    private UUID userId;
    private final String email = "lockout@delta.com";
    private final String correctPassword = "CorrectPass1";

    @BeforeEach
    void seed() {
        jpaTenantRepository.save(new TenantEntity(
            tenantId, "acme", "acme-" + UUID.randomUUID(), "active", Instant.now(), Instant.now(), null));
        userId = UUID.randomUUID();
        saveUser("active", 0, null);
        seedSigningKey();
    }

    @AfterEach
    void cleanup() {
        jpaUserRepository.deleteById(userId);
        jpaTenantRepository.deleteById(tenantId);
        jpaSigningKeyRepository.deleteAll();
        keyManager.refresh();
    }

    private void saveUser(String status, int failedAttempts, Instant lockedUntil) {
        jpaUserRepository.save(new UserEntity(
            userId, tenantId, "Lock User", email,
            passwordHasher.hash(new Password(correctPassword)).value(),
            status, false, null, null, failedAttempts, lockedUntil,
            Instant.now(), Instant.now(), null
        ));
    }

    private void seedSigningKey() {
        KeyPair keyPair = keyGenerator.generateRsaKeyPair();
        jpaSigningKeyRepository.save(new SigningKeyEntity(
            UUID.randomUUID(), "key-" + Instant.now().getEpochSecond(), "RS256",
            keyGenerator.encodePublicKey(keyPair.getPublic()), keyGenerator.encodePrivateKey(keyPair.getPrivate()),
            "active", Instant.now(), Instant.now(), null
        ));
        keyManager.refresh();
    }

    private org.springframework.test.web.servlet.ResultActions login(String password) throws Exception {
        return mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }

    @Test
    void fiveWrongThenASixthAttemptReturns423AndFailedAttemptsIsPersisted() throws Exception {
        for (int i = 0; i < 5; i++) {
            login("wrong-pass").andExpect(status().isUnauthorized());
        }

        login(correctPassword)
            .andExpect(status().isLocked())
            .andExpect(jsonPath("$.error").value("user_locked"))
            .andExpect(jsonPath("$.lockedUntil").exists());

        assertEquals(5, jpaUserRepository.findById(userId).orElseThrow().getFailedAttempts());
    }

    @Test
    void loginWorksAfterTheCooldownHasExpiredAndResetsTheCounter() throws Exception {
        saveUser("locked", 5, Instant.now().minusSeconds(1));

        login(correctPassword).andExpect(status().isOk());

        UserEntity entity = jpaUserRepository.findById(userId).orElseThrow();
        assertEquals(0, entity.getFailedAttempts());
        assertEquals("active", entity.getStatus());
    }

    @Test
    void wrongPasswordBeforeTheLimitStays401AndAccountRemainsActive() throws Exception {
        for (int i = 0; i < 4; i++) {
            login("wrong-pass").andExpect(status().isUnauthorized());
        }

        UserEntity entity = jpaUserRepository.findById(userId).orElseThrow();
        assertEquals(4, entity.getFailedAttempts());
        assertEquals("active", entity.getStatus());
    }
}
