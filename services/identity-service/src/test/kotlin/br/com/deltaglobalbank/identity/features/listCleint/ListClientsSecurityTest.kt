package br.com.deltaglobalbank.identity.features.listCleint

import br.com.deltaglobalbank.identity.TestcontainersConfiguration
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiClientEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiKeyEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiClientRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiKeyRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantRepository
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
@Transactional
class ListClientsSecurityTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var jpaTenantRepository: JpaTenantRepository

    @Autowired
    lateinit var jpaApiClientRepository: JpaApiClientRepository


    @Autowired
    lateinit var jpaApiKeyRepository : JpaApiKeyRepository

    private fun auth(tenantId: UUID, vararg roles: String) = JwtAuthenticationToken(
        AuthenticatedPrincipal(
            subject = UUID.randomUUID(),
            tenantId = tenantId,
            principalType = "user",
            roles = roles.toList(),
            modules = emptyList(),
            mustChangePassword = false,
            jti = UUID.randomUUID()
        )
    )

    private fun seedTenant(slug: String): UUID {
        val id = UUID.randomUUID()
        jpaTenantRepository.save(TenantEntity(id = id, name = slug, slug = slug, status = "active"))
        return id
    }

    private fun seedClient(tenantId: UUID, name: String) {
        jpaApiClientRepository.save(
            ApiClientEntity(
                id = UUID.randomUUID(), tenantId = tenantId,
                name = name, description = null, status = "active"
            )
        )
    }

    private fun seedKey(apiClientId: UUID, name: String, revoked: Boolean) {
        val unique = UUID.randomUUID().toString()
        jpaApiKeyRepository.save(
            ApiKeyEntity(
                id = UUID.randomUUID(),
                apiClientId = apiClientId,
                name = name,
                keyHash = "hash-$unique",
                keyPrefix = "dgb_${unique.take(8)}",
                fingerprint = "fp-$unique",
                expiresAt = null,
                lastUsedAt = null,
                revokedAt = if (revoked) Instant.now() else null,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        )
    }

    @Test
    fun `should return 403 when identity-admin calls cross-tenant endpoint`() {
        val tenantId = UUID.randomUUID()

        mockMvc.perform(
            get("/admin/tenants/$tenantId/api-clients")
                .with(authentication(auth(UUID.randomUUID(), "identity.admin")))
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `identity-admin should list only clients from its own tenant`() {
        val tenantA = seedTenant("tenant-a")
        val tenantB = seedTenant("tenant-b")
        seedClient(tenantA, "client-a1")
        seedClient(tenantA, "client-a2")
        seedClient(tenantB, "client-b1")

        mockMvc.perform(
            get("/admin/api-clients")
                .with(authentication(auth(tenantA, "identity.admin")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(2))
    }

    @Test
    fun `platform-admin should list clients of any tenant`() {
        val tenantB = seedTenant("tenant-b-platform")
        seedClient(tenantB, "client-b1")
        seedClient(tenantB, "client-b2")

        mockMvc.perform(
            get("/admin/tenants/$tenantB/api-clients")
                .with(authentication(auth(UUID.randomUUID(), "platform.admin")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(2))
    }

    @Test
    fun `should return 401 when request has no authentication`() {
        mockMvc.perform(
            get("/admin/api-clients")
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `activeKeysCount should count only active keys`() {
        val tenantId = seedTenant("tenant-keys")

        val clientId = UUID.randomUUID()
        jpaApiClientRepository.save(
            ApiClientEntity(
                id = clientId, tenantId = tenantId,
                name = "client-keys", description = null, status = "active"
            )
        )

        seedKey(clientId, "k-active", revoked = false)
        seedKey(clientId, "k-revoked", revoked = true)

        mockMvc.perform(
            get("/admin/api-clients")
                .with(authentication(auth(tenantId, "identity.admin")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items[0].activeKeysCount").value(1))
    }

    @Test
    fun `should return 401 when token is invalid`() {
        mockMvc.perform(
            get("/admin/api-clients")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.invalido")
        ).andExpect(status().isUnauthorized)
    }
}