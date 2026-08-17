package br.com.deltaglobalbank.identity.features.apiClients.createApiClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClient;
import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientRepository;
import br.com.deltaglobalbank.identity.domain.module.Module;
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository;
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository;
import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleCode;
import br.com.deltaglobalbank.identity.domain.role.RoleInactiveException;
import br.com.deltaglobalbank.identity.domain.role.RoleNotFoundException;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.user.DuplicateRoleException;
import br.com.deltaglobalbank.identity.domain.user.ModuleNotEnabledForTenantException;
import br.com.deltaglobalbank.identity.domain.user.TenantInactiveException;
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiClientRoleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiClientRoleRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.transaction.Transactional;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;

@Service
public class CreateApiClientUseCase {

    private final ApiClientRepository apiClientRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final TenantModuleRepository tenantModuleRepository;
    private final ModuleRepository moduleRepository;
    private final JpaApiClientRoleRepository apiClientRoleRepository;

    public CreateApiClientUseCase(
        ApiClientRepository apiClientRepository,
        TenantRepository tenantRepository,
        RoleRepository roleRepository,
        TenantModuleRepository tenantModuleRepository,
        ModuleRepository moduleRepository,
        JpaApiClientRoleRepository apiClientRoleRepository
    ) {
        this.apiClientRepository = apiClientRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.tenantModuleRepository = tenantModuleRepository;
        this.moduleRepository = moduleRepository;
        this.apiClientRoleRepository = apiClientRoleRepository;
    }

    @Transactional
    public CreateApiClientResponse execute(CreateApiClientCommand command) {
        Tenant tenant = tenantRepository.findById(command.tenantId());
        if (tenant == null) {
            throw new TenantNotFoundException();
        }
        if (!tenant.isActive()) {
            throw new TenantInactiveException();
        }

        List<RoleCode> roleCodesList = command.roleCodes().stream()
            .map(it -> new RoleCode(it.toString().toLowerCase(Locale.ROOT)))
            .toList();
        if (roleCodesList.size() != Set.copyOf(roleCodesList).size()) {
            throw new DuplicateRoleException();
        }

        List<Role> roles = roleCodesList.stream()
            .map(roleCode -> {
                Role role = roleRepository.findByCode(roleCode);
                if (role == null) {
                    throw new RoleNotFoundException();
                }
                if (!role.isActive()) {
                    throw new RoleInactiveException();
                }
                return role;
            })
            .toList();

        validateRolesAgainstTenantModules(roles, command.tenantId());
        validateRoleBelowPlatformAdmin(roles, command.creatorRoles());

        ApiClient apiClient = ApiClient.newApiClient(
            UuidCreator.getTimeOrderedEpoch(), command.tenantId(), command.name(), command.description());

        apiClientRepository.save(apiClient);

        Instant now = Instant.now();
        for (Role role : roles) {
            apiClientRoleRepository.save(new ApiClientRoleEntity(
                UuidCreator.getTimeOrderedEpoch(), apiClient.getId(), role.getId(), now, null));
        }

        return new CreateApiClientResponse(
            apiClient.getId(),
            apiClient.getTenantId(),
            apiClient.getName(),
            apiClient.getDescription(),
            apiClient.snapshot().status().toDatabaseValue(),
            roles.stream().map(Role::getCode).toList(),
            apiClient.getCreatedAt()
        );
    }

    private void validateRoleBelowPlatformAdmin(List<Role> roles, List<String> creatorRoles) {
        if (creatorRoles.contains("platform.admin")) {
            return;
        }
        for (Role role : roles) {
            if (role.getCode().toString().equals("platform.admin")) {
                throw new AuthorizationDeniedException("denied");
            }
        }
    }

    private void validateRolesAgainstTenantModules(List<Role> roles, UUID tenantId) {
        List<Role> rolesNeedingModule = roles.stream().filter(it -> it.getModuleId() != null).toList();
        if (rolesNeedingModule.isEmpty()) {
            return;
        }

        Set<UUID> enabledModuleIds = tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true).stream()
            .map(it -> it.getModuleId())
            .collect(Collectors.toSet());

        for (Role role : rolesNeedingModule) {
            if (!enabledModuleIds.contains(role.getModuleId())) {
                Module module = moduleRepository.findById(role.getModuleId());
                throw new ModuleNotEnabledForTenantException(module != null ? module.getCode().toString() : "unknown");
            }
        }
    }
}
