package br.com.deltaglobalbank.identity.features.apiClient.exchangeApiKey;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClient;
import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientRepository;
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKey;
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKeyRepository;
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository;
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository;
import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleCode;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import br.com.deltaglobalbank.identity.domain.shared.PasswordHasher;
import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.token.IssuedTokenAuditRepository;
import br.com.deltaglobalbank.identity.domain.user.HashedPassword;
import br.com.deltaglobalbank.identity.features.exchangeApiKey.ExchangeApiKeyCommand;
import br.com.deltaglobalbank.identity.features.exchangeApiKey.ExchangeApiKeyUseCase;
import br.com.deltaglobalbank.identity.infrastructure.security.apikey.ApiKeyFingerprinter;
import br.com.deltaglobalbank.identity.infrastructure.security.exchangeApiKey.ExchangeApiKeyCache;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.ApiClientClaims;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.IssuedJwt;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.JwtIssuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.ObjectMapper;

class ExchangeApiKeyUseCaseTests {

    private final ApiKeyRepository apiKeyRepository = mock(ApiKeyRepository.class);
    private final ApiClientRepository apiClientRepository = mock(ApiClientRepository.class);
    private final TenantRepository tenantRepository = mock(TenantRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final TenantModuleRepository tenantModuleRepository = mock(TenantModuleRepository.class);
    private final ModuleRepository moduleRepository = mock(ModuleRepository.class);
    private final IssuedTokenAuditRepository issuedTokenAuditRepository = mock(IssuedTokenAuditRepository.class);
    private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
    private final ApiKeyFingerprinter fingerprinter = mock(ApiKeyFingerprinter.class);
    private final ExchangeApiKeyCache cache = mock(ExchangeApiKeyCache.class);
    private final JwtIssuer jwtIssuer = mock(JwtIssuer.class);
    private ObjectMapper objectMapper;

    private ExchangeApiKeyUseCase useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID apiClientId = UUID.randomUUID();
    private final String rawKey = "raw-api-key";
    private final String fingerprint = "a".repeat(64);

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        useCase = new ExchangeApiKeyUseCase(
            apiKeyRepository, apiClientRepository, tenantRepository, roleRepository,
            tenantModuleRepository, moduleRepository, issuedTokenAuditRepository,
            passwordHasher, fingerprinter, cache, jwtIssuer, objectMapper
        );

        when(cache.get(fingerprint)).thenReturn(null);
        when(fingerprinter.fingerprint(rawKey)).thenReturn(fingerprint);

        ApiKey apiKey = ApiKey.create(
            UUID.randomUUID(), apiClientId, "key", new HashedPassword("hash"), "pfx", fingerprint, null);
        when(apiKeyRepository.findByFingerprint(fingerprint)).thenReturn(apiKey);
        when(passwordHasher.matches(rawKey, new HashedPassword("hash"))).thenReturn(true);
        when(apiKeyRepository.save(any())).thenReturn(apiKey);

        ApiClient apiClient = ApiClient.newApiClient(apiClientId, tenantId, "client", null);
        when(apiClientRepository.findById(apiClientId)).thenReturn(apiClient);

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);
    }

    @Test
    void mustExcludeInactiveRolesFromApiClientJwtClaims() {
        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenant.getId()).thenReturn(tenantId);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        Role activeRole = mock(Role.class);
        when(activeRole.getCode()).thenReturn(new RoleCode("customers.admin"));
        when(activeRole.getModuleId()).thenReturn(null);

        when(roleRepository.findAllByApiClientId(apiClientId)).thenReturn(List.of(activeRole));

        ArgumentCaptor<ApiClientClaims> claimsCaptor = ArgumentCaptor.forClass(ApiClientClaims.class);
        IssuedJwt issuedJwt = new IssuedJwt("JWT", UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(900));
        when(jwtIssuer.issueForApiClient(claimsCaptor.capture())).thenReturn(issuedJwt);

        useCase.execute(new ExchangeApiKeyCommand(rawKey, "127.0.0.1", "test"));

        List<String> roles = claimsCaptor.getValue().roles();
        assertTrue(roles.contains("customers.admin"));
        assertFalse(roles.contains("special.custom"));
    }
}
