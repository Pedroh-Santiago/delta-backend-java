package br.com.deltaglobalbank.identity.features.users.createUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.module.ModuleRepository;
import br.com.deltaglobalbank.identity.domain.module.TenantModule;
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository;
import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleCode;
import br.com.deltaglobalbank.identity.domain.role.RoleInactiveException;
import br.com.deltaglobalbank.identity.domain.role.RoleNotFoundException;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.user.DuplicateRoleException;
import br.com.deltaglobalbank.identity.domain.user.Email;
import br.com.deltaglobalbank.identity.domain.user.EmailAlreadyExistsException;
import br.com.deltaglobalbank.identity.domain.user.HashedPassword;
import br.com.deltaglobalbank.identity.domain.user.ModuleNotEnabledForTenantException;
import br.com.deltaglobalbank.identity.domain.user.Password;
import br.com.deltaglobalbank.identity.domain.user.TenantInactiveException;
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository;
import br.com.deltaglobalbank.identity.infrastructure.security.password.SpringPasswordHasher;
import br.com.deltaglobalbank.identity.infrastructure.security.password.TemporaryPasswordGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authorization.AuthorizationDeniedException;

class CreateUserTests {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final TenantRepository tenantRepository = mock(TenantRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final TenantModuleRepository tenantModuleRepository = mock(TenantModuleRepository.class);
    private final ModuleRepository moduleRepository = mock(ModuleRepository.class);
    private final JpaUserRoleRepository userRoleRepository = mock(JpaUserRoleRepository.class);
    private final SpringPasswordHasher passwordHasher = mock(SpringPasswordHasher.class);
    private final TemporaryPasswordGenerator temporaryPasswordGenerator = mock(TemporaryPasswordGenerator.class);

    private CreateUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateUserUseCase(
            userRepository, tenantRepository, roleRepository, tenantModuleRepository, moduleRepository,
            userRoleRepository, passwordHasher, temporaryPasswordGenerator
        );
    }

    @Test
    void mustThrowTenantNotFoundExceptionWhenTenantDoesNotExist() {
        UUID tenantId = UUID.randomUUID();
        CreateUserCommand command = new CreateUserCommand(
            tenantId, "New User", "admin@delta.com", List.of(new RoleCode("platform.admin")),
            UUID.randomUUID(), List.of("platform.admin"));

        when(tenantRepository.findById(tenantId)).thenReturn(null);

        assertThrows(TenantNotFoundException.class, () -> useCase.execute(command));

        verify(userRepository, times(0)).save(any());
        verify(userRoleRepository, times(0)).save(any());
    }

