package br.com.deltaglobalbank.identity.features.tenantLifecycleIntegration

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import br.com.deltaglobalbank.identity.TestcontainersConfiguration
import br.com.deltaglobalbank.identity.domain.shared.PasswordHasher
import br.com.deltaglobalbank.identity.domain.user.Password
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.SigningKeyEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRepository
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.KeyGenerator
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.KeyManager
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaSigningKeyRepository
import java.time.Instant

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
@Transactional
class TenantLifecycleIntegrationTest {

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

    private fun seedUser(id: UUID, tenantId: UUID) {
        jpaUserRepository.save(
            UserEntity(
                id = id,
                tenantId = tenantId,
                fullName = "New User",
                email = "a@a.com",
                passwordHash = passwordHasher.hash(Password("Secret123!")).value,
                status = "active"
            )
        )
    }

    @Test
    fun `suspended tenant blocks user login`() {
        val tenantId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        seedTenant(tenantId, status = "suspended")
        seedUser(userId, tenantId)

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"a@a.com","password":"Secret123!"}""")
        ).andExpect(status().isUnauthorized)
    }

    private fun authWith(userId: UUID, vararg roles: String) = JwtAuthenticationToken(
        AuthenticatedPrincipal(
            subject = userId,
            tenantId = UUID.randomUUID(),
            principalType = "user",
            roles = roles.toList(),
            modules = emptyList(),
            mustChangePassword = false,
            jti = UUID.randomUUID()
        )
    )

    @Test
    fun `reactivated tenant restores user login`(){
        val tenantId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        seedSigningKey()
        seedTenant(tenantId, status = "suspended")
        seedUser(userId, tenantId)

        mockMvc.perform(
            post("/admin/tenants/{tenantId}/activate", tenantId)
                .with(authentication(authWith(UUID.randomUUID(), "platform.admin")))
        ).andExpect(status().isNoContent)

        mockMvc.perform (
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content("""{"email":"a@a.com","password":"Secret123!"}""")
        ).andExpect(status().isOk)
    }

    @Test
    fun `identity admin cannot suspend tenant`() {
        mockMvc.perform(
            post("/admin/tenants/{tenantId}/suspend", UUID.randomUUID())
                .with(authentication(authWith(UUID.randomUUID(), "identity.admin")))
        ).andExpect(status().isForbidden)
    }
}