package br.com.deltaglobalbank.identity.features.auth.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.ipAllowlist.Cidr;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.IpNotAllowedException;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlist;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlistRepository;
import br.com.deltaglobalbank.identity.domain.module.Module;
import br.com.deltaglobalbank.identity.domain.module.ModuleCode;
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository;
import br.com.deltaglobalbank.identity.domain.module.TenantModule;
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository;
import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleCode;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import br.com.deltaglobalbank.identity.domain.shared.PasswordHasher;
import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.token.IssuedTokenAuditRepository;
import br.com.deltaglobalbank.identity.domain.token.RefreshToken;
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRepository;
import br.com.deltaglobalbank.identity.domain.user.Email;
import br.com.deltaglobalbank.identity.domain.user.InvalidCredentialsException;
import br.com.deltaglobalbank.identity.domain.user.LockoutPolicy;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import br.com.deltaglobalbank.identity.infrastructure.security.ipAllowlist.TenantIpAllowlistCache;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.IssuedJwt;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.JwtIssuer;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.JwtIssuerProperties;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.UserClaims;
import br.com.deltaglobalbank.identity.infrastructure.security.lockout.LockoutProperties;
import br.com.deltaglobalbank.identity.infrastructure.security.token.GeneratedRefreshToken;
import br.com.deltaglobalbank.identity.infrastructure.security.token.RefreshTokenGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class LoginUseCaseTests {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final TenantRepository tenantRepository = mock(TenantRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final TenantModuleRepository tenantModuleRepository = mock(TenantModuleRepository.class);
    private final ModuleRepository moduleRepository = mock(ModuleRepository.class);
    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final IssuedTokenAuditRepository issuedTokenAuditRepository = mock(IssuedTokenAuditRepository.class);
    private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
    private final RefreshTokenGenerator refreshTokenGenerator = mock(RefreshTokenGenerator.class);
    private final JwtIssuer jwtIssuer = mock(JwtIssuer.class);
    private final JwtIssuerProperties jwtProperties = mock(JwtIssuerProperties.class);
    private final TenantIpAllowlistRepository tenantIpAllowlistRepository = mock(TenantIpAllowlistRepository.class);
    private final TenantIpAllowlistCache tenantIpAllowlistCache = mock(TenantIpAllowlistCache.class);
    private final LockoutProperties lockoutProperties = mock(LockoutProperties.class);
    private final FailedLoginRecorder failedLoginRecorder = mock(FailedLoginRecorder.class);

    private LoginUseCase useCase;
    private LoginRequest request;
    private LoginContext context;
    private UUID userId;
    private UUID tenantId;
    private UUID enabledModuleId;
    private UUID disabledModuleId;
    private UUID moduleId;

    @BeforeEach
    void setUp() {
        useCase = new LoginUseCase(
            userRepository, tenantRepository, roleRepository, tenantModuleRepository, moduleRepository,
            refreshTokenRepository, issuedTokenAuditRepository, passwordHasher, refreshTokenGenerator,
            jwtIssuer, jwtProperties, tenantIpAllowlistRepository, tenantIpAllowlistCache,
            lockoutProperties, failedLoginRecorder
        );
        request = new LoginRequest("valid.email@delta.com", "Password123");
        context = new LoginContext("192.168.0.1", "test-agent");
        userId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        enabledModuleId = UUID.randomUUID();
        disabledModuleId = UUID.randomUUID();
        moduleId = UUID.randomUUID();
        lenient().when(lockoutProperties.toPolicy()).thenReturn(new LockoutPolicy(
            true, List.of(5, 3, 2), List.of(Duration.ofMinutes(5), Duration.ofMinutes(15))
        ));
    }

    @Test
    void mustThrowInvalidCredentialsExceptionWhenEmailIsInvalid() {
        request = new LoginRequest("invalid.email_delta.com", "Password123");
        assertThrows(InvalidCredentialsException.class, () -> useCase.execute(request, context));

        verify(userRepository, never()).save(any());
        verify(jwtIssuer, never()).issueForUser(any());
    }

    @Test
    void mustThrowInvalidCredentialsExceptionWhenUserIsNotFound() {
        when(userRepository.findByEmail(new Email("valid.email@delta.com"))).thenReturn(null);

        assertThrows(InvalidCredentialsException.class, () -> useCase.execute(request, context));

        verify(userRepository, never()).save(any());
        verify(jwtIssuer, never()).issueForUser(any());
    }

    @Test
    void mustThrowInvalidCredentialsExceptionWhenTenantIsNotFound() {
        User user = mock(User.class);
        when(user.getTenantId()).thenReturn(UUID.randomUUID());
        when(userRepository.findByEmail(new Email("valid.email@delta.com"))).thenReturn(user);

        when(tenantRepository.findById(any())).thenReturn(null);

        assertThrows(InvalidCredentialsException.class, () -> useCase.execute(request, context));

        verify(userRepository, never()).save(any());
        verify(jwtIssuer, never()).issueForUser(any());
    }

    @Test
    void mustThrowInvalidCredentialsExceptionWhenAuthenticationFails() {
        User user = mock(User.class);
        when(user.getTenantId()).thenReturn(UUID.randomUUID());
        when(userRepository.findByEmail(new Email("valid.email@delta.com"))).thenReturn(user);

        Tenant tenant = mock(Tenant.class);
        when(tenantRepository.findById(any())).thenReturn(tenant);
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(roleRepository.findAllByUserId(any())).thenReturn(List.of());
        lenient().when(tenant.isActive()).thenReturn(true);

        org.mockito.Mockito.doThrow(new InvalidCredentialsException()).when(user).authenticate(any(), any(), any(), any());

        lenient().when(tenantIpAllowlistRepository.findAllByTenantId(any())).thenReturn(List.of());
        lenient().when(tenantIpAllowlistCache.get(any())).thenReturn(null);

        assertThrows(InvalidCredentialsException.class, () -> useCase.execute(request, context));

        verify(userRepository, never()).save(any());
        verify(jwtIssuer, never()).issueForUser(any());
    }

    @Test
    void mustIncludeOnlyRolesFromEnabledModulesInJwtClaims() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getTenantId()).thenReturn(tenantId);
        when(user.mustChangePassword()).thenReturn(false);
        when(userRepository.findByEmail(new Email("valid.email@delta.com"))).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        Tenant tenant = mock(Tenant.class);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);
        when(tenant.isActive()).thenReturn(true);

        TenantModule enabledModule = mock(TenantModule.class);
        when(enabledModule.getModuleId()).thenReturn(enabledModuleId);
        when(tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true)).thenReturn(List.of(enabledModule));
        when(moduleRepository.findAllByIds(any())).thenReturn(List.of());

        when(tenantIpAllowlistRepository.findAllByTenantId(any())).thenReturn(List.of());
        when(tenantIpAllowlistCache.get(any())).thenReturn(null);

        Role roleEnabled = mock(Role.class);
        when(roleEnabled.getCode()).thenReturn(new RoleCode("customers.admin"));
        when(roleEnabled.getModuleId()).thenReturn(enabledModuleId);
        when(roleEnabled.isActive()).thenReturn(true);
        Role roleDisabled = mock(Role.class);
        when(roleDisabled.getCode()).thenReturn(new RoleCode("lending.admin"));
        when(roleDisabled.getModuleId()).thenReturn(disabledModuleId);
        when(roleDisabled.isActive()).thenReturn(true);
        Role roleGlobal = mock(Role.class);
        when(roleGlobal.getCode()).thenReturn(new RoleCode("platform.admin"));
        when(roleGlobal.getModuleId()).thenReturn(null);
        when(roleGlobal.isActive()).thenReturn(true);
        when(roleRepository.findAllByUserId(userId)).thenReturn(List.of(roleEnabled, roleDisabled, roleGlobal));

        ArgumentCaptor<UserClaims> claimsCaptor = ArgumentCaptor.forClass(UserClaims.class);
        IssuedJwt issuedJwt = new IssuedJwt("JWT", UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(900));
        when(jwtIssuer.issueForUser(claimsCaptor.capture())).thenReturn(issuedJwt);

        when(refreshTokenGenerator.generate()).thenReturn(new GeneratedRefreshToken("PLAIN", "HASH"));
        when(refreshTokenRepository.save(any())).thenReturn(mock(RefreshToken.class));
        when(issuedTokenAuditRepository.findByJti(any())).thenReturn(null);
        when(jwtProperties.accessTokenTtl()).thenReturn(Duration.ofSeconds(900));
        when(jwtProperties.refreshTokenTtl()).thenReturn(Duration.ofDays(7));

        useCase.execute(request, context);

        List<String> roles = claimsCaptor.getValue().roles();
        Assertions.assertAll(
            () -> assertTrue(roles.contains("customers.admin")),
            () -> assertTrue(roles.contains("platform.admin")),
            () -> assertFalse(roles.contains("lending.admin"))
        );
    }

    @Test
    void mustPropagateMustChangePasswordTrueIntoJwtClaims() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getTenantId()).thenReturn(tenantId);
        when(user.mustChangePassword()).thenReturn(true);
        when(userRepository.findByEmail(new Email("valid.email@delta.com"))).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        Tenant tenant = mock(Tenant.class);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);
        when(tenant.isActive()).thenReturn(true);

        when(tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true)).thenReturn(List.of());
        when(roleRepository.findAllByUserId(userId)).thenReturn(List.of());

        ArgumentCaptor<UserClaims> claimsCaptor = ArgumentCaptor.forClass(UserClaims.class);
        IssuedJwt issuedJwt = new IssuedJwt("JWT", UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(900));
        when(jwtIssuer.issueForUser(claimsCaptor.capture())).thenReturn(issuedJwt);

        when(refreshTokenGenerator.generate()).thenReturn(new GeneratedRefreshToken("PLAIN", "HASH"));
        when(refreshTokenRepository.save(any())).thenReturn(mock(RefreshToken.class));
        when(jwtProperties.accessTokenTtl()).thenReturn(Duration.ofSeconds(900));
        when(jwtProperties.refreshTokenTtl()).thenReturn(Duration.ofDays(7));

        when(tenantIpAllowlistRepository.findAllByTenantId(any())).thenReturn(List.of());
        when(tenantIpAllowlistCache.get(any())).thenReturn(null);

        LoginResult response = useCase.execute(request, context);

        Assertions.assertAll(
            () -> assertEquals(true, claimsCaptor.getValue().mustChangePassword()),
            () -> assertEquals(true, response.mustChangePassword())
        );
    }

    @Test
    void mustBuildJwtClaimsWithUserIdentityAndEnabledModules() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getTenantId()).thenReturn(tenantId);
        when(user.mustChangePassword()).thenReturn(false);
        when(userRepository.findByEmail(new Email("valid.email@delta.com"))).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        Tenant tenant = mock(Tenant.class);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);
        when(tenant.isActive()).thenReturn(true);

        TenantModule tenantModule = mock(TenantModule.class);
        when(tenantModule.getModuleId()).thenReturn(moduleId);
        when(tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true)).thenReturn(List.of(tenantModule));

        Module module = mock(Module.class);
        when(module.getCode()).thenReturn(new ModuleCode("customers"));
        when(moduleRepository.findAllByIds(any())).thenReturn(List.of(module));

        when(roleRepository.findAllByUserId(userId)).thenReturn(List.of());

        ArgumentCaptor<UserClaims> claimsCaptor = ArgumentCaptor.forClass(UserClaims.class);
        IssuedJwt issuedJwt = new IssuedJwt("JWT", UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(900));
        when(jwtIssuer.issueForUser(claimsCaptor.capture())).thenReturn(issuedJwt);

        when(refreshTokenGenerator.generate()).thenReturn(new GeneratedRefreshToken("PLAIN", "HASH"));
        when(refreshTokenRepository.save(any())).thenReturn(mock(RefreshToken.class));
        when(jwtProperties.accessTokenTtl()).thenReturn(Duration.ofSeconds(900));
        when(jwtProperties.refreshTokenTtl()).thenReturn(Duration.ofDays(7));

        when(tenantIpAllowlistRepository.findAllByTenantId(any())).thenReturn(List.of());
        when(tenantIpAllowlistCache.get(any())).thenReturn(null);

        useCase.execute(request, context);

        UserClaims claims = claimsCaptor.getValue();
        Assertions.assertAll(
            () -> assertEquals(userId, claims.userId()),
            () -> assertEquals(tenantId, claims.tenantId()),
            () -> assertEquals(List.of("customers"), claims.modules()),
            () -> assertEquals(false, claims.mustChangePassword())
        );
    }

    @Test
    void mustIssueAccessAndRefreshTokensOnSuccessfulLogin() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getTenantId()).thenReturn(tenantId);
        when(user.mustChangePassword()).thenReturn(false);
        when(userRepository.findByEmail(new Email("valid.email@delta.com"))).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        Tenant tenant = mock(Tenant.class);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);
        when(tenant.isActive()).thenReturn(true);

        when(tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true)).thenReturn(List.of());
        when(roleRepository.findAllByUserId(userId)).thenReturn(List.of());

        IssuedJwt issuedJwt = new IssuedJwt("JWT-ACCESS-TOKEN", UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(900));
        ArgumentCaptor<UserClaims> claimsCaptor = ArgumentCaptor.forClass(UserClaims.class);
        when(jwtIssuer.issueForUser(claimsCaptor.capture())).thenReturn(issuedJwt);

        GeneratedRefreshToken generated = new GeneratedRefreshToken("PLAIN-TOKEN", "HASH-INTERNO");
        when(refreshTokenGenerator.generate()).thenReturn(generated);

        ArgumentCaptor<RefreshToken> refreshCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        when(refreshTokenRepository.save(refreshCaptor.capture())).thenReturn(mock(RefreshToken.class));

        when(jwtProperties.accessTokenTtl()).thenReturn(Duration.ofSeconds(900));
        when(jwtProperties.refreshTokenTtl()).thenReturn(Duration.ofDays(7));

        when(tenantIpAllowlistRepository.findAllByTenantId(any())).thenReturn(List.of());
        when(tenantIpAllowlistCache.get(any())).thenReturn(null);

        LoginResult response = useCase.execute(request, context);

        Assertions.assertAll(
            () -> assertEquals("JWT-ACCESS-TOKEN", response.accessToken()),
            () -> assertEquals("PLAIN-TOKEN", response.refreshToken()),
            () -> assertEquals("Bearer", response.tokenType()),
            () -> assertEquals(900, response.expiresIn()),
            () -> assertEquals(false, response.mustChangePassword())
        );

        Assertions.assertAll(
            () -> assertEquals(userId, claimsCaptor.getValue().userId()),
            () -> assertEquals(tenantId, claimsCaptor.getValue().tenantId()),
            () -> assertEquals(false, claimsCaptor.getValue().mustChangePassword())
        );

        Assertions.assertAll(
            () -> assertEquals("HASH-INTERNO", refreshCaptor.getValue().getTokenHash()),
            () -> assertEquals("192.168.0.1", refreshCaptor.getValue().getIpAddress()),
            () -> assertEquals("test-agent", refreshCaptor.getValue().getUserAgent())
        );

        verify(jwtIssuer, times(1)).issueForUser(any());
    }

    @Test
    void mustAllowLoginWhenIpMatchesAnAllowlistEntry() {
        User user = mock(User.class);
        Tenant tenant = mock(Tenant.class);

        when(user.getId()).thenReturn(userId);
        when(user.getTenantId()).thenReturn(tenantId);
        when(user.mustChangePassword()).thenReturn(false);
        when(userRepository.findByEmail(new Email("valid.email@delta.com"))).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);
        when(tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true)).thenReturn(List.of());
        when(roleRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(jwtIssuer.issueForUser(any())).thenReturn(new IssuedJwt("JWT", UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(900)));
        when(refreshTokenGenerator.generate()).thenReturn(new GeneratedRefreshToken("PLAIN", "HASH"));
        when(refreshTokenRepository.save(any())).thenReturn(mock(RefreshToken.class));
        when(jwtProperties.accessTokenTtl()).thenReturn(Duration.ofSeconds(900));
        when(jwtProperties.refreshTokenTtl()).thenReturn(Duration.ofDays(7));

        TenantIpAllowlist entry = mock(TenantIpAllowlist.class);
        when(entry.getCidr()).thenReturn(new Cidr("192.168.0.0/24"));
        when(tenantIpAllowlistRepository.findAllByTenantId(tenantId)).thenReturn(List.of(entry));
        when(tenantIpAllowlistCache.get(any())).thenReturn(null);

        LoginResult response = useCase.execute(request, context);
        assertEquals("JWT", response.accessToken());
        verify(jwtIssuer, times(1)).issueForUser(any());
    }

    @Test
    void mustThrowIpNotAllowedExceptionWhenIpIsNotInTheAllowlist() {
        User user = mock(User.class);
        lenient().when(user.getTenantId()).thenReturn(tenantId);
        when(userRepository.findByEmail(new Email("valid.email@delta.com"))).thenReturn(user);
        when(tenantRepository.findById(tenantId)).thenReturn(mock(Tenant.class));

        TenantIpAllowlist entry = mock(TenantIpAllowlist.class);
        when(entry.getCidr()).thenReturn(new Cidr("10.0.0.0/8"));
        when(tenantIpAllowlistRepository.findAllByTenantId(tenantId)).thenReturn(List.of(entry));
        when(tenantIpAllowlistCache.get(any())).thenReturn(null);

        assertThrows(IpNotAllowedException.class, () -> useCase.execute(request, context));

        verify(user, never()).authenticate(any(), any(), any(), any());
        verify(jwtIssuer, never()).issueForUser(any());
    }

    @Test
    void mustThrowIpNotAllowedExceptionWhenIpIsNullAndAllowlistExists() {
        LoginContext contextWithoutIp = new LoginContext(null, "test-agent");

        User user = mock(User.class);
        lenient().when(user.getTenantId()).thenReturn(tenantId);
        when(userRepository.findByEmail(new Email("valid.email@delta.com"))).thenReturn(user);
        when(tenantRepository.findById(tenantId)).thenReturn(mock(Tenant.class));

        TenantIpAllowlist entry = mock(TenantIpAllowlist.class);
        when(entry.getCidr()).thenReturn(new Cidr("192.168.0.0/24"));
        when(tenantIpAllowlistRepository.findAllByTenantId(tenantId)).thenReturn(List.of(entry));
        when(tenantIpAllowlistCache.get(any())).thenReturn(null);

        assertThrows(IpNotAllowedException.class, () -> useCase.execute(request, contextWithoutIp));

        verify(user, never()).authenticate(any(), any(), any(), any());
    }

    @Test
    void mustUseCachedAllowlistWithoutQueryingRepositoryOnCacheHit() {
        User user = mock(User.class);
        Tenant tenant = mock(Tenant.class);

        when(user.getId()).thenReturn(userId);
        when(user.getTenantId()).thenReturn(tenantId);
        when(user.mustChangePassword()).thenReturn(false);

        when(userRepository.findByEmail(new Email("valid.email@delta.com"))).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);
        when(tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true)).thenReturn(List.of());

        when(roleRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(jwtIssuer.issueForUser(any())).thenReturn(new IssuedJwt("JWT", UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(900)));

        when(refreshTokenGenerator.generate()).thenReturn(new GeneratedRefreshToken("PLAIN", "HASH"));
        when(refreshTokenRepository.save(any())).thenReturn(mock(RefreshToken.class));
        when(jwtProperties.accessTokenTtl()).thenReturn(Duration.ofSeconds(900));
        when(jwtProperties.refreshTokenTtl()).thenReturn(Duration.ofDays(7));

        when(tenantIpAllowlistCache.get(tenantId)).thenReturn(List.of("192.168.0.0/24"));

        useCase.execute(request, context);

        verify(tenantIpAllowlistRepository, never()).findAllByTenantId(any());
        verify(tenantIpAllowlistCache, never()).set(any(), any());
    }

    @Test
    void mustPopulateCacheAfterFetchingAllowlistFromRepositoryOnCacheMiss() {
        User user = mock(User.class);
        Tenant tenant = mock(Tenant.class);

        when(user.getId()).thenReturn(userId);
        when(user.getTenantId()).thenReturn(tenantId);
        when(user.mustChangePassword()).thenReturn(false);

        when(userRepository.findByEmail(new Email("valid.email@delta.com"))).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);
        when(tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true)).thenReturn(List.of());

        when(roleRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(jwtIssuer.issueForUser(any())).thenReturn(new IssuedJwt("JWT", UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(900)));

        when(refreshTokenGenerator.generate()).thenReturn(new GeneratedRefreshToken("PLAIN", "HASH"));
        when(refreshTokenRepository.save(any())).thenReturn(mock(RefreshToken.class));
        when(jwtProperties.accessTokenTtl()).thenReturn(Duration.ofSeconds(900));
        when(jwtProperties.refreshTokenTtl()).thenReturn(Duration.ofDays(7));

        TenantIpAllowlist entry = mock(TenantIpAllowlist.class);
        when(entry.getCidr()).thenReturn(new Cidr("192.168.0.0/24"));
        when(tenantIpAllowlistCache.get(tenantId)).thenReturn(null);
        when(tenantIpAllowlistRepository.findAllByTenantId(tenantId)).thenReturn(List.of(entry));

        useCase.execute(request, context);

        verify(tenantIpAllowlistRepository, times(1)).findAllByTenantId(tenantId);
        verify(tenantIpAllowlistCache, times(1)).set(tenantId, List.of("192.168.0.0/24"));
    }
}
