package br.com.deltaglobalbank.identity.features.auth.refresh

import br.com.deltaglobalbank.identity.domain.tenant.Tenant
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.domain.token.InvalidRefreshTokenException
import br.com.deltaglobalbank.identity.domain.token.RefreshToken
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRepository
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenReuseDetectedException
import br.com.deltaglobalbank.identity.domain.user.User
import br.com.deltaglobalbank.identity.domain.user.UserRepository
import br.com.deltaglobalbank.identity.domain.user.UserStatus
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.RoleEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantModuleEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserRoleEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaIssuedTokenAuditRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaModuleRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaRoleRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantModuleRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.IssuedJwt
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.JwtIssuer
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.JwtIssuerProperties
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.UserClaims
import br.com.deltaglobalbank.identity.infrastructure.security.token.GeneratedRefreshToken
import br.com.deltaglobalbank.identity.infrastructure.security.token.RefreshTokenGenerator
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.Instant
import java.util.UUID

@ExtendWith(MockKExtension::class)
class RefreshTokenTests {

    @MockK
    lateinit var refreshTokenRepository: RefreshTokenRepository
    @MockK
    lateinit var userRepository: UserRepository
    @MockK
    lateinit var tenantRepository: TenantRepository
    @MockK
    lateinit var userRoleRepository: JpaUserRoleRepository
    @MockK
    lateinit var roleRepository: JpaRoleRepository
    @MockK
    lateinit var tenantModuleRepository: JpaTenantModuleRepository
    @MockK
    lateinit var moduleRepository: JpaModuleRepository
    @MockK
    lateinit var issuedTokenAuditRepository: JpaIssuedTokenAuditRepository
    @MockK
    lateinit var refreshTokenGenerator: RefreshTokenGenerator
    @MockK
    lateinit var jwtIssuer: JwtIssuer
    @MockK
    lateinit var jwtProperties: JwtIssuerProperties

    private lateinit var useCase: RefreshTokenUseCase
    private lateinit var context: RefreshTokenContext
    private lateinit var userId: UUID
    private lateinit var tenantId: UUID
    private lateinit var expiresAt: Instant
    private lateinit var enabledModuleId: UUID
    private lateinit var disabledModuleId: UUID

    @BeforeEach
    fun setUp() {
        useCase = RefreshTokenUseCase(
            refreshTokenRepository,
            userRepository,
            tenantRepository,
            userRoleRepository,
            roleRepository,
            tenantModuleRepository,
            moduleRepository,
            issuedTokenAuditRepository,
            refreshTokenGenerator,
            jwtIssuer,
            jwtProperties

        )
        userId = UUID.randomUUID()
        tenantId = UUID.randomUUID()
        expiresAt = Instant.now().plusSeconds(3600)
        context = RefreshTokenContext(ipAddress = "192.168.0.1", userAgent = "test-agent")
        every { refreshTokenGenerator.hash(any()) } returns "algum-hash"
        enabledModuleId = UUID.randomUUID()
        disabledModuleId = UUID.randomUUID()

    }

    @Test
    fun `must throw InvalidRefreshTokenException when token is not found`() {
        every { refreshTokenRepository.findByTokenHash(any()) } returns null

        assertThrows(InvalidRefreshTokenException::class.java) {
            useCase.execute("raw-token", context)
        }

        verify(exactly = 0) { refreshTokenRepository.save(any()) }
        verify(exactly = 0) { jwtIssuer.issueForUser(any()) }
    }

    @Test
    fun `must revoke all active tokens and throw when reuse is detected`() {
        val existing = mockk<RefreshToken>()
        every { existing.isRevoked() } returns true
        every { existing.userId } returns userId
        every { refreshTokenRepository.findByTokenHash(any()) } returns existing

        val active1 = mockk<RefreshToken>(relaxed = true)
        val active2 = mockk<RefreshToken>(relaxed = true)
        every { refreshTokenRepository.findAllActiveByUserId(userId) } returns listOf(active1, active2)
        every { refreshTokenRepository.saveAll(any()) } returns listOf(active1, active2)

        assertThrows(RefreshTokenReuseDetectedException::class.java) {
            useCase.execute("raw-token", context)
        }

        verify { active1.revoke() }
        verify { active2.revoke() }
        verify(exactly = 1) { refreshTokenRepository.saveAll(any()) }

        verify(exactly = 0) { jwtIssuer.issueForUser(any()) }
        verify(exactly = 0) { issuedTokenAuditRepository.save(any()) }
    }

    @Test
    fun `must throw reuse exception without saving when no active tokens exist`() {
        val existing = mockk<RefreshToken>()
        every { existing.isRevoked() } returns true
        every { existing.userId } returns userId
        every { refreshTokenRepository.findByTokenHash(any()) } returns existing
        every { refreshTokenRepository.findAllActiveByUserId(userId) } returns emptyList()

        assertThrows(RefreshTokenReuseDetectedException::class.java) {
            useCase.execute("raw-token", context)
        }

        verify(exactly = 0) { refreshTokenRepository.saveAll(any()) }
        verify(exactly = 0) { jwtIssuer.issueForUser(any()) }
    }

