package br.com.deltaglobalbank.identity.features.auth.refresh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.token.InvalidRefreshTokenException;
import br.com.deltaglobalbank.identity.domain.token.RefreshToken;
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRepository;
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenReuseDetectedException;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import br.com.deltaglobalbank.identity.domain.user.UserStatus;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.RoleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantModuleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserRoleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaIssuedTokenAuditRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaModuleRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaRoleRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantModuleRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.IssuedJwt;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.JwtIssuer;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.JwtIssuerProperties;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.UserClaims;
import br.com.deltaglobalbank.identity.infrastructure.security.token.GeneratedRefreshToken;
import br.com.deltaglobalbank.identity.infrastructure.security.token.RefreshTokenGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

class RefreshTokenTests {

    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final TenantRepository tenantRepository = mock(TenantRepository.class);
    private final JpaUserRoleRepository userRoleRepository = mock(JpaUserRoleRepository.class);
    private final JpaRoleRepository roleRepository = mock(JpaRoleRepository.class);
    private final JpaTenantModuleRepository tenantModuleRepository = mock(JpaTenantModuleRepository.class);
    private final JpaModuleRepository moduleRepository = mock(JpaModuleRepository.class);
    private final JpaIssuedTokenAuditRepository issuedTokenAuditRepository = mock(JpaIssuedTokenAuditRepository.class);
    private final RefreshTokenGenerator refreshTokenGenerator = mock(RefreshTokenGenerator.class);
    private final JwtIssuer jwtIssuer = mock(JwtIssuer.class);
    private final JwtIssuerProperties jwtProperties = mock(JwtIssuerProperties.class);

    private RefreshTokenUseCase useCase;
    private RefreshTokenContext context;
    private UUID userId;
    private UUID tenantId;
    private Instant expiresAt;
    private UUID enabledModuleId;
    private UUID disabledModuleId;

    @BeforeEach
    void setUp() {
        useCase = new RefreshTokenUseCase(
            refreshTokenRepository, userRepository, tenantRepository, userRoleRepository, roleRepository,
            tenantModuleRepository, moduleRepository, issuedTokenAuditRepository, refreshTokenGenerator,
            jwtIssuer, jwtProperties
        );
        userId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        expiresAt = Instant.now().plusSeconds(3600);
        context = new RefreshTokenContext("192.168.0.1", "test-agent");
        lenient().when(refreshTokenGenerator.hash(any())).thenReturn("algum-hash");
        enabledModuleId = UUID.randomUUID();
        disabledModuleId = UUID.randomUUID();
    }

    @Test
    void mustThrowInvalidRefreshTokenExceptionWhenTokenIsNotFound() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(null);

        assertThrows(InvalidRefreshTokenException.class, () -> useCase.execute("raw-token", context));

