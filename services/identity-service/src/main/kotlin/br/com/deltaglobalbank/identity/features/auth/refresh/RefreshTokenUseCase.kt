package br.com.deltaglobalbank.identity.features.auth.refresh

import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.domain.token.InvalidRefreshTokenException
import br.com.deltaglobalbank.identity.domain.token.RefreshToken
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRepository
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenReuseDetectedException
import br.com.deltaglobalbank.identity.domain.user.User
import br.com.deltaglobalbank.identity.domain.user.UserRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaIssuedTokenAuditRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaModuleRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaRoleRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantModuleRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.IssuedTokenAuditEntity
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.JwtIssuer
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.JwtIssuerProperties
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.UserClaims
import br.com.deltaglobalbank.identity.infrastructure.security.token.RefreshTokenGenerator
import com.github.f4b6a3.uuid.UuidCreator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

data class RefreshTokenContext(
    val ipAddress: String?,
    val userAgent: String?
)

data class RefreshTokenResult(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val mustChangePassword: Boolean = false
)

@Service
class RefreshTokenUseCase(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userRepository: UserRepository,
    private val tenantRepository: TenantRepository,
    private val userRoleRepository: JpaUserRoleRepository,
    private val roleRepository: JpaRoleRepository,
    private val tenantModuleRepository: JpaTenantModuleRepository,
    private val moduleRepository: JpaModuleRepository,
    private val issuedTokenAuditRepository: JpaIssuedTokenAuditRepository,
    private val refreshTokenGenerator: RefreshTokenGenerator,
    private val jwtIssuer: JwtIssuer,

    private val jwtProperties: JwtIssuerProperties
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun execute(refreshToken: String, context: RefreshTokenContext): RefreshTokenResult {
        val hash = refreshTokenGenerator.hash(refreshToken)

        val existing = refreshTokenRepository.findByTokenHash(hash)
            ?: throw InvalidRefreshTokenException()

        if (existing.isRevoked()) {
            log.warn(
                "Reuse de refresh token detectado para user {}. Revogando todos os tokens ativos.",
                existing.userId
            )
            revokeAllActiveTokensOfUser(existing.userId)
            throw RefreshTokenReuseDetectedException()
        }

        if (existing.isExpired()) {
            throw InvalidRefreshTokenException()
        }

        val user = userRepository.findById(existing.userId)
            ?: throw InvalidRefreshTokenException()

        val tenant = tenantRepository.findById(user.tenantId)
            ?: throw InvalidRefreshTokenException()

        val roleIds = userRoleRepository.findAllByUserId(user.id).map { it.roleId }.toSet()
        val isPlatformAdmin = roleIds.isNotEmpty() &&
                roleRepository.findAllById(roleIds).any { it.code == "platform.admin" }

        if (!isPlatformAdmin && !tenant.isActive()) {
            throw InvalidRefreshTokenException()
        }

        if (!user.isActive()) throw InvalidRefreshTokenException()

        existing.markUsed()
        existing.revoke()
        refreshTokenRepository.save(existing)

        val (roleCodes, moduleCodes) = resolveRolesAndModules(user)
        val issuedJwt = jwtIssuer.issueForUser(
            UserClaims(
                userId = user.id,
                tenantId = user.tenantId,
                roles = roleCodes,
                modules = moduleCodes,
                mustChangePassword = user.mustChangePassword()
            )
        )

        val newRawToken = refreshTokenGenerator.generate()
        val newRefreshToken = RefreshToken.newToken(
            id = UuidCreator.getTimeOrderedEpoch(),
            userId = user.id,
            tokenHash = newRawToken.hash,
            expiresAt = existing.expiresAt,
            userAgent = context.userAgent,
            ipAddress = context.ipAddress
        )
        refreshTokenRepository.save(newRefreshToken)

        issuedTokenAuditRepository.save(
            IssuedTokenAuditEntity(
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

        val now = Instant.now()
        val expiresInSeconds = java.time.Duration.between(now, existing.expiresAt).seconds
            .coerceAtLeast(0)

        return RefreshTokenResult(
            accessToken = issuedJwt.token,
            refreshToken = newRawToken.plainText,
            expiresIn = jwtProperties.accessTokenTtl.seconds,
            mustChangePassword = user.mustChangePassword()
        )
    }

    private fun revokeAllActiveTokensOfUser(userId: java.util.UUID) {
        val active = refreshTokenRepository.findAllActiveByUserId(userId)
        if (active.isEmpty()) return
        active.forEach { it.revoke() }
        refreshTokenRepository.saveAll(active)
    }

    private fun resolveRolesAndModules(user: User): Pair<List<String>, List<String>> {
        val enabledTenantModules = tenantModuleRepository
            .findAllByTenantIdAndEnabled(user.tenantId, true)
        val enabledModuleIds = enabledTenantModules.map { it.moduleId }.toSet()

        val moduleCodes = if (enabledModuleIds.isEmpty()) {
            emptyList()
        } else {
            moduleRepository.findAllById(enabledModuleIds).map { it.code }
        }

        val userRoles = userRoleRepository.findAllByUserId(user.id)
        val roleIds = userRoles.map { it.roleId }.toSet()

        if (roleIds.isEmpty()) {
            return emptyList<String>() to moduleCodes
        }

        val roleCodes = roleRepository.findAllById(roleIds)
            .filter { it.moduleId == null || it.moduleId in enabledModuleIds }
            .map { it.code }

        return roleCodes to moduleCodes
    }
}