package br.com.deltaglobalbank.identity.features.roles.updateRoleStatus;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleNotFoundException;
import br.com.deltaglobalbank.identity.domain.role.RoleProtectedException;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import br.com.deltaglobalbank.identity.domain.token.AccessTokenRevoker;
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRevoker;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiClientRoleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiKeyEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserRoleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiClientRoleRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiKeyRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository;
import br.com.deltaglobalbank.identity.infrastructure.security.exchangeApiKey.ExchangeApiKeyCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UpdateRoleStatusUseCase {

    private static final String PLATFORM_ADMIN_CODE = "platform.admin";

    private static final Logger log = LoggerFactory.getLogger(UpdateRoleStatusUseCase.class);

    private final RoleRepository roleRepository;
    private final JpaUserRoleRepository jpaUserRoleRepository;
    private final JpaApiClientRoleRepository jpaApiClientRoleRepository;
    private final JpaApiKeyRepository jpaApiKeyRepository;
    private final AccessTokenRevoker accessTokenRevoker;
    private final RefreshTokenRevoker refreshTokenRevoker;
    private final ExchangeApiKeyCache exchangeApiKeyCache;

    public UpdateRoleStatusUseCase(
        RoleRepository roleRepository,
        JpaUserRoleRepository jpaUserRoleRepository,
        JpaApiClientRoleRepository jpaApiClientRoleRepository,
        JpaApiKeyRepository jpaApiKeyRepository,
        AccessTokenRevoker accessTokenRevoker,
        RefreshTokenRevoker refreshTokenRevoker,
        ExchangeApiKeyCache exchangeApiKeyCache
    ) {
        this.roleRepository = roleRepository;
        this.jpaUserRoleRepository = jpaUserRoleRepository;
        this.jpaApiClientRoleRepository = jpaApiClientRoleRepository;
        this.jpaApiKeyRepository = jpaApiKeyRepository;
        this.accessTokenRevoker = accessTokenRevoker;
        this.refreshTokenRevoker = refreshTokenRevoker;
        this.exchangeApiKeyCache = exchangeApiKeyCache;
    }

    public UpdateRoleStatusResponse execute(UpdateRoleStatusCommand command) {
        Role role = roleRepository.findById(command.roleId());
        if (role == null) {
            throw new RoleNotFoundException();
        }

        if (!command.active() && role.getCode().value().equals(PLATFORM_ADMIN_CODE)) {
            throw new RoleProtectedException();
        }

        boolean wasActive = role.isActive();
        Role updatedRole = command.active() ? role.activate() : role.deactivate();
        Role saved = roleRepository.save(updatedRole);

        if (wasActive && !saved.isActive()) {
            revokeUserSessions(saved.getId());
            revokeApiClientSessions(saved.getId());
        }

        log.info("role status updated roleId={} active={}", saved.getId(), saved.isActive());

        return new UpdateRoleStatusResponse(saved.getId(), saved.getCode().value(), saved.isActive());
    }

    private void revokeUserSessions(UUID roleId) {
        Set<UUID> affectedUserIds = jpaUserRoleRepository.findAllByRoleId(roleId).stream()
            .map(UserRoleEntity::getUserId)
            .collect(Collectors.toSet());

        for (UUID userId : affectedUserIds) {
            accessTokenRevoker.revokeUser(userId);
            refreshTokenRevoker.revokeAllForUser(userId);
        }

        log.info("revoked sessions for {} user(s) due to role deactivation roleId={}", affectedUserIds.size(), roleId);
    }

    private void revokeApiClientSessions(UUID roleId) {
        Set<UUID> affectedApiClientIds = jpaApiClientRoleRepository.findAllByRoleId(roleId).stream()
            .map(ApiClientRoleEntity::getApiClientId)
            .collect(Collectors.toSet());

        if (affectedApiClientIds.isEmpty()) {
            return;
        }

        for (UUID apiClientId : affectedApiClientIds) {
            accessTokenRevoker.revokeUser(apiClientId);
        }

        List<ApiKeyEntity> affectedKeys = jpaApiKeyRepository.findAllByApiClientIdIn(affectedApiClientIds);
        for (ApiKeyEntity key : affectedKeys) {
            exchangeApiKeyCache.invalidate(key.getFingerprint());
        }

        log.info(
            "revoked sessions for {} api client(s) and invalidated {} cached key(s) due to role deactivation roleId={}",
            affectedApiClientIds.size(), affectedKeys.size(), roleId
        );
    }
}
