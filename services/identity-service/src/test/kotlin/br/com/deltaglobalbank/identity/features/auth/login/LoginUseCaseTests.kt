package br.com.deltaglobalbank.identity.features.auth.login

import br.com.deltaglobalbank.identity.domain.ipAllowlist.Cidr
import br.com.deltaglobalbank.identity.domain.ipAllowlist.IpNotAllowedException
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlist
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlistRepository
import br.com.deltaglobalbank.identity.domain.module.Module
import br.com.deltaglobalbank.identity.domain.module.ModuleCode
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository
import br.com.deltaglobalbank.identity.domain.module.TenantModule
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository
import br.com.deltaglobalbank.identity.domain.role.Role
import br.com.deltaglobalbank.identity.domain.role.RoleCode
import br.com.deltaglobalbank.identity.domain.role.RoleRepository
import br.com.deltaglobalbank.identity.domain.shared.PasswordHasher
import br.com.deltaglobalbank.identity.domain.tenant.Tenant
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.domain.token.IssuedTokenAuditRepository
import br.com.deltaglobalbank.identity.domain.token.RefreshToken
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRepository
import br.com.deltaglobalbank.identity.domain.user.Email
import br.com.deltaglobalbank.identity.domain.user.InvalidCredentialsException
import br.com.deltaglobalbank.identity.domain.user.User
import br.com.deltaglobalbank.identity.domain.user.UserRepository
import br.com.deltaglobalbank.identity.infrastructure.security.ipAllowlist.TenantIpAllowlistCache
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.IssuedJwt
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.JwtIssuer
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.JwtIssuerProperties
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.UserClaims
import br.com.deltaglobalbank.identity.infrastructure.security.token.GeneratedRefreshToken
import br.com.deltaglobalbank.identity.infrastructure.security.token.RefreshTokenGenerator
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
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
import java.time.Instant
import java.util.UUID

@ExtendWith(MockKExtension::class)
class LoginUseCaseTests {

    @MockK
    lateinit var userRepository: UserRepository
    @MockK
    lateinit var tenantRepository: TenantRepository
    @MockK
    lateinit var roleRepository: RoleRepository
    @MockK
    lateinit var tenantModuleRepository: TenantModuleRepository
    @MockK
    lateinit var moduleRepository: ModuleRepository
    @MockK
    lateinit var refreshTokenRepository: RefreshTokenRepository
    @MockK
    lateinit var issuedTokenAuditRepository: IssuedTokenAuditRepository
    @MockK
    lateinit var passwordHasher: PasswordHasher
    @MockK
    lateinit var refreshTokenGenerator: RefreshTokenGenerator
    @MockK
    lateinit var jwtIssuer: JwtIssuer
    @MockK
    lateinit var jwtProperties: JwtIssuerProperties
    @MockK
    lateinit var tenantIpAllowlistRepository: TenantIpAllowlistRepository
    @MockK
    lateinit var tenantIpAllowlistCache: TenantIpAllowlistCache

    private lateinit var useCase: LoginUseCase
    private lateinit var request: LoginRequest
    private lateinit var context: LoginContext
    private lateinit var userId: UUID
    private lateinit var tenantId: UUID
    private lateinit var enabledModuleId: UUID
    private lateinit var disabledModuleId: UUID
    private lateinit var moduleId: UUID

    @BeforeEach
    fun setUp() {
        useCase = LoginUseCase(
            userRepository,
            tenantRepository,
            roleRepository,
            tenantModuleRepository,
            moduleRepository,
            refreshTokenRepository,
            issuedTokenAuditRepository,
            passwordHasher,
            refreshTokenGenerator,
            jwtIssuer,
            jwtProperties,
            tenantIpAllowlistRepository,
            tenantIpAllowlistCache

        )
        request = LoginRequest(email = "valid.email@delta.com", password = "Password123")
        context = LoginContext(ipAddress = "192.168.0.1", userAgent = "test-agent")
        userId = UUID.randomUUID()
        tenantId = UUID.randomUUID()
        enabledModuleId = UUID.randomUUID()
        disabledModuleId = UUID.randomUUID()
        moduleId = UUID.randomUUID()
    }

