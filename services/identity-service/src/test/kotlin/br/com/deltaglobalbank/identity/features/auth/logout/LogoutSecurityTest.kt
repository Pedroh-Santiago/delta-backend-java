package br.com.deltaglobalbank.identity.features.auth.logout

import br.com.deltaglobalbank.identity.TestcontainersConfiguration
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.RefreshTokenEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaRefreshTokenRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantRepository
import br.com.deltaglobalbank.identity.infrastructure.security.token.RefreshTokenGenerator
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID
import org.mockito.Mockito.verify
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
@Transactional
class LogoutSecurityTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var jpaRefreshTokenRepository: JpaRefreshTokenRepository

    @Autowired
    lateinit var jpaUserRepository: JpaUserRepository

    @Autowired
    lateinit var jpaTenantRepository : JpaTenantRepository

    @Autowired
    lateinit var refreshTokenGenerator: RefreshTokenGenerator

    private fun auth(userId: UUID) = JwtAuthenticationToken(
        AuthenticatedPrincipal(
            subject = userId,
            tenantId = UUID.randomUUID(),
            principalType = "user",
            roles = emptyList(),
            modules = emptyList(),
            mustChangePassword = false,
            jti = UUID.randomUUID()
        )
    )

    private fun seedRefreshToken(userId: UUID, rawToken: String): UUID {
        val id = UUID.randomUUID()
        jpaRefreshTokenRepository.save(
            RefreshTokenEntity(
                id = id,
                userId = userId,
                tokenHash = refreshTokenGenerator.hash(rawToken),
                expiresAt = Instant.now().plusSeconds(3600)
            )
        )
        return id
    }

    private fun seedTenant(id: UUID) {
        jpaTenantRepository.save(
            TenantEntity(
                id = id,
                name = "acme",
                slug = "acme",
                status = "active"
            )
        )
    }
    private fun seedUser(id: UUID, tenantId:UUID) {
        jpaUserRepository.save(
            UserEntity(
                id = id,
                tenantId = tenantId,
                fullName = "New User",
                email = "a@a.com",
                passwordHash = "x",
                status = "active"
            ))
    }

    @Test
    fun `should revoke refresh token on single session logout`() {
        val userId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val rawToken = "raw-token-123"
        seedTenant(tenantId)
        seedUser(userId, tenantId )
        val tokenId = seedRefreshToken(userId, rawToken)

        mockMvc.perform(
            post("/auth/logout")
                .with(authentication(auth(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken":"$rawToken"}""")
        ).andExpect(status().isNoContent)

        val saved = jpaRefreshTokenRepository.findById(tokenId).orElseThrow()
        assertNotNull(saved.revokedAt)
    }

    @Test
    fun `should revoke all active tokens when allSessions is true`() {
        val userId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        seedTenant(tenantId)
        seedUser(userId, tenantId)
        val id1 = seedRefreshToken(userId, "raw-1")
        val id2 = seedRefreshToken(userId, "raw-2")

        mockMvc.perform(
            post("/auth/logout")
                .param("allSessions", "true")
                .with(authentication(auth(userId)))
        ).andExpect(status().isNoContent)

        assertNotNull(jpaRefreshTokenRepository.findById(id1).orElseThrow().revokedAt)
        assertNotNull(jpaRefreshTokenRepository.findById(id2).orElseThrow().revokedAt)
    }

    @Test
    fun `should not revoke a refresh token that belongs to another user`() {
        val userA = UUID.randomUUID()
        val userB = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        seedTenant(tenantId)
        seedUser(userB, tenantId)
        val rawTokenB = "raw-token-b"
        val tokenIdB = seedRefreshToken(userB, rawTokenB)

        mockMvc.perform(
            post("/auth/logout")
                .with(authentication(auth(userA)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken":"$rawTokenB"}""")
        ).andExpect(status().isNoContent)

        val saved = jpaRefreshTokenRepository.findById(tokenIdB).orElseThrow()
        assertNull(saved.revokedAt)
    }

    @Test
    fun `should return 400 when single logout has no refresh token`() {
        val userId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        seedTenant(tenantId)
        seedUser(userId, tenantId)

        mockMvc.perform(
            post("/auth/logout")
                .with(authentication(auth(userId)))
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 204 when refresh token does not exist`(){
        val userId = UUID.randomUUID()

        mockMvc.perform(
            post("/auth/logout")
                .with(authentication(auth(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken":"no-refresh"}""")
        ).andExpect(status().isNoContent)
    }

    @Test
    fun `should revoke token from cookie and clear cookie on logout`(){
        val userId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val rawToken = "raw-token-cookie"
        seedTenant(tenantId)
        seedUser(userId, tenantId)
        val tokenId = seedRefreshToken(userId, rawToken)

        mockMvc.perform(
            post("/auth/logout")
                .with(authentication(auth(userId)))
                .cookie(Cookie("refreshToken", rawToken))
        ).andExpect(status().isNoContent)
            .andExpect(cookie().maxAge("refreshToken", 0))

        val saved = jpaRefreshTokenRepository.findById(tokenId).orElseThrow()
        assertNotNull(saved.revokedAt)
    }
}