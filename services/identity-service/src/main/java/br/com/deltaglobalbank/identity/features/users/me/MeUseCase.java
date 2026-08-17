package br.com.deltaglobalbank.identity.features.users.me;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClient;
import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientRepository;
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository;
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import br.com.deltaglobalbank.identity.domain.user.UserSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MeUseCase {

    private final UserRepository userRepository;
    private final ApiClientRepository apiClientRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final TenantModuleRepository tenantModuleRepository;
    private final ModuleRepository moduleRepository;

    public MeUseCase(
        UserRepository userRepository,
        ApiClientRepository apiClientRepository,
        TenantRepository tenantRepository,
        RoleRepository roleRepository,
        TenantModuleRepository tenantModuleRepository,
        ModuleRepository moduleRepository
    ) {
        this.userRepository = userRepository;
        this.apiClientRepository = apiClientRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.tenantModuleRepository = tenantModuleRepository;
        this.moduleRepository = moduleRepository;
    }

    @Transactional(readOnly = true)
    public MeResponse execute(MeQuery query) {
        return switch (query.principalType()) {
            case "user" -> resolveUser(query.principalId());
            case "api_client" -> resolveApiClient(query.principalId());
            default -> throw new UnsupportedPrincipalTypeException(query.principalType());
        };
    }

    private MeResponse resolveUser(UUID userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new PrincipalNotFoundException();
        }

        Tenant tenant = tenantRepository.findById(user.getTenantId());
        if (tenant == null) {
            throw new PrincipalNotFoundException();
        }

        RolesAndModules resolved = resolveRolesAndModulesForUser(user, tenant);
        UserSnapshot snapshot = user.snapshot();

        return new MeResponse(
            user.getId(),
            "user",
            user.getFullName(),
            tenant.getId(),
            tenant.getSlug(),
            tenant.getName(),
            snapshot.status().toDatabaseValue(),
            resolved.roles(),
            resolved.modules(),
            user.getCreatedAt(),
            user.getEmail().value(),
            user.mustChangePassword(),
            snapshot.lastLoginAt(),
            null,
            null
        );
    }

    private MeResponse resolveApiClient(UUID apiClientId) {
        ApiClient apiClient = apiClientRepository.findById(apiClientId);
        if (apiClient == null) {
            throw new PrincipalNotFoundException();
        }

        Tenant tenant = tenantRepository.findById(apiClient.getTenantId());
        if (tenant == null) {
            throw new PrincipalNotFoundException();
        }

        RolesAndModules resolved = resolveRolesAndModulesForApiClient(apiClient, tenant);

        return new MeResponse(
            apiClient.getId(),
            "api_client",
            null,
            tenant.getId(),
            tenant.getSlug(),
            tenant.getName(),
            apiClient.statusAsString(),
            resolved.roles(),
            resolved.modules(),
            apiClient.getCreatedAt(),
            null,
            null,
            null,
            apiClient.getName(),
            apiClient.getDescription()
        );
    }

    private record RolesAndModules(List<String> roles, List<String> modules) {
    }

    private RolesAndModules resolveRolesAndModulesForUser(User user, Tenant tenant) {
        Set<UUID> enabledModuleIds = tenantModuleRepository.findAllByTenantIdAndEnabled(tenant.getId(), true).stream()
            .map(it -> it.getModuleId())
            .collect(Collectors.toSet());

        List<String> modules = enabledModuleIds.isEmpty()
            ? List.of()
            : moduleRepository.findAllByIds(enabledModuleIds).stream().map(it -> it.getCode().value()).toList();

        List<String> roles = roleRepository.findAllByUserId(user.getId()).stream()
            .filter(it -> it.getModuleId() == null || enabledModuleIds.contains(it.getModuleId()))
            .map(it -> it.getCode().value())
            .toList();

        return new RolesAndModules(roles, modules);
    }

    private RolesAndModules resolveRolesAndModulesForApiClient(ApiClient apiClient, Tenant tenant) {
        Set<UUID> enabledModuleIds = tenantModuleRepository.findAllByTenantIdAndEnabled(tenant.getId(), true).stream()
            .map(it -> it.getModuleId())
            .collect(Collectors.toSet());

        List<String> modules = enabledModuleIds.isEmpty()
            ? List.of()
            : moduleRepository.findAllByIds(enabledModuleIds).stream().map(it -> it.getCode().value()).toList();

        List<String> roles = roleRepository.findAllByApiClientId(apiClient.getId()).stream()
            .filter(it -> it.getModuleId() == null || enabledModuleIds.contains(it.getModuleId()))
            .map(it -> it.getCode().value())
            .toList();

        return new RolesAndModules(roles, modules);
    }
}