    @Test
    fun `must throw InvalidCredentialsException when email is invalid`() {
        request = LoginRequest(email = "invalid.email_delta.com", password = "Password123")
        assertThrows(InvalidCredentialsException::class.java) {
            useCase.execute(request, context)
        }

        verify(exactly = 0) { userRepository.save(any()) }
        verify(exactly = 0) { jwtIssuer.issueForUser(any()) }
    }

    @Test
    fun `must throw InvalidCredentialsException when user is not found`() {
        every { userRepository.findByEmail(Email("valid.email@delta.com")) } returns null

        assertThrows(InvalidCredentialsException::class.java) {
            useCase.execute(request, context)
        }

        verify(exactly = 0) { userRepository.save(any()) }
        verify(exactly = 0) { jwtIssuer.issueForUser(any()) }
    }

    @Test
    fun `must throw InvalidCredentialsException when tenant is not found`() {
        val user = mockk<User>()
        every { user.tenantId } returns UUID.randomUUID()
        every { userRepository.findByEmail(Email("valid.email@delta.com")) } returns user

        every { tenantRepository.findById(any()) } returns null

        assertThrows(InvalidCredentialsException::class.java) {
            useCase.execute(request, context)
        }

        verify(exactly = 0) { userRepository.save(any()) }
        verify(exactly = 0) { jwtIssuer.issueForUser(any()) }
    }

    @Test
    fun `must throw InvalidCredentialsException when authentication fails`() {
        val user = mockk<User>()
        every { user.tenantId } returns UUID.randomUUID()
        every { userRepository.findByEmail(Email("valid.email@delta.com")) } returns user

        val tenant = mockk<Tenant>()
        every { tenantRepository.findById(any()) } returns tenant
        every { user.id } returns UUID.randomUUID()
        every { roleRepository.findAllByUserId(any()) } returns emptyList()
        every { tenant.isActive() } returns true

        every { user.authenticate(any(), any())} throws InvalidCredentialsException()

        every { tenantIpAllowlistRepository.findAllByTenantId(any()) } returns emptyList()
        every { tenantIpAllowlistCache.get(any()) } returns null
        every { tenantIpAllowlistCache.set(any(), any()) } just runs

        assertThrows(InvalidCredentialsException::class.java) {
            useCase.execute(request, context)
        }

        verify(exactly = 0) { userRepository.save(any()) }
        verify(exactly = 0) { jwtIssuer.issueForUser(any()) }
    }

    @Test
    fun `must include only roles from enabled modules in jwt claims`() {
        val user = mockk<User>(relaxed = true)
        every { user.id } returns userId
        every { user.tenantId } returns tenantId
        every { user.mustChangePassword() } returns false
        every { user.authenticate(any(), any()) } just runs
        every { userRepository.findByEmail(Email("valid.email@delta.com")) } returns user
        every { userRepository.save(user) } returns user

        val tenant = mockk<Tenant>()
        every { tenantRepository.findById(tenantId) } returns tenant

        val enabledModule = mockk<TenantModule>()
        every { enabledModule.moduleId } returns enabledModuleId
        every { tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true) } returns listOf(enabledModule)
        every { moduleRepository.findAllByIds(any()) } returns emptyList()

        every { tenantIpAllowlistRepository.findAllByTenantId(any()) } returns emptyList()

        every { tenantIpAllowlistCache.get(any()) } returns null
        every { tenantIpAllowlistCache.set(any(), any()) } just runs

