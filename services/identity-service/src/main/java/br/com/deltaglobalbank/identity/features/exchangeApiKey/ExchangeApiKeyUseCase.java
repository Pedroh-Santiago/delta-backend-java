package br.com.deltaglobalbank.identity.features.exchangeApiKey;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClient;
import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientRepository;
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKey;
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKeyRepository;
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository;
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository;
import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import br.com.deltaglobalbank.identity.domain.shared.PasswordHasher;
import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.token.IssuedTokenAudit;
import br.com.deltaglobalbank.identity.domain.token.IssuedTokenAuditRepository;
import br.com.deltaglobalbank.identity.infrastructure.security.apikey.ApiKeyFingerprinter;
import br.com.deltaglobalbank.identity.infrastructure.security.exchangeApiKey.ExchangeApiKeyCache;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.ApiClientClaims;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.IssuedJwt;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.JwtIssuer;
import tools.jackson.databind.ObjectMapper;
import com.github.f4b6a3.uuid.UuidCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExchangeApiKeyUseCase {

    private static final Logger log = LoggerFactory.getLogger(ExchangeApiKeyUseCase.class);

    private final ApiKeyRepository apiKeyRepository;
    private final ApiClientRepository apiClientRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final TenantModuleRepository tenantModuleRepository;
    private final ModuleRepository moduleRepository;
    private final IssuedTokenAuditRepository issuedTokenAuditRepository;
    private final PasswordHasher passwordHasher;
    private final ApiKeyFingerprinter fingerprinter;
    private final ExchangeApiKeyCache cache;
    private final JwtIssuer jwtIssuer;
    private final ObjectMapper objectMapper;

    public ExchangeApiKeyUseCase(
        ApiKeyRepository apiKeyRepository,
        ApiClientRepository apiClientRepository,
        TenantRepository tenantRepository,
        RoleRepository roleRepository,
        TenantModuleRepository tenantModuleRepository,
        ModuleRepository moduleRepository,
        IssuedTokenAuditRepository issuedTokenAuditRepository,
        PasswordHasher passwordHasher,
        ApiKeyFingerprinter fingerprinter,
        ExchangeApiKeyCache cache,
        JwtIssuer jwtIssuer,
        ObjectMapper objectMapper
    ) {
        this.apiKeyRepository = apiKeyRepository;
        this.apiClientRepository = apiClientRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.tenantModuleRepository = tenantModuleRepository;
        this.moduleRepository = moduleRepository;
        this.issuedTokenAuditRepository = issuedTokenAuditRepository;
        this.passwordHasher = passwordHasher;
        this.fingerprinter = fingerprinter;
        this.cache = cache;
        this.jwtIssuer = jwtIssuer;
        this.objectMapper = objectMapper;
    }

    public ExchangeApiKeyResult execute(ExchangeApiKeyCommand command) {
        if (command.apiKey().isBlank()) {
            throw new ApiKeyRequiredException();
        }

        String fingerprint = fingerprinter.fingerprint(command.apiKey());

        String cachedToken = cache.get(fingerprint);
        if (cachedToken != null) {
            log.debug("ExchangeApiKey cache HIT fingerprint={}", prefix(fingerprint));
            return decodeToResult(cachedToken);
        }

        log.debug("ExchangeApiKey cache MISS fingerprint={}", prefix(fingerprint));
        return processInDatabase(command, fingerprint);
    }

    @Transactional
    protected ExchangeApiKeyResult processInDatabase(ExchangeApiKeyCommand command, String fingerprint) {
        ApiKey apiKey = apiKeyRepository.findByFingerprint(fingerprint);
        if (apiKey == null) {
            throw new InvalidApiKeyException();
        }

        if (!passwordHasher.matches(command.apiKey(), apiKey.getKeyHash())) {
            log.warn("ExchangeApiKey fingerprint match mas bcrypt falhou: prefix={}", apiKey.getKeyPrefix());
            throw new InvalidApiKeyException();
        }

        if (!apiKey.isActive()) {
            throw new InvalidApiKeyException();
        }

        ApiClient apiClient = apiClientRepository.findById(apiKey.getApiClientId());
        if (apiClient == null) {
            throw new InvalidApiKeyException();
        }

        if (!apiClient.isActive()) {
            throw new ApiClientSuspendedForExchangeException();
        }

        Tenant tenant = tenantRepository.findById(apiClient.getTenantId());
        if (tenant == null) {
            throw new InvalidApiKeyException();
        }

        if (!tenant.isActive()) {
            throw new TenantInactiveForExchangeException();
        }

        RolesAndModules resolved = resolveRolesAndModules(apiClient, tenant);

        IssuedJwt issued = jwtIssuer.issueForApiClient(
            new ApiClientClaims(apiClient.getId(), tenant.getId(), resolved.roleCodes(), resolved.moduleCodes())
        );

        apiKey.markUsed();
        apiKeyRepository.save(apiKey);

        auditIssuedToken(apiClient.getId(), tenant.getId(), issued, command);

        long ttlSeconds = issued.expiresAt().getEpochSecond() - issued.issuedAt().getEpochSecond();
        cache.set(fingerprint, issued.token(), ttlSeconds);

        return new ExchangeApiKeyResult(
            issued.token(),
            issued.expiresAt().getEpochSecond(),
            apiClient.getId().toString(),
            tenant.getId().toString()
        );
    }

    private record RolesAndModules(List<String> roleCodes, List<String> moduleCodes) {
    }

    private RolesAndModules resolveRolesAndModules(ApiClient apiClient, Tenant tenant) {
        List<br.com.deltaglobalbank.identity.domain.module.TenantModule> enabledTenantModules =
            tenantModuleRepository.findAllByTenantIdAndEnabled(tenant.getId(), true);
        Set<UUID> enabledModuleIds = enabledTenantModules.stream()
            .map(br.com.deltaglobalbank.identity.domain.module.TenantModule::getModuleId)
            .collect(Collectors.toSet());

        List<String> moduleCodes = enabledModuleIds.isEmpty()
            ? List.of()
            : moduleRepository.findAllByIds(enabledModuleIds).stream()
                .map(it -> it.getCode().value())
                .toList();

        List<String> roleCodes = roleRepository.findAllByApiClientId(apiClient.getId()).stream()
            .filter(role -> role.getModuleId() == null || enabledModuleIds.contains(role.getModuleId()))
            .map(Role::getCode)
            .map(it -> it.value())
            .toList();

        return new RolesAndModules(roleCodes, moduleCodes);
    }

    private void auditIssuedToken(UUID apiClientId, UUID tenantId, IssuedJwt issued, ExchangeApiKeyCommand command) {
        issuedTokenAuditRepository.save(new IssuedTokenAudit(
            UuidCreator.getTimeOrderedEpoch(),
            issued.jti(),
            "api_client",
            apiClientId,
            tenantId,
            issued.issuedAt(),
            issued.expiresAt(),
            command.sourceIp().isBlank() ? null : command.sourceIp(),
            command.userAgent().isBlank() ? null : command.userAgent()
        ));
    }

    private ExchangeApiKeyResult decodeToResult(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("invalid_cached_jwt_format");
        }

        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

        Map<?, ?> claims;
        try {
            claims = objectMapper.readValue(payloadJson, Map.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid_cached_jwt_format", ex);
        }

        return new ExchangeApiKeyResult(
            token,
            ((Number) claims.get("exp")).longValue(),
            (String) claims.get("sub"),
            (String) claims.get("tenant_id")
        );
    }

    private String prefix(String value) {
        return value.length() > 8 ? value.substring(0, 8) : value;
    }
}