        verify(refreshTokenRepository, times(0)).save(any());
        verify(jwtIssuer, times(0)).issueForUser(any());
    }

    @Test
    void mustRevokeAllActiveTokensAndThrowWhenReuseIsDetected() {
        RefreshToken existing = mock(RefreshToken.class);
        when(existing.isRevoked()).thenReturn(true);
        when(existing.getUserId()).thenReturn(userId);
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(existing);

        RefreshToken active1 = mock(RefreshToken.class);
        RefreshToken active2 = mock(RefreshToken.class);
        when(refreshTokenRepository.findAllActiveByUserId(userId)).thenReturn(List.of(active1, active2));
        when(refreshTokenRepository.saveAll(any())).thenReturn(List.of(active1, active2));

        assertThrows(RefreshTokenReuseDetectedException.class, () -> useCase.execute("raw-token", context));

        verify(active1, times(1)).revoke();
        verify(active2, times(1)).revoke();
        verify(refreshTokenRepository, times(1)).saveAll(any());

        verify(jwtIssuer, times(0)).issueForUser(any());
        verify(issuedTokenAuditRepository, times(0)).save(any());
    }

    @Test
    void mustThrowReuseExceptionWithoutSavingWhenNoActiveTokensExist() {
        RefreshToken existing = mock(RefreshToken.class);
        when(existing.isRevoked()).thenReturn(true);
        when(existing.getUserId()).thenReturn(userId);
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(existing);
        when(refreshTokenRepository.findAllActiveByUserId(userId)).thenReturn(List.of());

        assertThrows(RefreshTokenReuseDetectedException.class, () -> useCase.execute("raw-token", context));

        verify(refreshTokenRepository, times(0)).saveAll(any());
        verify(jwtIssuer, times(0)).issueForUser(any());
    }

    @Test
    void mustThrowInvalidRefreshTokenExceptionWhenTokenIsExpired() {
        RefreshToken existing = mock(RefreshToken.class);
        when(existing.isRevoked()).thenReturn(false);
        when(existing.isExpired()).thenReturn(true);
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(existing);

        assertThrows(InvalidRefreshTokenException.class, () -> useCase.execute("raw-token", context));

        verify(refreshTokenRepository, times(0)).save(any());
        verify(jwtIssuer, times(0)).issueForUser(any());
    }

    @Test
    void mustThrowInvalidRefreshTokenExceptionWhenUserIsNotFound() {
        RefreshToken existing = mock(RefreshToken.class);
        when(existing.isRevoked()).thenReturn(false);
        when(existing.isExpired()).thenReturn(false);
        when(existing.getUserId()).thenReturn(userId);
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(existing);
        when(userRepository.findById(any())).thenReturn(null);

        assertThrows(InvalidRefreshTokenException.class, () -> useCase.execute("raw-token", context));

        verify(refreshTokenRepository, times(0)).save(any());
        verify(jwtIssuer, times(0)).issueForUser(any());
    }

    @Test
    void mustThrowInvalidRefreshTokenExceptionWhenTenantIsNotFound() {
        RefreshToken existing = mock(RefreshToken.class);
        when(existing.isRevoked()).thenReturn(false);
        when(existing.isExpired()).thenReturn(false);
        when(existing.getUserId()).thenReturn(userId);
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(existing);

        User user = mock(User.class);
        when(user.getTenantId()).thenReturn(UUID.randomUUID());
        when(userRepository.findById(any())).thenReturn(user);

        when(tenantRepository.findById(any())).thenReturn(null);

        assertThrows(InvalidRefreshTokenException.class, () -> useCase.execute("raw-token", context));

        verify(refreshTokenRepository, times(0)).save(any());
        verify(jwtIssuer, times(0)).issueForUser(any());
    }

    @Test
    void mustThrowInvalidRefreshTokenExceptionWhenTenantIsInactive() {
        RefreshToken existing = mock(RefreshToken.class);
        when(existing.isRevoked()).thenReturn(false);
        when(existing.isExpired()).thenReturn(false);
        when(existing.getUserId()).thenReturn(userId);
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(existing);

        User user = mock(User.class);
        when(user.getTenantId()).thenReturn(UUID.randomUUID());
        when(userRepository.findById(any())).thenReturn(user);

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(false);
        when(tenantRepository.findById(any())).thenReturn(tenant);
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(userRoleRepository.findAllByUserId(any())).thenReturn(List.of());

        assertThrows(InvalidRefreshTokenException.class, () -> useCase.execute("raw-token", context));

        verify(refreshTokenRepository, times(0)).save(any());
        verify(jwtIssuer, times(0)).issueForUser(any());
    }

    @ParameterizedTest
    @EnumSource(value = UserStatus.class, names = {"SUSPENDED", "LOCKED"})
    void mustThrowInvalidRefreshTokenExceptionWhenUserIsNotActive(UserStatus status) {
        RefreshToken existing = mock(RefreshToken.class);
        when(existing.isRevoked()).thenReturn(false);
        when(existing.isExpired()).thenReturn(false);
        when(existing.getUserId()).thenReturn(userId);
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(existing);

        User user = mock(User.class);
        when(user.getTenantId()).thenReturn(UUID.randomUUID());
        when(user.isActive()).thenReturn(status == UserStatus.ACTIVE);
        when(userRepository.findById(any())).thenReturn(user);

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(any())).thenReturn(tenant);
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(userRoleRepository.findAllByUserId(any())).thenReturn(List.of());

        assertThrows(InvalidRefreshTokenException.class, () -> useCase.execute("raw-token", context));

        verify(refreshTokenRepository, times(0)).save(any());
        verify(jwtIssuer, times(0)).issueForUser(any());
    }

    @Test
    void mustIssueNewTokensAndRevokeCurrentTokenOnSuccessfulRefresh() {
        RefreshToken existing = mock(RefreshToken.class);
        when(existing.isRevoked()).thenReturn(false);
        when(existing.isExpired()).thenReturn(false);
        when(existing.getUserId()).thenReturn(userId);
        when(existing.getExpiresAt()).thenReturn(expiresAt);
        doNothing().when(existing).markUsed();
        doNothing().when(existing).revoke();
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(existing);

        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getTenantId()).thenReturn(tenantId);
        when(user.isActive()).thenReturn(true);
        when(user.mustChangePassword()).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(user);

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        when(tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true)).thenReturn(List.of());
        when(userRoleRepository.findAllByUserId(userId)).thenReturn(List.of());

        IssuedJwt issuedJwt = new IssuedJwt("JWT-ACCESS-TOKEN", UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(900));
        when(jwtIssuer.issueForUser(any())).thenReturn(issuedJwt);

        GeneratedRefreshToken generated = new GeneratedRefreshToken("PLAIN-TOKEN", "HASH-INTERNO");
        when(refreshTokenGenerator.generate()).thenReturn(generated);
        when(jwtProperties.accessTokenTtl()).thenReturn(Duration.ofSeconds(900));

        ArgumentCaptor<RefreshToken> savedTokens = ArgumentCaptor.forClass(RefreshToken.class);
        when(refreshTokenRepository.save(savedTokens.capture())).thenReturn(mock(RefreshToken.class));
        when(issuedTokenAuditRepository.save(any())).thenReturn(null);

        RefreshTokenResult response = useCase.execute("raw-token", context);

        Assertions.assertAll(
            () -> assertEquals("JWT-ACCESS-TOKEN", response.accessToken()),
            () -> assertEquals("PLAIN-TOKEN", response.refreshToken()),
            () -> assertEquals("Bearer", response.tokenType()),
            () -> assertEquals(900, response.expiresIn()),
            () -> assertEquals(false, response.mustChangePassword())
        );

        RefreshToken novoToken = savedTokens.getAllValues().get(1);
        Assertions.assertAll(
            () -> assertEquals("HASH-INTERNO", novoToken.getTokenHash()),
            () -> assertEquals("192.168.0.1", novoToken.getIpAddress()),
            () -> assertEquals("test-agent", novoToken.getUserAgent())
        );

        verify(existing, times(1)).revoke();
        verify(jwtIssuer, times(1)).issueForUser(any());
        verify(issuedTokenAuditRepository, times(1)).save(any());
        verify(refreshTokenRepository, times(2)).save(any());
    }

    @Test
    void mustIncludeOnlyRolesFromEnabledModulesInJwtClaimsOnRefresh() {
        RefreshToken existing = mock(RefreshToken.class);
        when(existing.isRevoked()).thenReturn(false);
        when(existing.isExpired()).thenReturn(false);
        when(existing.getUserId()).thenReturn(userId);
        when(existing.getExpiresAt()).thenReturn(expiresAt);
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(existing);

        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getTenantId()).thenReturn(tenantId);
        when(user.isActive()).thenReturn(true);
        when(user.mustChangePassword()).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(user);

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        TenantModuleEntity enabledModule = mock(TenantModuleEntity.class);
        when(enabledModule.getModuleId()).thenReturn(enabledModuleId);
        when(tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true)).thenReturn(List.of(enabledModule));
        when(moduleRepository.findAllById(any())).thenReturn(List.of());

        UserRoleEntity link1 = mock(UserRoleEntity.class);
        UserRoleEntity link2 = mock(UserRoleEntity.class);
        UserRoleEntity link3 = mock(UserRoleEntity.class);
        when(userRoleRepository.findAllByUserId(userId)).thenReturn(List.of(link1, link2, link3));

        RoleEntity roleEnabled = mock(RoleEntity.class);
        when(roleEnabled.getCode()).thenReturn("customers.admin");
        when(roleEnabled.getModuleId()).thenReturn(enabledModuleId);
        RoleEntity roleDisabled = mock(RoleEntity.class);
        when(roleDisabled.getCode()).thenReturn("lending.admin");
        when(roleDisabled.getModuleId()).thenReturn(disabledModuleId);
        RoleEntity roleGlobal = mock(RoleEntity.class);
        when(roleGlobal.getCode()).thenReturn("platform.admin");
        when(roleGlobal.getModuleId()).thenReturn(null);
        when(roleRepository.findAllById(any())).thenReturn(List.of(roleEnabled, roleDisabled, roleGlobal));

        ArgumentCaptor<UserClaims> claimsCaptor = ArgumentCaptor.forClass(UserClaims.class);
        IssuedJwt issuedJwt = new IssuedJwt("JWT", UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(900));
        when(jwtIssuer.issueForUser(claimsCaptor.capture())).thenReturn(issuedJwt);

        when(refreshTokenGenerator.generate()).thenReturn(new GeneratedRefreshToken("PLAIN", "HASH"));
        when(refreshTokenRepository.save(any())).thenReturn(mock(RefreshToken.class));
        when(issuedTokenAuditRepository.save(any())).thenReturn(null);
        when(jwtProperties.accessTokenTtl()).thenReturn(Duration.ofSeconds(900));

        useCase.execute("raw-token", context);

        List<String> roles = claimsCaptor.getValue().roles();
        Assertions.assertAll(
            () -> assertTrue(roles.contains("customers.admin")),
            () -> assertTrue(roles.contains("platform.admin")),
            () -> assertFalse(roles.contains("lending.admin"))
        );
    }
}
