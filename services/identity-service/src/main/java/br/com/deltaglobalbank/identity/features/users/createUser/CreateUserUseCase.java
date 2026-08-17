package br.com.deltaglobalbank.identity.features.users.createUser;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.deltaglobalbank.identity.domain.module.Module;
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository;
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository;
import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleCode;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.user.DuplicateRoleException;
import br.com.deltaglobalbank.identity.domain.user.Email;
import br.com.deltaglobalbank.identity.domain.user.EmailAlreadyExistsException;
import br.com.deltaglobalbank.identity.domain.user.HashedPassword;
import br.com.deltaglobalbank.identity.domain.user.ModuleNotEnabledForTenantException;
import br.com.deltaglobalbank.identity.domain.user.Password;
import br.com.deltaglobalbank.identity.domain.user.RoleNotFoundException;
import br.com.deltaglobalbank.identity.domain.user.TenantInactiveException;
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserRoleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository;
import br.com.deltaglobalbank.identity.infrastructure.security.password.SpringPasswordHasher;
import br.com.deltaglobalbank.identity.infrastructure.security.password.TemporaryPasswordGenerator;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.transaction.Transactional;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;

@Service
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final TenantModuleRepository tenantModuleRepository;
    private final ModuleRepository moduleRepository;
    private final JpaUserRoleRepository userRoleRepository;
    private final SpringPasswordHasher passwordHasher;
    private final TemporaryPasswordGenerator temporaryPasswordGenerator;

    public CreateUserUseCase(
        UserRepository userRepository,
        TenantRepository tenantRepository,
        RoleRepository roleRepository,
        TenantModuleRepository tenantModuleRepository,
        ModuleRepository moduleRepository,
        JpaUserRoleRepository userRoleRepository,
        SpringPasswordHasher passwordHasher,
        TemporaryPasswordGenerator temporaryPasswordGenerator
    ) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.tenantModuleRepository = tenantModuleRepository;
        this.moduleRepository = moduleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordHasher = passwordHasher;
        this.temporaryPasswordGenerator = temporaryPasswordGenerator;
    }

    @Transactional
    public CreateUserResponse execute(CreateUserCommand command) {
        Tenant tenant = tenantRepository.findById(command.tenantId());
        if (tenant == null) {
            throw new TenantNotFoundException();
        }
        if (!tenant.isActive()) {
            throw new TenantInactiveException();
        }

        Email email = new Email(command.email());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();
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
                    throw new RoleNotFoundException(roleCode.toString());
                }
                return role;
            })
            .toList();

        validateRolesAgainstTenantModules(roles, command.tenantId());
        validateRoleBelowPlatformAdmin(roles, command.creatorRoles());

        String temporaryPassword = temporaryPasswordGenerator.generatePassword();
        HashedPassword hashedPassword = passwordHasher.hash(new Password(temporaryPassword));

        User user = User.newUser(
            UuidCreator.getTimeOrderedEpoch(), command.tenantId(), command.fullName(), email, hashedPassword);

        userRepository.save(user);

        Instant now = Instant.now();
        for (Role role : roles) {
            userRoleRepository.save(new UserRoleEntity(
                UuidCreator.getTimeOrderedEpoch(), user.getId(), role.getId(), now, command.grantedBy(), null));
        }

        return new CreateUserResponse(
            new CreatedUser(
                user.getId(),
                user.getFullName(),
                user.getEmail().value(),
                user.getTenantId(),
                roles.stream().map(Role::getCode).toList(),
                user.mustChangePassword(),
                user.getCreatedAt()
            ),
            new Password(temporaryPassword)
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