    @Test
    fun `must throw InvalidRefreshTokenException when token is expired`() {
        val existing = mockk<RefreshToken>()
        every { existing.isRevoked() } returns false
        every { existing.isExpired() } returns true
        every { refreshTokenRepository.findByTokenHash(any()) } returns existing

        assertThrows(InvalidRefreshTokenException::class.java) {
            useCase.execute("raw-token", context)
        }

        verify(exactly = 0) { refreshTokenRepository.save(any()) }
        verify(exactly = 0) { jwtIssuer.issueForUser(any()) }
    }

    @Test
    fun `must throw InvalidRefreshTokenException when user is not found`(){
        val existing = mockk<RefreshToken>()
        every { existing.isRevoked() } returns false
        every { existing.isExpired() } returns false
        every { existing.userId } returns userId
        every { refreshTokenRepository.findByTokenHash(any()) } returns existing
        every { userRepository.findById(any()) } returns null

        assertThrows(InvalidRefreshTokenException::class.java) {
            useCase.execute("raw-token", context)
        }

        verify(exactly = 0) { refreshTokenRepository.save(any()) }
        verify(exactly = 0) { jwtIssuer.issueForUser(any()) }
    }

    @Test
    fun `must throw InvalidRefreshTokenException when tenant is not found`() {
        val existing = mockk<RefreshToken>()
        every { existing.isRevoked() } returns false
        every { existing.isExpired() } returns false
        every { existing.userId } returns userId
        every { refreshTokenRepository.findByTokenHash(any()) } returns existing

        val user = mockk<User>()
        every { user.tenantId } returns UUID.randomUUID()
        every { userRepository.findById(any()) } returns user

        every { tenantRepository.findById(any()) } returns null

        assertThrows(InvalidRefreshTokenException::class.java) {
            useCase.execute("raw-token", context)
        }

        verify(exactly = 0) { refreshTokenRepository.save(any()) }
        verify(exactly = 0) { jwtIssuer.issueForUser(any()) }
    }

    @Test
    fun `must throw InvalidRefreshTokenException when tenant is inactive`() {
        val existing = mockk<RefreshToken>()
        every { existing.isRevoked() } returns false
        every { existing.isExpired() } returns false
        every { existing.userId } returns userId
        every { refreshTokenRepository.findByTokenHash(any()) } returns existing

        val user = mockk<User>()
        every { user.tenantId } returns UUID.randomUUID()
        every { userRepository.findById(any()) } returns user

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns false
        every { tenantRepository.findById(any()) } returns tenant
        every { user.id } returns UUID.randomUUID()
        every { userRoleRepository.findAllByUserId(any()) } returns emptyList()

        assertThrows(InvalidRefreshTokenException::class.java) {
            useCase.execute("raw-token", context)
        }

        verify(exactly = 0) { refreshTokenRepository.save(any()) }
        verify(exactly = 0) { jwtIssuer.issueForUser(any()) }
    }

    @ParameterizedTest
    @EnumSource(value = UserStatus::class, names = ["SUSPENDED", "LOCKED"])
    fun `must throw InvalidRefreshTokenException when user is not active`(status: UserStatus) {
        val existing = mockk<RefreshToken>()
        every { existing.isRevoked() } returns false
        every { existing.isExpired() } returns false
        every { existing.userId } returns userId
        every { refreshTokenRepository.findByTokenHash(any()) } returns existing

        val user = mockk<User>()
        every { user.tenantId } returns UUID.randomUUID()
        every { user.isActive() } returns (status == UserStatus.ACTIVE)
        every { userRepository.findById(any()) } returns user

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(any()) } returns tenant
        every { user.id } returns UUID.randomUUID()
        every { userRoleRepository.findAllByUserId(any()) } returns emptyList()

        assertThrows(InvalidRefreshTokenException::class.java) {
            useCase.execute("raw-token", context)
        }

