package br.com.deltaglobalbank.identity.features.users.userLifecycleIntegrationTest

import br.com.deltaglobalbank.identity.TestcontainersConfiguration
import br.com.deltaglobalbank.identity.domain.shared.PasswordHasher
import br.com.deltaglobalbank.identity.domain.user.Password
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.SigningKeyEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaSigningKeyRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRepository
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.KeyGenerator
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.KeyManager
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.time.Instant
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
@Transactional
class UserLifecycleIntegrationTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var jpaTenantRepository: JpaTenantRepository

    @Autowired
    lateinit var jpaUserRepository: JpaUserRepository

    @Autowired
    lateinit var passwordHasher: PasswordHasher

    @Autowired
    lateinit var keyGenerator: KeyGenerator

    @Autowired
    lateinit var jpaSigningKeyRepository: JpaSigningKeyRepository

    @Autowired
    lateinit var keyManager: KeyManager

    private fun seedTenant(id: UUID, status: String) {
        jpaTenantRepository.save(
            TenantEntity(id = id, name = "acme", slug = "acme", status = status)
        )
    }

    private fun seedSigningKey() {
        val keyPair = keyGenerator.generateRsaKeyPair()
        jpaSigningKeyRepository.save(
            SigningKeyEntity(
                id = UUID.randomUUID(),
                kid = "key-${Instant.now().epochSecond}",
                algorithm = "RS256",
                publicKey = keyGenerator.encodePublicKey(keyPair.public),
                privateKey = keyGenerator.encodePrivateKey(keyPair.private),
                status = "active",
                activatedAt = Instant.now()
            )
        )
        keyManager.refresh()
    }

    private fun seedUser(id: UUID, tenantId: UUID, status: String) {
        jpaUserRepository.save(
            UserEntity(
                id = id,
                tenantId = tenantId,
                fullName = "New User",
                email = "a@a.com",
                passwordHash = passwordHasher.hash(Password("Secret123!")).value,
                status = status
            )
        )
    }

    private fun authWith(userId: UUID, tenantId: UUID,vararg roles: String) = JwtAuthenticationToken(
        AuthenticatedPrincipal(
            subject = userId,
            tenantId = tenantId,
            principalType = "user",
            roles = roles.toList(),
            modules = emptyList(),
            mustChangePassword = false,
            jti = UUID.randomUUID()
        )
    )

    @Test
    fun `suspended user cannot login`() {
        val tenantId = UUID.randomUUID(); val userId = UUID.randomUUID()
        seedTenant(tenantId, "active")
        seedUser(userId, tenantId, "suspended")
        mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON).content("""{"email":"a@a.com","password":"Secret123!"}"""))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `reactivated user can login`() {
        val tenantId = UUID.randomUUID(); val userId = UUID.randomUUID()
        seedSigningKey()
        seedTenant(tenantId, "active")
        seedUser(userId, tenantId, "suspended")
        mockMvc.perform(post("/admin/users/{id}/activate", userId)
            .with(authentication(authWith(userId, tenantId, "identity.admin"))))
            .andExpect(status().isNoContent)
        mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON).content("""{"email":"a@a.com","password":"Secret123!"}"""))
            .andExpect(status().isOk)
    }

    @Test
    fun `soft deleted user disappears from listing`() {
        val tenantId = UUID.randomUUID(); val userId = UUID.randomUUID()
        seedTenant(tenantId, "active")
        seedUser(userId, tenantId, "active")
        mockMvc.perform(delete("/admin/users/{id}", userId)
            .with(authentication(authWith(userId, tenantId, "identity.admin"))))
            .andExpect(status().isNoContent)
        mockMvc.perform(get("/admin/users")
            .with(authentication(authWith(userId, tenantId, "identity.admin"))))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalElements").value(0))
    }
}