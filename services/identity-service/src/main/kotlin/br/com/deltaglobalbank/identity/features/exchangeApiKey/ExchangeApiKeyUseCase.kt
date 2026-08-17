package br.com.deltaglobalbank.identity.features.exchangeApiKey

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClient
import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientRepository
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKeyRepository
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository
import br.com.deltaglobalbank.identity.domain.role.RoleRepository
import br.com.deltaglobalbank.identity.domain.shared.PasswordHasher
import br.com.deltaglobalbank.identity.domain.tenant.Tenant
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.domain.token.IssuedTokenAudit
import br.com.deltaglobalbank.identity.domain.token.IssuedTokenAuditRepository
import br.com.deltaglobalbank.identity.infrastructure.security.apikey.ApiKeyFingerprinter
import br.com.deltaglobalbank.identity.infrastructure.security.exchangeApiKey.ExchangeApiKeyCache
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.ApiClientClaims
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.IssuedJwt
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.JwtIssuer
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.f4b6a3.uuid.UuidCreator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Base64
import java.util.UUID

@Service
class ExchangeApiKeyUseCase(
    private val apiKeyRepository: ApiKeyRepository,
    private val apiClientRepository: ApiClientRepository,
    private val tenantRepository: TenantRepository,
    private val roleRepository: RoleRepository,
    private val tenantModuleRepository: TenantModuleRepository,
    private val moduleRepository: ModuleRepository,
    private val issuedTokenAuditRepository: IssuedTokenAuditRepository,
    private val passwordHasher: PasswordHasher,
    private val fingerprinter: ApiKeyFingerprinter,
    private val cache: ExchangeApiKeyCache,
    private val jwtIssuer: JwtIssuer,
    private val objectMapper: ObjectMapper
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun execute(command: ExchangeApiKeyCommand): ExchangeApiKeyResult {
        if (command.apiKey.isBlank()) {
            throw ApiKeyRequiredException()
        }

        val fingerprint = fingerprinter.fingerprint(command.apiKey)

        cache.get(fingerprint)?.let { cachedToken ->
            log.debug("ExchangeApiKey cache HIT fingerprint={}", fingerprint.take(8))
            return decodeToResult(cachedToken)
        }

        log.debug("ExchangeApiKey cache MISS fingerprint={}", fingerprint.take(8))
        return processInDatabase(command, fingerprint)
    }

    @Transactional
    protected fun processInDatabase(
        command: ExchangeApiKeyCommand,
        fingerprint: String
    ): ExchangeApiKeyResult {
        val apiKey = apiKeyRepository.findByFingerprint(fingerprint)
            ?: throw InvalidApiKeyException()

        if (!passwordHasher.matches(command.apiKey, apiKey.keyHash)) {
            log.warn(
                "ExchangeApiKey fingerprint match mas bcrypt falhou: prefix={}",
                apiKey.keyPrefix
            )
            throw InvalidApiKeyException()
        }

        if (!apiKey.isActive()) {
            throw InvalidApiKeyException()
        }

        val apiClient = apiClientRepository.findById(apiKey.apiClientId)
            ?: throw InvalidApiKeyException()

        if (!apiClient.isActive()) {
            throw ApiClientSuspendedForExchangeException()
        }

        val tenant = tenantRepository.findById(apiClient.tenantId)
            ?: throw InvalidApiKeyException()

        if (!tenant.isActive()) {
            throw TenantInactiveForExchangeException()
        }

        // monta claims (mesmo padrão do LoginUseCase)
        val (roleCodes, moduleCodes) = resolveRolesAndModules(apiClient, tenant)

        val issued = jwtIssuer.issueForApiClient(
            ApiClientClaims(
                apiClientId = apiClient.id,
                tenantId = tenant.id,
                roles = roleCodes,
                modules = moduleCodes
            )
        )

        // marca uso e audita
        apiKey.markUsed()
        apiKeyRepository.save(apiKey)

        auditIssuedToken(apiClient.id, tenant.id, issued, command)

        val ttlSeconds = issued.expiresAt.epochSecond - issued.issuedAt.epochSecond
        cache.set(fingerprint, issued.token, ttlSeconds)

        return ExchangeApiKeyResult(
            internalToken = issued.token,
            expiresAtEpochSeconds = issued.expiresAt.epochSecond,
            principalId = apiClient.id.toString(),
            tenantId = tenant.id.toString()
        )
    }

    private fun resolveRolesAndModules(
        apiClient: ApiClient,
        tenant: Tenant
    ): Pair<List<String>, List<String>> {
        val enabledTenantModules = tenantModuleRepository
            .findAllByTenantIdAndEnabled(tenant.id, true)
        val enabledModuleIds = enabledTenantModules.map { it.moduleId }.toSet()

        val moduleCodes = if (enabledModuleIds.isEmpty()) {
            emptyList()
        } else {
            moduleRepository.findAllByIds(enabledModuleIds).map { it.code.value }
        }

        val roleCodes = roleRepository.findAllByApiClientId(apiClient.id)
            .filter { role -> role.moduleId == null || role.moduleId in enabledModuleIds }
            .map { it.code.value }

        return roleCodes to moduleCodes
    }

    private fun auditIssuedToken(
        apiClientId: UUID,
        tenantId: UUID,
        issued: IssuedJwt,
        command: ExchangeApiKeyCommand
    ) {
        issuedTokenAuditRepository.save(
            IssuedTokenAudit(
                id = UuidCreator.getTimeOrderedEpoch(),
                jti = issued.jti,
                principalType = "api_client",
                principalId = apiClientId,
                tenantId = tenantId,
                issuedAt = issued.issuedAt,
                expiresAt = issued.expiresAt,
                ipAddress = command.sourceIp.takeIf { it.isNotBlank() },
                userAgent = command.userAgent.takeIf { it.isNotBlank() }
            )
        )
    }

    private fun decodeToResult(token: String): ExchangeApiKeyResult {
        val parts = token.split(".")
        require(parts.size == 3) { "invalid_cached_jwt_format" }

        val payloadJson = String(Base64.getUrlDecoder().decode(parts[1]))

        @Suppress("UNCHECKED_CAST")
        val claims = objectMapper.readValue(payloadJson, Map::class.java) as Map<String, Any>

        return ExchangeApiKeyResult(
            internalToken = token,
            expiresAtEpochSeconds = (claims["exp"] as Number).toLong(),
            principalId = claims["sub"] as String,
            tenantId = claims["tenant_id"] as String
        )
    }
}