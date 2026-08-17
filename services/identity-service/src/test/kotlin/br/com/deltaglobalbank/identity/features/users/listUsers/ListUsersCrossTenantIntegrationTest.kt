package br.com.deltaglobalbank.identity.features.users.listUsers

import br.com.deltaglobalbank.identity.TestcontainersConfiguration
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRepository
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration::class)
@Transactional
class ListUsersCrossTenantIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var jpaTenantRepository: JpaTenantRepository

    @Autowired
    lateinit var jpaUserRepository: JpaUserRepository

    private fun auth(vararg roles: String) = JwtAuthenticationToken(
        AuthenticatedPrincipal(
            subject = UUID.randomUUID(),
            tenantId = UUID.randomUUID(),
            principalType = "user",
            roles = roles.toList(),
            modules = emptyList(),
            mustChangePassword = false,
            jti = UUID.randomUUID()
        )
    )

    private fun seedTenant(name: String, slug: String): UUID {
        val id = UUID.randomUUID()
        jpaTenantRepository.save(
            TenantEntity(
                id = id,
                name = name,
                slug = slug,
                status = "active",
                createdAt = Instant.now()
            )
        )
        return id
    }

    private fun seedUser(tenantId: UUID, email: String): UUID {
        val id = UUID.randomUUID()
        jpaUserRepository.save(
            UserEntity(
                id = id,
                tenantId = tenantId,
                fullName = "New User",
                email = email,
                passwordHash = "hash",
                status = "active",
                mustChangePassword = false,
                failedAttempts = 0,
                createdAt = Instant.now()
            )
        )
        return id
    }

    @Test
    fun `platform admin lists users of specific tenant`() {
        val tenantA = seedTenant("Tenant A", "tenant-a")
        val tenantB = seedTenant("Tenant B", "tenant-b")
        seedUser(tenantA, "a@example.com")
        seedUser(tenantB, "b@example.com")

        mockMvc.perform(
            get("/admin/tenants/{tenantId}/users", tenantA)
                .with(authentication(auth("platform.admin")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].email").value("a@example.com"))
            .andExpect(jsonPath("$.items[0].tenantId").value(tenantA.toString()))
    }

    @Test
    fun `identity admin cannot list users cross tenant`() {
        val tenantId = seedTenant("Tenant A", "tenant-a")

        mockMvc.perform(
            get("/admin/tenants/{tenantId}/users", tenantId)
                .with(authentication(auth("identity.admin")))
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `unknown tenant returns 404`() {
        mockMvc.perform(
            get("/admin/tenants/{tenantId}/users", UUID.randomUUID())
                .with(authentication(auth("platform.admin")))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("tenant_not_found"))
    }

    @Test
    fun `pagination works for cross tenant list`() {
        val tenantId = seedTenant("Tenant A", "tenant-a")
        seedUser(tenantId, "u1@example.com")
        seedUser(tenantId, "u2@example.com")

        mockMvc.perform(
            get("/admin/tenants/{tenantId}/users?page=0&size=1", tenantId)
                .with(authentication(auth("platform.admin")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.items.length()").value(1))
    }
}