        val roleEnabled = mockk<Role>()
        every { roleEnabled.code } returns RoleCode("customers.admin")
        every { roleEnabled.moduleId } returns enabledModuleId
        val roleDisabled = mockk<Role>()
        every { roleDisabled.code } returns RoleCode("lending.admin")
        every { roleDisabled.moduleId } returns disabledModuleId
        val roleGlobal = mockk<Role>()
        every { roleGlobal.code } returns RoleCode("platform.admin")
        every { roleGlobal.moduleId } returns null
        every { roleRepository.findAllByUserId(userId) } returns listOf(roleEnabled, roleDisabled, roleGlobal)

        val claimsSlot = slot<UserClaims>()
        val issuedJwt = IssuedJwt("JWT", UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(900))
        every { jwtIssuer.issueForUser(capture(claimsSlot)) } returns issuedJwt

        every { refreshTokenGenerator.generate() } returns GeneratedRefreshToken("PLAIN", "HASH")
        every { refreshTokenRepository.save(any()) } returns mockk(relaxed = true)
        every { issuedTokenAuditRepository.save(any()) } returns mockk(relaxed = true)
        every { jwtProperties.accessTokenTtl } returns java.time.Duration.ofSeconds(900)
        every { jwtProperties.refreshTokenTtl } returns java.time.Duration.ofDays(7)

        useCase.execute(request, context)