        verify(exactly = 0) { refreshTokenRepository.save(any()) }
        verify(exactly = 0) { jwtIssuer.issueForUser(any()) }
    }

    @Test
    fun `must issue new tokens and revoke current token on successful refresh`() {
        val existing = mockk<RefreshToken>()
        every { existing.isRevoked() } returns false
        every { existing.isExpired() } returns false
        every { existing.userId } returns userId
        every { existing.expiresAt } returns expiresAt
        every { existing.markUsed() } just Runs
        every { existing.revoke() } just Runs
        every { refreshTokenRepository.findByTokenHash(any()) } returns existing

        val user = mockk<User>()
        every { user.id } returns userId
        every { user.tenantId } returns tenantId
        every { user.isActive() } returns true
        every { user.mustChangePassword() } returns false
        every { userRepository.findById(userId) } returns user

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        every { tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true) } returns emptyList()
        every { userRoleRepository.findAllByUserId(userId) } returns emptyList()

        val issuedJwt = IssuedJwt(
            token = "JWT-ACCESS-TOKEN",
            jti = UUID.randomUUID(),
            issuedAt = Instant.now(),
            expiresAt = Instant.now().plusSeconds(900)
        )
        every { jwtIssuer.issueForUser(any()) } returns issuedJwt

        val generated = GeneratedRefreshToken(plainText = "PLAIN-TOKEN", hash = "HASH-INTERNO")
        every { refreshTokenGenerator.generate() } returns generated
        every { jwtProperties.accessTokenTtl } returns java.time.Duration.ofSeconds(900)

        val savedTokens = mutableListOf<RefreshToken>()
        every { refreshTokenRepository.save(capture(savedTokens)) } returns mockk(relaxed = true)
        every { issuedTokenAuditRepository.save(any()) } returns mockk(relaxed = true)

        val response = useCase.execute("raw-token", context)

        assertAll(
            { assertEquals("JWT-ACCESS-TOKEN", response.accessToken) },
            { assertEquals("PLAIN-TOKEN", response.refreshToken) },
            { assertEquals("Bearer", response.tokenType) },
            { assertEquals(900, response.expiresIn) },
            { assertEquals(false, response.mustChangePassword) }
        )

        val novoToken = savedTokens[1]
        assertAll(
            { assertEquals("HASH-INTERNO", novoToken.tokenHash) },
            { assertEquals("192.168.0.1", novoToken.ipAddress) },
            { assertEquals("test-agent", novoToken.userAgent) }
        )

        verify { existing.revoke() }
        verify(exactly = 1) { jwtIssuer.issueForUser(any()) }
        verify(exactly = 1) { issuedTokenAuditRepository.save(any()) }
        verify(exactly = 2) { refreshTokenRepository.save(any()) }
    }

    @Test
    fun `must include only roles from enabled modules in jwt claims on refresh`() {
        val existing = mockk<RefreshToken>(relaxed = true)
        every { existing.isRevoked() } returns false
        every { existing.isExpired() } returns false
        every { existing.userId } returns userId
        every { existing.expiresAt } returns expiresAt
        every { refreshTokenRepository.findByTokenHash(any()) } returns existing

        val user = mockk<User>()
        every { user.id } returns userId
        every { user.tenantId } returns tenantId
        every { user.isActive() } returns true
        every { user.mustChangePassword() } returns false
        every { userRepository.findById(userId) } returns user

        val tenant = mockk<Tenant>()
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant

        val enabledModule = mockk<TenantModuleEntity>()
        every { enabledModule.moduleId } returns enabledModuleId
        every { tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true) } returns listOf(enabledModule)
        every { moduleRepository.findAllById(any()) } returns emptyList()

        val link1 = mockk<UserRoleEntity>(relaxed = true)
        val link2 = mockk<UserRoleEntity>(relaxed = true)
        val link3 = mockk<UserRoleEntity>(relaxed = true)
        every { userRoleRepository.findAllByUserId(userId) } returns listOf(link1, link2, link3)

        val roleEnabled = mockk<RoleEntity>()
        every { roleEnabled.code } returns ("customers.admin")
        every { roleEnabled.moduleId } returns enabledModuleId
        val roleDisabled = mockk<RoleEntity>()
        every { roleDisabled.code } returns ("lending.admin")
        every { roleDisabled.moduleId } returns disabledModuleId
        val roleGlobal = mockk<RoleEntity>()
        every { roleGlobal.code } returns ("platform.admin")
        every { roleGlobal.moduleId } returns null
        every { roleRepository.findAllById(any()) } returns listOf(roleEnabled, roleDisabled, roleGlobal)

        val claimsSlot = slot<UserClaims>()
        val issuedJwt = IssuedJwt("JWT", UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(900))
        every { jwtIssuer.issueForUser(capture(claimsSlot)) } returns issuedJwt

        every { refreshTokenGenerator.generate() } returns GeneratedRefreshToken("PLAIN", "HASH")
        every { refreshTokenRepository.save(any()) } returns mockk(relaxed = true)
        every { issuedTokenAuditRepository.save(any()) } returns mockk(relaxed = true)
        every { jwtProperties.accessTokenTtl } returns java.time.Duration.ofSeconds(900)

        useCase.execute("raw-token", context)

        val roles = claimsSlot.captured.roles
        assertAll(
            { assertTrue(roles.contains("customers.admin")) },
            { assertTrue(roles.contains("platform.admin")) },
            { assertFalse(roles.contains("lending.admin")) }
        )
    }

}