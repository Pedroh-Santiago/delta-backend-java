package br.com.deltaglobalbank.identity.features.auth.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import br.com.deltaglobalbank.identity.TestcontainersConfiguration;
import br.com.deltaglobalbank.identity.domain.user.Password;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRepository;
import br.com.deltaglobalbank.identity.infrastructure.security.password.SpringPasswordHasher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "identity.security.lockout.enabled=false")
class LoginLockoutDisabledIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Autowired
    private JpaTenantRepository jpaTenantRepository;

    @Autowired
    private SpringPasswordHasher passwordHasher;

    private final UUID tenantId = UUID.randomUUID();
    private UUID userId;
    private final String email = "lockout-disabled@delta.com";
    private final String correctPassword = "CorrectPass1";

    @BeforeEach
    void seed() {
        jpaTenantRepository.save(new TenantEntity(
            tenantId, "teste", "teste-teste", "active", Instant.now(), Instant.now(), null));
        userId = UUID.randomUUID();
        jpaUserRepository.save(new UserEntity(
            userId, tenantId, "Lock User", email,
            passwordHasher.hash(new Password(correctPassword)).value(),
            "active", false, null, null, 0, null,
            Instant.now(), Instant.now(), null
        ));
    }

    @AfterEach
    void cleanup() {
        jpaUserRepository.deleteById(userId);
        jpaTenantRepository.deleteById(tenantId);
    }

    private ResultActions login(String password) throws Exception {
        return mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }

    @Test
    void disabledLockoutNeverReturns423ButStillCounts() throws Exception {
        for (int i = 0; i < 6; i++) {
            login("wrong-pass").andExpect(status().isUnauthorized());
        }

        UserEntity entity = jpaUserRepository.findById(userId).orElseThrow();
        assertEquals(6, entity.getFailedAttempts());
        assertEquals("active", entity.getStatus());
    }
}
