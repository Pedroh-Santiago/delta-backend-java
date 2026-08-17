package br.com.deltaglobalbank.identity.features.apiClients.listClients;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiClientEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiClientRoleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.ApiKeyActiveCount;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiClientRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiClientRoleRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiKeyRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ListClientsUseCase {

    public static final int MAX_PAGE_SIZE = 100;

    private final JpaApiClientRepository jpaApiClientRepository;
    private final JpaTenantRepository tenantRepository;
    private final JpaApiClientRoleRepository jpaApiClientRoleRepository;
    private final RoleRepository roleRepository;
    private final JpaApiKeyRepository jpaApiKeyRepository;

    public ListClientsUseCase(
        JpaApiClientRepository jpaApiClientRepository,
        JpaTenantRepository tenantRepository,
        JpaApiClientRoleRepository jpaApiClientRoleRepository,
        RoleRepository roleRepository,
        JpaApiKeyRepository jpaApiKeyRepository
    ) {
        this.jpaApiClientRepository = jpaApiClientRepository;
        this.tenantRepository = tenantRepository;
        this.jpaApiClientRoleRepository = jpaApiClientRoleRepository;
        this.roleRepository = roleRepository;
        this.jpaApiKeyRepository = jpaApiKeyRepository;
    }

    @Transactional
    public ListClientsResponse listClients(UUID tenantId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.Direction.DESC, "createdAt");

        Page<ApiClientEntity> clientsPage = jpaApiClientRepository.findByTenantId(tenantId, pageable);
        if (clientsPage.getContent().isEmpty()) {
            return new ListClientsResponse(List.of(), safePage, safeSize, 0, 0);
        }

        Set<UUID> clientIds = clientsPage.getContent().stream().map(ApiClientEntity::getId).collect(Collectors.toSet());
        TenantEntity tenant = tenantRepository.findById(tenantId).orElse(null);

        Map<UUID, List<ApiClientRoleEntity>> rolesByClientId = jpaApiClientRoleRepository.findAllByApiClientIdIn(clientIds)
            .stream()
            .collect(Collectors.groupingBy(ApiClientRoleEntity::getApiClientId));

        Set<UUID> roleIds = rolesByClientId.values().stream()
            .flatMap(List::stream)
            .map(ApiClientRoleEntity::getRoleId)
            .collect(Collectors.toSet());
        Map<UUID, Role> rolesById = roleIds.isEmpty()
            ? Map.of()
            : roleRepository.findAllByIds(roleIds).stream().collect(Collectors.toMap(Role::getId, r -> r));

        Instant now = Instant.now();

        Map<UUID, Long> activeCountByClient = jpaApiKeyRepository.countActiveByApiClientIdIn(clientIds, now).stream()
            .collect(Collectors.toMap(ApiKeyActiveCount::getApiClientId, ApiKeyActiveCount::getTotal));

        List<ItemsResponse> items = clientsPage.getContent().stream()
            .map(client -> {
                List<String> roleCodes = rolesByClientId.getOrDefault(client.getId(), List.of()).stream()
                    .map(it -> rolesById.get(it.getRoleId()))
                    .filter(java.util.Objects::nonNull)
                    .map(r -> r.getCode().toString())
                    .toList();
                int activeKeys = activeCountByClient.getOrDefault(client.getId(), 0L).intValue();

                return new ItemsResponse(
                    client.getId(),
                    client.getTenantId(),
                    tenant != null ? tenant.getSlug() : "unknown",
                    client.getName(),
                    client.getDescription() != null ? client.getDescription() : "",
                    client.getStatus(),
                    roleCodes,
                    activeKeys,
                    client.getCreatedAt()
                );
            })
            .toList();

        return new ListClientsResponse(
            items,
            safePage,
            safeSize,
            clientsPage.getTotalElements(),
            clientsPage.getTotalPages()
        );
    }
}
