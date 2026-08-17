package br.com.deltaglobalbank.identity.features.auth.login

import br.com.deltaglobalbank.identity.domain.ipAllowlist.Cidr
import br.com.deltaglobalbank.identity.domain.ipAllowlist.IpNotAllowedException
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlistRepository
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository
import br.com.deltaglobalbank.identity.domain.role.RoleRepository
import br.com.deltaglobalbank.identity.domain.shared.PasswordHasher
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.domain.token.IssuedTokenAudit
import br.com.deltaglobalbank.identity.domain.token.IssuedTokenAuditRepository
import br.com.deltaglobalbank.identity.domain.token.RefreshToken
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRepository
import br.com.deltaglobalbank.identity.domain.user.Email
import br.com.deltaglobalbank.identity.domain.user.InvalidCredentialsException
import br.com.deltaglobalbank.identity.domain.user.TenantNotActiveException
import br.com.deltaglobalbank.identity.domain.user.User
import br.com.deltaglobalbank.identity.domain.user.UserRepository
import br.com.deltaglobalbank.identity.infrastructure.security.ipAllowlist.TenantIpAllowlistCache
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.IssuedJwt
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.JwtIssuer
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.JwtIssuerProperties
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.UserClaims
import br.com.deltaglobalbank.identity.infrastructure.security.token.RefreshTokenGenerator
import com.github.f4b6a3.uuid.UuidCreator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Instant
import java.util.UUID

data class LoginContext(
    val ipAddress: String?,
    val userAgent: String?
)

data class LoginResult(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val mustChangePassword: Boolean
)

@Service
class LoginUseCase(
    private val userRepository: UserRepository,
    private val tenantRepository: TenantRepository,
    private val roleRepository: RoleRepository,
    private val tenantModuleRepository: TenantModuleRepository,
    private val moduleRepository: ModuleRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val issuedTokenAuditRepository: IssuedTokenAuditRepository,
    private val passwordHasher: PasswordHasher,
    private val refreshTokenGenerator: RefreshTokenGenerator,
    private val jwtIssuer: JwtIssuer,
    private val jwtProperties: JwtIssuerProperties,
    private val tenantIpAllowlistRepository: TenantIpAllowlistRepository,
    private val tenantIpAllwlistCache: TenantIpAllowlistCache
) {

    private val timingRandom = SecureRandom()

    @Transactional
    fun execute(request: LoginRequest, context: LoginContext): LoginResult {
        val email = parseEmailOrFail(request.email)

        val user = userRepository.findByEmail(email)
            ?: failWithTimingDelay()

        val tenant = tenantRepository.findById(user.tenantId)
            ?: throw InvalidCredentialsException()

        assertIpAllowed(user.tenantId, context.ipAddress)

        val isPlatformAdmin = roleRepository.findAllByUserId(user.id)
            .any { it.code.value == "platform.admin" }

        if (!isPlatformAdmin && !tenant.isActive()) {
            throw TenantNotActiveException()
        }

        user.authenticate(request.password, passwordHasher)
        userRepository.save(user)

        val (roleCodes, moduleCodes) = resolveRolesAndModules(user)
        val issuedJwt = issueAccessToken(user, roleCodes, moduleCodes)
        val refreshToken = createRefreshToken(user, context)
        auditIssuedToken(user, issuedJwt, context)

        return LoginResult(
            accessToken = issuedJwt.token,
            refreshToken = refreshToken,
            expiresIn = jwtProperties.accessTokenTtl.seconds,
            mustChangePassword = user.mustChangePassword()
        )
    }

    private fun parseEmailOrFail(rawEmail: String): Email {
        return try {
            Email(rawEmail)
        } catch (ex: IllegalArgumentException) {
            failWithTimingDelay()
        }
    }

    private fun failWithTimingDelay(): Nothing {
        Thread.sleep(200L + timingRandom.nextInt(150))
        throw InvalidCredentialsException()
    }

    private fun resolveRolesAndModules(user: User): Pair<List<String>, List<String>> {
        val enabledTenantModules = tenantModuleRepository.findAllByTenantIdAndEnabled(user.tenantId, true)
        val enabledModuleIds = enabledTenantModules.map { it.moduleId }.toSet()

        val moduleCodes = if (enabledModuleIds.isEmpty()) {
            emptyList()
        } else {
            moduleRepository.findAllByIds(enabledModuleIds).map { it.code.value }
        }

        val roleCodes = roleRepository.findAllByUserId(user.id)
            .filter { role -> role.moduleId == null || role.moduleId in enabledModuleIds }
            .map { it.code.value }

        return roleCodes to moduleCodes
    }

    private fun issueAccessToken(
        user: User,
        roleCodes: List<String>,
        moduleCodes: List<String>
    ) = jwtIssuer.issueForUser(
        UserClaims(
            userId = user.id,
            tenantId = user.tenantId,
            roles = roleCodes,
            modules = moduleCodes,
            mustChangePassword = user.mustChangePassword()
        )
    )

    private fun createRefreshToken(user: User, context: LoginContext): String {
        val token = refreshTokenGenerator.generate()
        val now = Instant.now()
        refreshTokenRepository.save(
            RefreshToken(
                id = UuidCreator.getTimeOrderedEpoch(),
                userId = user.id,
                tokenHash = token.hash,
                expiresAt = now.plus(jwtProperties.refreshTokenTtl),
                revokedAt = null,
                createdAt = now,
                lastUsedAt = null,
                userAgent = context.userAgent,
                ipAddress = context.ipAddress
            )
        )
        return token.plainText
    }

    private fun auditIssuedToken(user: User, issuedJwt: IssuedJwt, context: LoginContext) {
        issuedTokenAuditRepository.save(
            IssuedTokenAudit(
                id = UuidCreator.getTimeOrderedEpoch(),
                jti = issuedJwt.jti,
                principalType = "user",
                principalId = user.id,
                tenantId = user.tenantId,
                issuedAt = issuedJwt.issuedAt,
                expiresAt = issuedJwt.expiresAt,
                ipAddress = context.ipAddress,
                userAgent = context.userAgent
            )
        )
    }

    private fun assertIpAllowed(tenantId: UUID, ipAddress: String?) {
        val cidrs = tenantIpAllwlistCache.get(tenantId)
            ?: tenantIpAllowlistRepository.findAllByTenantId(tenantId)
                .map { it.cidr.value }
                .also { tenantIpAllwlistCache.set(tenantId, it) }

        if (cidrs.isEmpty()) return
        if (ipAddress == null || cidrs.none { Cidr(it).matches(ipAddress) }) throw IpNotAllowedException()
    }
}
