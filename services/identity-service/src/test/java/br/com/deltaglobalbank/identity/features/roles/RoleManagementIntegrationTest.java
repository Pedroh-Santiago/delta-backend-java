package br.com.deltaglobalbank.identity.features.roles;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.TestcontainersConfiguration;
import br.com.deltaglobalbank.identity.domain.shared.PasswordHasher;
import br.com.deltaglobalbank.identity.domain.user.Password;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserRoleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaRoleRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository;
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
class RoleManagementIntegrationTest {

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

    private JwtAuthenticationToken auth(String... roles) {
        return new JwtAuthenticationToken(new AuthenticatedPrincipal(
            UUID.randomUUID(), UUID.randomUUID(), "user", List.of(roles), List.of(), false, UUID.randomUUID()));
    }

    @Test
    void identityAdminCannotCreateRole() throws Exception {
        mockMvc.perform(
            post("/roles")
                .with(authentication(auth("identity.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"tentativa.identity.admin\",\"label\":\"Nao deveria\"}")
        ).andExpect(status().isForbidden());
    }

    @Test
    void platformAdminCanCreateRoleAndDeleteItAndTheCodeCanBeReusedAfterwards() throws Exception {
        String createResponse = mockMvc.perform(
            post("/roles")
                .with(authentication(auth("platform.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"temp.role.regression\",\"label\":\"Temp\"}")
        ).andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String roleId = JsonPath.read(createResponse, "$.id");

        mockMvc.perform(
            delete("/roles/{roleId}", roleId)
                .with(authentication(auth("platform.admin")))
        ).andExpect(status().isNoContent());

        mockMvc.perform(
            post("/roles")
                .with(authentication(auth("platform.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"temp.role.regression\",\"label\":\"Temp de novo\"}")
        ).andExpect(status().isCreated());
    }

    @Test
    void deletingPlatformAdminRoleIsBlocked() throws Exception {
        UUID platformAdminId = jpaRoleRepository.findByCode("platform.admin").getId();

        mockMvc.perform(
            delete("/roles/{roleId}", platformAdminId)
                .with(authentication(auth("platform.admin")))
        ).andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("role_protected"));
    }

    @Test
    void deactivatingPlatformAdminRoleIsBlocked() throws Exception {
        UUID platformAdminId = jpaRoleRepository.findByCode("platform.admin").getId();

        mockMvc.perform(
            patch("/admin/roles/{roleId}/status", platformAdminId)
                .with(authentication(auth("platform.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\":false}")
        ).andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("role_protected"));
    }

    @Test
    void deletingARoleAssignedToAUserIsBlocked() throws Exception {
        UUID tenantId = UuidCreator.getTimeOrderedEpoch();
        UUID userId = UuidCreator.getTimeOrderedEpoch();
        jpaTenantRepository.save(new TenantEntity(
            tenantId, "acme", "acme-" + tenantId, "active", Instant.now(), Instant.now(), null));
        jpaUserRepository.save(new UserEntity(
            userId, tenantId, "User", "user-" + userId + "@delta.com",
            passwordHasher.hash(new Password("Secret123!")).value(), "active",
            false, null, null, 0, null, Instant.now(), Instant.now(), null));

        String createResponse = mockMvc.perform(
            post("/roles")
                .with(authentication(auth("platform.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"temp.role.inuse\",\"label\":\"Temp em uso\"}")
        ).andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        UUID roleId = UUID.fromString(JsonPath.read(createResponse, "$.id"));

        jpaUserRoleRepository.save(new UserRoleEntity(
            UuidCreator.getTimeOrderedEpoch(), userId, roleId, Instant.now(), null, null));

        mockMvc.perform(
            delete("/roles/{roleId}", roleId)
                .with(authentication(auth("platform.admin")))
        ).andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("role_in_use"));
    }

    @Test
    void assigningADeactivatedRoleToAUserIsBlocked() throws Exception {
        UUID tenantId = UuidCreator.getTimeOrderedEpoch();
        UUID userId = UuidCreator.getTimeOrderedEpoch();
        jpaTenantRepository.save(new TenantEntity(
            tenantId, "acme", "acme-" + tenantId, "active", Instant.now(), Instant.now(), null));
        jpaUserRepository.save(new UserEntity(
            userId, tenantId, "User", "user-" + userId + "@delta.com",
            passwordHasher.hash(new Password("Secret123!")).value(), "active",
            false, null, null, 0, null, Instant.now(), Instant.now(), null));

        String createResponse = mockMvc.perform(
            post("/roles")
                .with(authentication(auth("platform.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"temp.role.deactivated\",\"label\":\"Temp desativada\"}")
        ).andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String roleId = JsonPath.read(createResponse, "$.id");

        mockMvc.perform(
            patch("/admin/roles/{roleId}/status", roleId)
                .with(authentication(auth("platform.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\":false}")
        ).andExpect(status().isOk());

        mockMvc.perform(
            post("/admin/tenants/{tenantId}/users/{userId}/roles", tenantId, userId)
                .with(authentication(auth("platform.admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rolesCodes\":[\"temp.role.deactivated\"]}")
        ).andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("role_inactive"));
    }
}