        val roles = claimsSlot.captured.roles
        assertAll(
            { assertTrue(roles.contains("customers.admin")) },
            { assertTrue(roles.contains("platform.admin")) },
            { assertFalse(roles.contains("lending.admin")) }
        )
    }

    @Test
    fun `must propagate mustChangePassword true into jwt claims`() {
        val user = mockk<User>(relaxed = true)
        every { user.id } returns userId
        every { user.tenantId } returns tenantId
        every { user.mustChangePassword() } returns true
        every { user.authenticate(any(), any()) } just runs
        every { userRepository.findByEmail(Email("valid.email@delta.com")) } returns user
        every { userRepository.save(user) } returns user

        val tenant = mockk<Tenant>()
        every { tenantRepository.findById(tenantId) } returns tenant

        every { tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true) } returns emptyList()
        every { roleRepository.findAllByUserId(userId) } returns emptyList()

        val claimsSlot = slot<UserClaims>()
        val issuedJwt = IssuedJwt("JWT", UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(900))
        every { jwtIssuer.issueForUser(capture(claimsSlot)) } returns issuedJwt

        every { refreshTokenGenerator.generate() } returns GeneratedRefreshToken("PLAIN", "HASH")
        every { refreshTokenRepository.save(any()) } returns mockk(relaxed = true)
        every { issuedTokenAuditRepository.save(any()) } returns mockk(relaxed = true)
        every { jwtProperties.accessTokenTtl } returns java.time.Duration.ofSeconds(900)
        every { jwtProperties.refreshTokenTtl } returns java.time.Duration.ofDays(7)

        every { tenantIpAllowlistRepository.findAllByTenantId(any()) } returns emptyList()
        every { tenantIpAllowlistCache.get(any()) } returns null
        every { tenantIpAllowlistCache.set(any(), any()) } just runs
        every { tenant.isActive() } returns true

        val response = useCase.execute(request, context)

        assertAll(
            { assertEquals(true, claimsSlot.captured.mustChangePassword) },
            { assertEquals(true, response.mustChangePassword) }
        )
    }

    @Test
    fun `must build jwt claims with user identity and enabled modules`() {
        val user = mockk<User>(relaxed = true)
        every { user.id } returns userId
        every { user.tenantId } returns tenantId
        every { user.mustChangePassword() } returns false
        every { user.authenticate(any(), any()) } just runs
        every { userRepository.findByEmail(Email("valid.email@delta.com")) } returns user
        every { userRepository.save(user) } returns user

        val tenant = mockk<Tenant>()
        every { tenantRepository.findById(tenantId) } returns tenant

        val tenantModule = mockk<TenantModule>()
        every { tenantModule.moduleId } returns moduleId
        every { tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true) } returns listOf(tenantModule)

        val module = mockk<Module>()
        every { module.code } returns ModuleCode("customers")
        every { moduleRepository.findAllByIds(any()) } returns listOf(module)

        every { roleRepository.findAllByUserId(userId) } returns emptyList()

        val claimsSlot = slot<UserClaims>()
        val issuedJwt = IssuedJwt("JWT", UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(900))
        every { jwtIssuer.issueForUser(capture(claimsSlot)) } returns issuedJwt

        every { refreshTokenGenerator.generate() } returns GeneratedRefreshToken("PLAIN", "HASH")
        every { refreshTokenRepository.save(any()) } returns mockk(relaxed = true)
        every { issuedTokenAuditRepository.save(any()) } returns mockk(relaxed = true)
        every { jwtProperties.accessTokenTtl } returns java.time.Duration.ofSeconds(900)
        every { jwtProperties.refreshTokenTtl } returns java.time.Duration.ofDays(7)

        every { tenantIpAllowlistRepository.findAllByTenantId(any()) } returns emptyList()
        every { tenantIpAllowlistCache.get(any()) } returns null
        every { tenantIpAllowlistCache.set(any(), any()) } just runs
        every { tenant.isActive() } returns true

        useCase.execute(request, context)

        val claims = claimsSlot.captured
        assertAll(
            { assertEquals(userId, claims.userId) },
            { assertEquals(tenantId, claims.tenantId) },
            { assertEquals(listOf("customers"), claims.modules) },
            { assertEquals(false, claims.mustChangePassword) }
        )
    }

    @Test
    fun `must issue access and refresh tokens on successful login`() {
        val user = mockk<User>(relaxed = true)
        every { user.id } returns userId
        every { user.tenantId } returns tenantId
        every { user.mustChangePassword() } returns false
        every { user.authenticate(any(), any()) } just runs
        every { userRepository.findByEmail(Email("valid.email@delta.com")) } returns user
        every { userRepository.save(user) } returns user
        val tenant = mockk<Tenant>()
        every { tenantRepository.findById(tenantId) } returns tenant

        every { tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true) } returns emptyList()
        every { roleRepository.findAllByUserId(userId) } returns emptyList()

        val issuedJwt = IssuedJwt(
            token = "JWT-ACCESS-TOKEN",
            jti = UUID.randomUUID(),
            issuedAt = Instant.now(),
            expiresAt = Instant.now().plusSeconds(900)
        )
        val claimsSlot = slot<UserClaims>()
        every { jwtIssuer.issueForUser(capture(claimsSlot)) } returns issuedJwt

        val generated = GeneratedRefreshToken(plainText = "PLAIN-TOKEN", hash = "HASH-INTERNO")
        every { refreshTokenGenerator.generate() } returns generated

        val refreshSlot = slot<RefreshToken>()
        every { refreshTokenRepository.save(capture(refreshSlot)) } returns mockk(relaxed = true)
        every { issuedTokenAuditRepository.save(any()) } returns mockk(relaxed = true)

        every { jwtProperties.accessTokenTtl } returns java.time.Duration.ofSeconds(900)
        every { jwtProperties.refreshTokenTtl } returns java.time.Duration.ofDays(7)

        every { tenantIpAllowlistRepository.findAllByTenantId(any()) } returns emptyList()
        every { tenantIpAllowlistCache.get(any()) } returns null
        every { tenantIpAllowlistCache.set(any(), any()) } just runs
        every { tenant.isActive() } returns true

        val response = useCase.execute(request, context)

        assertAll(
            { assertEquals("JWT-ACCESS-TOKEN", response.accessToken) },
            { assertEquals("PLAIN-TOKEN", response.refreshToken) },
            { assertEquals("Bearer", response.tokenType) },
            { assertEquals(900, response.expiresIn) },
            { assertEquals(false, response.mustChangePassword) }
        )

        assertAll(
            { assertEquals(userId, claimsSlot.captured.userId) },
            { assertEquals(tenantId, claimsSlot.captured.tenantId) },
            { assertEquals(false, claimsSlot.captured.mustChangePassword) }
        )

        assertAll(
            { assertEquals("HASH-INTERNO", refreshSlot.captured.tokenHash) },
            { assertEquals("192.168.0.1", refreshSlot.captured.ipAddress) },
            { assertEquals("test-agent", refreshSlot.captured.userAgent) }
        )

        verify(exactly = 1) { jwtIssuer.issueForUser(any()) }
        verify(exactly = 1) { issuedTokenAuditRepository.save(any()) }
    }

    @Test
    fun `must allow login when ip matches an allowlist entry`() {
        val user = mockk<User>(relaxed = true)

        val tenant = mockk<Tenant>()

        every { user.id } returns userId
        every { user.tenantId } returns tenantId
        every { user.mustChangePassword() } returns false
        every { user.authenticate(any(), any()) } just runs
        every { userRepository.findByEmail(Email("valid.email@delta.com")) } returns user
        every { userRepository.save(user) } returns user
        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant
        every { tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true) } returns emptyList()
        every { roleRepository.findAllByUserId(userId) } returns emptyList()
        every { jwtIssuer.issueForUser(any()) } returns IssuedJwt("JWT", UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(900))
        every { refreshTokenGenerator.generate() } returns GeneratedRefreshToken("PLAIN", "HASH")
        every { refreshTokenRepository.save(any()) } returns mockk(relaxed = true)
        every { issuedTokenAuditRepository.save(any()) } returns mockk(relaxed = true)
        every { jwtProperties.accessTokenTtl } returns java.time.Duration.ofSeconds(900)
        every { jwtProperties.refreshTokenTtl } returns java.time.Duration.ofDays(7)

        val entry = mockk<TenantIpAllowlist>()
        every { entry.cidr } returns Cidr("192.168.0.0/24")
        every { tenantIpAllowlistRepository.findAllByTenantId(tenantId) } returns listOf(entry)
        every { tenantIpAllowlistCache.get(any()) } returns null
        every { tenantIpAllowlistCache.set(any(), any()) } just runs

        val response = useCase.execute(request, context)
        assertEquals("JWT", response.accessToken)
        verify(exactly = 1) { jwtIssuer.issueForUser(any()) }
    }

    @Test
    fun `must throw IpNotAllowedException when ip is not in the allowlist`() {
        val user = mockk<User>(relaxed = true)
        every { user.tenantId } returns tenantId
        every { userRepository.findByEmail(Email("valid.email@delta.com")) } returns user
        every { tenantRepository.findById(tenantId) } returns mockk<Tenant>()

        val entry = mockk<TenantIpAllowlist>()
        every { entry.cidr } returns Cidr("10.0.0.0/8")
        every { tenantIpAllowlistRepository.findAllByTenantId(tenantId) } returns listOf(entry)
        every { tenantIpAllowlistCache.get(any()) } returns null
        every { tenantIpAllowlistCache.set(any(), any()) } just runs

        assertThrows(IpNotAllowedException::class.java) {
            useCase.execute(request, context)
        }

        verify(exactly = 0) { user.authenticate(any(), any()) }
        verify(exactly = 0) { jwtIssuer.issueForUser(any()) }
    }

    @Test
    fun `must throw IpNotAllowedException when ip is null and allowlist exists`() {
        val contextWithoutIp = LoginContext(ipAddress = null, userAgent = "test-agent")

        val user = mockk<User>(relaxed = true)
        every { user.tenantId } returns tenantId
        every { userRepository.findByEmail(Email("valid.email@delta.com")) } returns user
        every { tenantRepository.findById(tenantId) } returns mockk<Tenant>()

        val entry = mockk<TenantIpAllowlist>()
        every { entry.cidr } returns Cidr("192.168.0.0/24")
        every { tenantIpAllowlistRepository.findAllByTenantId(tenantId) } returns listOf(entry)
        every { tenantIpAllowlistCache.get(any()) } returns null
        every { tenantIpAllowlistCache.set(any(), any()) } just runs

        assertThrows(IpNotAllowedException::class.java) {
            useCase.execute(request, contextWithoutIp)
        }

        verify(exactly = 0) { user.authenticate(any(), any()) }
    }

    @Test
    fun `must use cached allowlist without querying repository on cache hit`() {
        val user = mockk<User>(relaxed = true)
        val tenant = mockk<Tenant>()

        every { user.id } returns userId
        every { user.tenantId } returns tenantId
        every { user.mustChangePassword() } returns false
        every { user.authenticate(any(), any()) } just runs

        every { userRepository.findByEmail(Email("valid.email@delta.com")) } returns user
        every { userRepository.save(user) } returns user

        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant
        every { tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true) } returns emptyList()

        every { roleRepository.findAllByUserId(userId) } returns emptyList()
        every { jwtIssuer.issueForUser(any()) } returns IssuedJwt("JWT", UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(900))

        every { refreshTokenGenerator.generate() } returns GeneratedRefreshToken("PLAIN", "HASH")
        every { refreshTokenRepository.save(any()) } returns mockk(relaxed = true)
        every { issuedTokenAuditRepository.save(any()) } returns mockk(relaxed = true)
        every { jwtProperties.accessTokenTtl } returns java.time.Duration.ofSeconds(900)
        every { jwtProperties.refreshTokenTtl } returns java.time.Duration.ofDays(7)

        every { tenantIpAllowlistCache.get(tenantId) } returns listOf("192.168.0.0/24")

        useCase.execute(request, context)

        verify(exactly = 0) { tenantIpAllowlistRepository.findAllByTenantId(any()) }
        verify(exactly = 0) { tenantIpAllowlistCache.set(any(), any()) }
    }

    @Test
    fun `must populate cache after fetching allowlist from repository on cache miss`() {
        val user = mockk<User>(relaxed = true)

        val tenant = mockk<Tenant>()

        every { user.id } returns userId
        every { user.tenantId } returns tenantId
        every { user.mustChangePassword() } returns false
        every { user.authenticate(any(), any()) } just runs

        every { userRepository.findByEmail(Email("valid.email@delta.com")) } returns user
        every { userRepository.save(user) } returns user

        every { tenant.isActive() } returns true
        every { tenantRepository.findById(tenantId) } returns tenant
        every { tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true) } returns emptyList()

        every { roleRepository.findAllByUserId(userId) } returns emptyList()
        every { jwtIssuer.issueForUser(any()) } returns IssuedJwt("JWT", UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(900))

        every { refreshTokenGenerator.generate() } returns GeneratedRefreshToken("PLAIN", "HASH")
        every { refreshTokenRepository.save(any()) } returns mockk(relaxed = true)
        every { issuedTokenAuditRepository.save(any()) } returns mockk(relaxed = true)
        every { jwtProperties.accessTokenTtl } returns java.time.Duration.ofSeconds(900)
        every { jwtProperties.refreshTokenTtl } returns java.time.Duration.ofDays(7)

        val entry = mockk<TenantIpAllowlist>()
        every { entry.cidr } returns Cidr("192.168.0.0/24")
        every { tenantIpAllowlistCache.get(tenantId) } returns null
        every { tenantIpAllowlistRepository.findAllByTenantId(tenantId) } returns listOf(entry)
        every { tenantIpAllowlistCache.set(eq(tenantId), any()) } just runs

        useCase.execute(request, context)

        verify(exactly = 1) { tenantIpAllowlistRepository.findAllByTenantId(tenantId) }
        verify(exactly = 1) { tenantIpAllowlistCache.set(tenantId, listOf("192.168.0.0/24")) }
    }
}