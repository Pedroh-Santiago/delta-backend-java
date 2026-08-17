package br.com.deltaglobalbank.identity.features.apiClients.createApiKey;

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClient;
import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientRepository;
import br.com.deltaglobalbank.identity.domain.apiKey.ApiClientNotFoundException;
import br.com.deltaglobalbank.identity.domain.apiKey.ApiClientSuspendedException;
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKey;
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKeyRepository;
import br.com.deltaglobalbank.identity.domain.apiKey.TenantInactiveForApiKeyException;
import br.com.deltaglobalbank.identity.domain.shared.PasswordHasher;
import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.user.HashedPassword;
import br.com.deltaglobalbank.identity.domain.user.Password;
import br.com.deltaglobalbank.identity.infrastructure.security.apikey.ApiKeyFingerprinter;
import br.com.deltaglobalbank.identity.infrastructure.security.apikey.ApiKeyGenerator;
import br.com.deltaglobalbank.identity.infrastructure.security.apikey.GeneratedApiKey;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateApiKeyUseCase {

    private final ApiClientRepository apiClientRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final TenantRepository tenantRepository;
    private final PasswordHasher passwordHasher;
    private final ApiKeyGenerator apiKeyGenerator;
    private final ApiKeyFingerprinter apiKeyFingerprinter;

    public CreateApiKeyUseCase(
        ApiClientRepository apiClientRepository,
        ApiKeyRepository apiKeyRepository,
        TenantRepository tenantRepository,
        PasswordHasher passwordHasher,
        ApiKeyGenerator apiKeyGenerator,
        ApiKeyFingerprinter apiKeyFingerprinter
    ) {
        this.apiClientRepository = apiClientRepository;
        this.apiKeyRepository = apiKeyRepository;
        this.tenantRepository = tenantRepository;
        this.passwordHasher = passwordHasher;
        this.apiKeyGenerator = apiKeyGenerator;
        this.apiKeyFingerprinter = apiKeyFingerprinter;
    }

    @Transactional
    public CreateApiKeyResponse execute(CreateApiKeyCommand command) {
        ApiClient apiClient = apiClientRepository.findById(command.apiClientId());
        if (apiClient == null) {
            throw new ApiClientNotFoundException();
        }

        if (command.expectedTenantId() != null && !apiClient.getTenantId().equals(command.expectedTenantId())) {
            throw new ApiClientNotFoundException();
        }

        if (!apiClient.isActive()) {
            throw new ApiClientSuspendedException();
        }

        Tenant tenant = tenantRepository.findById(apiClient.getTenantId());
        if (tenant == null) {
            throw new ApiClientNotFoundException();
        }
        if (!tenant.isActive()) {
            throw new TenantInactiveForApiKeyException();
        }

        GeneratedApiKey generated = apiKeyGenerator.generate();
        HashedPassword keyHash = passwordHasher.hash(new Password(generated.plainKey()));
        String fingerprint = apiKeyFingerprinter.fingerprint(generated.plainKey());

        ApiKey apiKey = ApiKey.create(
            UuidCreator.getTimeOrderedEpoch(),
            apiClient.getId(),
            command.name(),
            keyHash,
            generated.prefix(),
            fingerprint,
            command.expiresAt()
        );

        ApiKey saved = apiKeyRepository.save(apiKey);

        return new CreateApiKeyResponse(
            new CreatedApiKey(
                saved.getId(),
                saved.getApiClientId(),
                saved.getName(),
                saved.getKeyPrefix(),
                saved.snapshot().expiresAt(),
                saved.getCreatedAt()
            ),
            generated.plainKey()
        );
    }
}