    @Test
    void mustThrowTenantInactiveExceptionWhenTenantIsInactive() {
        UUID tenantId = UUID.randomUUID();
        CreateUserCommand command = new CreateUserCommand(
            tenantId, "New User", "admin@delta.com", List.of(new RoleCode("platform.admin")),
            UUID.randomUUID(), List.of("platform.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(false);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        assertThrows(TenantInactiveException.class, () -> useCase.execute(command));

        verify(userRepository, times(0)).save(any());
        verify(userRoleRepository, times(0)).save(any());
    }

    @Test
    void mustThrowEmailAlreadyExistsExceptionWhenEmailAlreadyExists() {
        UUID tenantId = UUID.randomUUID();
        CreateUserCommand command = new CreateUserCommand(
            tenantId, "New User", "admin@delta.com", List.of(new RoleCode("platform.admin")),
            UUID.randomUUID(), List.of("platform.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        Email email = new Email("admin@delta.com");
        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> useCase.execute(command));

        verify(userRepository, times(0)).save(any());
        verify(userRoleRepository, times(0)).save(any());
    }

    @Test
    void mustThrowDuplicateRoleExceptionWhenRoleCodesListHasDuplicates() {
        UUID tenantId = UUID.randomUUID();
        RoleCode roleCode = new RoleCode("identity.admin");
        CreateUserCommand command = new CreateUserCommand(
            tenantId, "New User", "admin@delta.com", List.of(roleCode, roleCode),
            UUID.randomUUID(), List.of("platform.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        Email email = new Email("admin@delta.com");
        when(userRepository.existsByEmail(email)).thenReturn(false);

        assertThrows(DuplicateRoleException.class, () -> useCase.execute(command));

        verify(userRepository, times(0)).save(any());
        verify(userRoleRepository, times(0)).save(any());
    }

    @Test
    void mustThrowDuplicateRoleExceptionWhenRoleCodesDifferOnlyByCase() {
        UUID tenantId = UUID.randomUUID();
        CreateUserCommand command = new CreateUserCommand(
            tenantId, "New User", "admin@delta.com",
            List.of(new RoleCode("IDENTITY.ADMIN"), new RoleCode("identity.admin")),
            UUID.randomUUID(), List.of("platform.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        Email email = new Email("admin@delta.com");
        when(userRepository.existsByEmail(email)).thenReturn(false);

        assertThrows(DuplicateRoleException.class, () -> useCase.execute(command));

        verify(userRepository, times(0)).save(any());
        verify(userRoleRepository, times(0)).save(any());
    }

    @Test
    void mustThrowRoleNotFoundExceptionWhenRoleDoesNotExist() {
        UUID tenantId = UUID.randomUUID();
        RoleCode roleCode = new RoleCode("nonexistent.role");
        CreateUserCommand command = new CreateUserCommand(
            tenantId, "New User", "admin@delta.com", List.of(roleCode),
            UUID.randomUUID(), List.of("platform.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        Email email = new Email("admin@delta.com");
        when(userRepository.existsByEmail(email)).thenReturn(false);

        when(roleRepository.findByCode(roleCode)).thenReturn(null);

        assertThrows(RoleNotFoundException.class, () -> useCase.execute(command));

        verify(userRepository, times(0)).save(any());
        verify(userRoleRepository, times(0)).save(any());
    }

    @Test
    void mustThrowModuleNotEnabledForTenantExceptionWhenRoleModuleIsNotEnabled() {
        UUID tenantId = UUID.randomUUID();
        RoleCode roleCode = new RoleCode("identity.viewer");
        CreateUserCommand command = new CreateUserCommand(
            tenantId, "New User", "admin@delta.com", List.of(roleCode),
            UUID.randomUUID(), List.of("identity.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        Email email = new Email("admin@delta.com");
        when(userRepository.existsByEmail(email)).thenReturn(false);

        UUID moduleId = UUID.randomUUID();
        Role role = mock(Role.class);
        when(role.getCode()).thenReturn(roleCode);
        when(role.getModuleId()).thenReturn(moduleId);
        when(role.isActive()).thenReturn(true);
        when(roleRepository.findByCode(roleCode)).thenReturn(role);

        when(tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true)).thenReturn(List.of());
        when(moduleRepository.findById(any())).thenReturn(null);

        assertThrows(ModuleNotEnabledForTenantException.class, () -> useCase.execute(command));

        verify(userRepository, times(0)).save(any());
        verify(userRoleRepository, times(0)).save(any());
    }

    @Test
    void mustCreateUserWithNoRolesWhenListIsEmpty() {
        UUID tenantId = UUID.randomUUID();
        CreateUserCommand command = new CreateUserCommand(
            tenantId, "New User", "admin@delta.com", List.of(),
            UUID.randomUUID(), List.of("identity.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        Email email = new Email("admin@delta.com");
        when(userRepository.existsByEmail(email)).thenReturn(false);

        String rawPassword = "Password123!";
        Password password = new Password(rawPassword);
        when(temporaryPasswordGenerator.generatePassword()).thenReturn(rawPassword);
        when(passwordHasher.hash(password)).thenReturn(new HashedPassword("algumHashValido"));
        when(userRepository.save(any())).thenReturn(mock(User.class));

        CreateUserResponse response = useCase.execute(command);

        assertEquals("admin@delta.com", response.user().email());
        verify(userRepository, times(1)).save(any());
        verify(userRoleRepository, times(0)).save(any());
    }

    @Test
    void mustCreateUserSuccessfullyWithActiveStatusAndTemporaryPasswordRequiringChange() {
        UUID tenantId = UUID.randomUUID();
        RoleCode roleCode = new RoleCode("identity.admin");
        CreateUserCommand command = new CreateUserCommand(
            tenantId, "New User", "admin@delta.com", List.of(roleCode),
            UUID.randomUUID(), List.of("identity.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        Email email = new Email("admin@delta.com");
        when(userRepository.existsByEmail(email)).thenReturn(false);

        UUID moduleId = UUID.randomUUID();
        Role role = mock(Role.class);
        when(role.getCode()).thenReturn(roleCode);
        when(role.getModuleId()).thenReturn(moduleId);
        when(role.getId()).thenReturn(UUID.randomUUID());
        when(role.isActive()).thenReturn(true);
        when(roleRepository.findByCode(roleCode)).thenReturn(role);

        TenantModule tenantModule = mock(TenantModule.class);
        when(tenantModule.getModuleId()).thenReturn(moduleId);
        when(tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true)).thenReturn(List.of(tenantModule));

        String rawPassword = "Password123!";
        Password password = new Password(rawPassword);
        when(temporaryPasswordGenerator.generatePassword()).thenReturn(rawPassword);
        when(passwordHasher.hash(password)).thenReturn(new HashedPassword("algumHashValido"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenReturn(mock(User.class));

        CreateUserResponse response = useCase.execute(command);

        User saved = userCaptor.getValue();
        Assertions.assertAll(
            () -> assertEquals(true, response.user().mustChangePassword()),
            () -> assertEquals("admin@delta.com", response.user().email()),
            () -> assertEquals(tenantId, response.user().tenantId()),
            () -> assertNotNull(response.temporaryPassword()),
            () -> assertTrue(saved.isActive()),
            () -> assertEquals(new HashedPassword("algumHashValido"), saved.snapshot().passwordHash())
        );

        verify(userRepository, times(1)).save(any());
        verify(userRoleRepository, times(1)).save(any());
    }

    @Test
    void mustCreatePlatformAdminUserWhenCreatorIsPlatformAdmin() {
        UUID tenantId = UUID.randomUUID();
        RoleCode roleCode = new RoleCode("platform.admin");
        CreateUserCommand command = new CreateUserCommand(
            tenantId, "New User", "admin@delta.com", List.of(roleCode),
            UUID.randomUUID(), List.of("platform.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        Email email = new Email("admin@delta.com");
        when(userRepository.existsByEmail(email)).thenReturn(false);

        Role role = mock(Role.class);
        when(role.getCode()).thenReturn(roleCode);
        when(role.getModuleId()).thenReturn(null);
        when(role.getId()).thenReturn(UUID.randomUUID());
        when(role.isActive()).thenReturn(true);
        when(roleRepository.findByCode(roleCode)).thenReturn(role);

        String rawPassword = "Password123!";
        Password password = new Password(rawPassword);
        when(temporaryPasswordGenerator.generatePassword()).thenReturn(rawPassword);
        when(passwordHasher.hash(password)).thenReturn(new HashedPassword("algumHashValido"));

        when(userRepository.save(any())).thenReturn(mock(User.class));

        CreateUserResponse response = useCase.execute(command);

        assertEquals("admin@delta.com", response.user().email());
        assertEquals(tenantId, response.user().tenantId());
        verify(userRepository, times(1)).save(any());
        verify(userRoleRepository, times(1)).save(any());
    }

    @Test
    void mustNotCreatePlatformAdminUserWhenCreatorIsNotPlatformAdmin() {
        UUID tenantId = UUID.randomUUID();
        RoleCode roleCode = new RoleCode("platform.admin");
        CreateUserCommand command = new CreateUserCommand(
            tenantId, "New User", "admin@delta.com", List.of(roleCode),
            UUID.randomUUID(), List.of("identity.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        Email email = new Email("admin@delta.com");
        when(userRepository.existsByEmail(email)).thenReturn(false);

        Role role = mock(Role.class);
        when(role.getCode()).thenReturn(roleCode);
        when(role.getModuleId()).thenReturn(null);
        when(role.isActive()).thenReturn(true);
        when(roleRepository.findByCode(roleCode)).thenReturn(role);

        AuthorizationDeniedException ex = assertThrows(AuthorizationDeniedException.class, () -> useCase.execute(command));
        assertEquals("denied", ex.getMessage());

        verify(userRepository, times(0)).save(any());
        verify(userRoleRepository, times(0)).save(any());
    }

    @Test
    void mustCreateUserWhenCreatorIsIdentityAdmin() {
        UUID tenantId = UUID.randomUUID();
        RoleCode roleCode = new RoleCode("customers.viewer");
        CreateUserCommand command = new CreateUserCommand(
            tenantId, "New User", "customers.viewer@delta.com", List.of(roleCode),
            UUID.randomUUID(), List.of("identity.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        Email email = new Email("customers.viewer@delta.com");
        when(userRepository.existsByEmail(email)).thenReturn(false);

        UUID moduleId = UUID.randomUUID();
        Role role = mock(Role.class);
        when(role.getCode()).thenReturn(roleCode);
        when(role.getModuleId()).thenReturn(moduleId);
        when(role.getId()).thenReturn(UUID.randomUUID());
        when(role.isActive()).thenReturn(true);
        when(roleRepository.findByCode(roleCode)).thenReturn(role);

        TenantModule tenantModule = mock(TenantModule.class);
        when(tenantModule.getModuleId()).thenReturn(moduleId);
        when(tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true)).thenReturn(List.of(tenantModule));

        String rawPassword = "Password123!";
        Password password = new Password(rawPassword);
        when(temporaryPasswordGenerator.generatePassword()).thenReturn(rawPassword);
        when(passwordHasher.hash(password)).thenReturn(new HashedPassword("algumHashValido"));

        when(userRepository.save(any())).thenReturn(mock(User.class));

        CreateUserResponse response = useCase.execute(command);

        assertEquals("customers.viewer@delta.com", response.user().email());
        assertEquals(tenantId, response.user().tenantId());
        verify(userRepository, times(1)).save(any());
        verify(userRoleRepository, times(1)).save(any());
    }

    @Test
    void mustNotCreatePlatformAdminEvenWhenItIsNotFirstInTheRoleList() {
        UUID tenantId = UUID.randomUUID();
        RoleCode viewerCode = new RoleCode("customers.viewer");
        RoleCode adminCode = new RoleCode("platform.admin");
        CreateUserCommand command = new CreateUserCommand(
            tenantId, "New User", "admin@delta.com", List.of(viewerCode, adminCode),
            UUID.randomUUID(), List.of("identity.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        Email email = new Email("admin@delta.com");
        when(userRepository.existsByEmail(email)).thenReturn(false);

        Role viewerRole = mock(Role.class);
        when(viewerRole.getCode()).thenReturn(viewerCode);
        when(viewerRole.getModuleId()).thenReturn(null);
        when(viewerRole.isActive()).thenReturn(true);
        when(roleRepository.findByCode(viewerCode)).thenReturn(viewerRole);

        Role adminRole = mock(Role.class);
        when(adminRole.getCode()).thenReturn(adminCode);
        when(adminRole.getModuleId()).thenReturn(null);
        when(adminRole.isActive()).thenReturn(true);
        when(roleRepository.findByCode(adminCode)).thenReturn(adminRole);

        AuthorizationDeniedException ex = assertThrows(AuthorizationDeniedException.class, () -> useCase.execute(command));
        assertEquals("denied", ex.getMessage());

        verify(userRepository, times(0)).save(any());
        verify(userRoleRepository, times(0)).save(any());
    }

    @Test
    void mustSaveOneUserRoleEntityPerRoleWhenCreatingUserWithMultipleRoles() {
        UUID tenantId = UUID.randomUUID();
        RoleCode adminCode = new RoleCode("custumer.admin");
        RoleCode viewerCode = new RoleCode("custumer.viewer");
        CreateUserCommand command = new CreateUserCommand(
            tenantId, "New User", "costumer@delta.com", List.of(adminCode, viewerCode),
            UUID.randomUUID(), List.of("platform.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        Email email = new Email("costumer@delta.com");
        when(userRepository.existsByEmail(email)).thenReturn(false);

        UUID moduleId = UUID.randomUUID();
        Role adminRole = mock(Role.class);
        when(adminRole.getCode()).thenReturn(adminCode);
        when(adminRole.getModuleId()).thenReturn(moduleId);
        when(adminRole.getId()).thenReturn(UUID.randomUUID());
        when(adminRole.isActive()).thenReturn(true);
        when(roleRepository.findByCode(adminCode)).thenReturn(adminRole);

        Role viewerRole = mock(Role.class);
        when(viewerRole.getCode()).thenReturn(viewerCode);
        when(viewerRole.getModuleId()).thenReturn(moduleId);
        when(viewerRole.getId()).thenReturn(UUID.randomUUID());
        when(viewerRole.isActive()).thenReturn(true);
        when(roleRepository.findByCode(viewerCode)).thenReturn(viewerRole);

        TenantModule tenantModule = mock(TenantModule.class);
        when(tenantModule.getModuleId()).thenReturn(moduleId);
        when(tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true)).thenReturn(List.of(tenantModule));

        String rawPassword = "Password123!";
        Password password = new Password(rawPassword);
        when(temporaryPasswordGenerator.generatePassword()).thenReturn(rawPassword);
        when(passwordHasher.hash(password)).thenReturn(new HashedPassword("algumHashValido"));

        when(userRepository.save(any())).thenReturn(mock(User.class));

        CreateUserResponse response = useCase.execute(command);

        assertEquals("costumer@delta.com", response.user().email());
        verify(userRepository, times(1)).save(any());
        verify(userRoleRepository, times(2)).save(any());
    }

    @Test
    void mustCreateUserWithRolesFromDifferentEnabledModules() {
        UUID tenantId = UUID.randomUUID();
        RoleCode customersModuleCode = new RoleCode("customers.admin");
        RoleCode lendingModuleCode = new RoleCode("lending.operator");
        CreateUserCommand command = new CreateUserCommand(
            tenantId, "New User", "admin@delta.com", List.of(customersModuleCode, lendingModuleCode),
            UUID.randomUUID(), List.of("platform.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        Email email = new Email("admin@delta.com");
        when(userRepository.existsByEmail(email)).thenReturn(false);

        UUID customersModuleId = UUID.randomUUID();
        UUID lendingModuleId = UUID.randomUUID();
        Role customersRole = mock(Role.class);
        when(customersRole.getCode()).thenReturn(customersModuleCode);
        when(customersRole.getModuleId()).thenReturn(customersModuleId);
        when(customersRole.getId()).thenReturn(UUID.randomUUID());
        when(customersRole.isActive()).thenReturn(true);
        when(roleRepository.findByCode(customersModuleCode)).thenReturn(customersRole);

        Role lendingRole = mock(Role.class);
        when(lendingRole.getCode()).thenReturn(lendingModuleCode);
        when(lendingRole.getModuleId()).thenReturn(lendingModuleId);
        when(lendingRole.getId()).thenReturn(UUID.randomUUID());
        when(lendingRole.isActive()).thenReturn(true);
        when(roleRepository.findByCode(lendingModuleCode)).thenReturn(lendingRole);

        TenantModule customersModule = mock(TenantModule.class);
        when(customersModule.getModuleId()).thenReturn(customersModuleId);
        TenantModule lendingModule = mock(TenantModule.class);
        when(lendingModule.getModuleId()).thenReturn(lendingModuleId);
        when(tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true))
            .thenReturn(List.of(customersModule, lendingModule));

        String rawPassword = "Password123!";
        Password password = new Password(rawPassword);
        when(temporaryPasswordGenerator.generatePassword()).thenReturn(rawPassword);
        when(passwordHasher.hash(password)).thenReturn(new HashedPassword("algumHashValido"));
        when(userRepository.save(any())).thenReturn(mock(User.class));

        useCase.execute(command);

        verify(userRepository, times(1)).save(any());
        verify(userRoleRepository, times(2)).save(any());
    }

    @Test
    void mustThrowRoleInactiveExceptionWhenRoleIsDeactivated() {
        UUID tenantId = UUID.randomUUID();
        RoleCode roleCode = new RoleCode("internal-payroll.admin");
        CreateUserCommand command = new CreateUserCommand(
            tenantId, "New User", "admin@delta.com", List.of(roleCode),
            UUID.randomUUID(), List.of("platform.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        Email email = new Email("admin@delta.com");
        when(userRepository.existsByEmail(email)).thenReturn(false);

        Role role = mock(Role.class);
        when(role.isActive()).thenReturn(false);
        when(roleRepository.findByCode(roleCode)).thenReturn(role);

        assertThrows(RoleInactiveException.class, () -> useCase.execute(command));

        verify(userRepository, times(0)).save(any());
    }
}
