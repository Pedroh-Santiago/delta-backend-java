package br.com.deltaglobalbank.identity.features.auth.refresh;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.token.InvalidRefreshTokenException;
import br.com.deltaglobalbank.identity.domain.token.RefreshToken;
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRepository;
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenReuseDetectedException;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.IssuedTokenAuditEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ModuleEntity;
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
import com.github.f4b6a3.uuid.UuidCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenUseCase.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final JpaUserRoleRepository userRoleRepository;
    private final JpaRoleRepository roleRepository;
    private final JpaTenantModuleRepository tenantModuleRepository;
    private final JpaModuleRepository moduleRepository;
    private final JpaIssuedTokenAuditRepository issuedTokenAuditRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final JwtIssuer jwtIssuer;
    private final JwtIssuerProperties jwtProperties;

    public RefreshTokenUseCase(
        RefreshTokenRepository refreshTokenRepository,
        UserRepository userRepository,
        TenantRepository tenantRepository,
        JpaUserRoleRepository userRoleRepository,
        JpaRoleRepository roleRepository,
        JpaTenantModuleRepository tenantModuleRepository,
        JpaModuleRepository moduleRepository,
        JpaIssuedTokenAuditRepository issuedTokenAuditRepository,
        RefreshTokenGenerator refreshTokenGenerator,
        JwtIssuer jwtIssuer,
        JwtIssuerProperties jwtProperties
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.tenantModuleRepository = tenantModuleRepository;
        this.moduleRepository = moduleRepository;
        this.issuedTokenAuditRepository = issuedTokenAuditRepository;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.jwtIssuer = jwtIssuer;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public RefreshTokenResult execute(String refreshToken, RefreshTokenContext context) {
        String hash = refreshTokenGenerator.hash(refreshToken);

        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash);
        if (existing == null) {
            throw new InvalidRefreshTokenException();
        }

        if (existing.isRevoked()) {
            log.warn("Reuse de refresh token detectado para user {}. Revogando todos os tokens ativos.",
                existing.getUserId());
            revokeAllActiveTokensOfUser(existing.getUserId());
            throw new RefreshTokenReuseDetectedException();
        }

        if (existing.isExpired()) {
            throw new InvalidRefreshTokenException();
        }

        User user = userRepository.findById(existing.getUserId());
        if (user == null) {
            throw new InvalidRefreshTokenException();
        }

        Tenant tenant = tenantRepository.findById(user.getTenantId());
        if (tenant == null) {
            throw new InvalidRefreshTokenException();
        }

        Set<UUID> roleIds = userRoleRepository.findAllByUserId(user.getId()).stream()
            .map(UserRoleEntity::getRoleId)
            .collect(Collectors.toSet());
        boolean isPlatformAdmin = !roleIds.isEmpty() && roleRepository.findAllById(roleIds).stream()
            .anyMatch(it -> it.getCode().equals("platform.admin"));

        if (!isPlatformAdmin && !tenant.isActive()) {
            throw new InvalidRefreshTokenException();
        }

        if (!user.isActive()) {
            throw new InvalidRefreshTokenException();
        }

        existing.markUsed();
        existing.revoke();
        refreshTokenRepository.save(existing);

        RolesAndModules resolved = resolveRolesAndModules(user);
        IssuedJwt issuedJwt = jwtIssuer.issueForUser(new UserClaims(
            user.getId(),
            user.getTenantId(),
            resolved.roleCodes(),
            resolved.moduleCodes(),
            user.mustChangePassword()
        ));

        GeneratedRefreshToken newRawToken = refreshTokenGenerator.generate();
        RefreshToken newRefreshToken = RefreshToken.newToken(
            UuidCreator.getTimeOrderedEpoch(),
            user.getId(),
            newRawToken.hash(),
            existing.getExpiresAt(),
            context.userAgent(),
            context.ipAddress()
        );
        refreshTokenRepository.save(newRefreshToken);

        issuedTokenAuditRepository.save(new IssuedTokenAuditEntity(
            UuidCreator.getTimeOrderedEpoch(),
            issuedJwt.jti(),
            "user",
            user.getId(),
            user.getTenantId(),
            issuedJwt.issuedAt(),
            issuedJwt.expiresAt(),
            context.ipAddress(),
            context.userAgent()
        ));

        Instant now = Instant.now();
        long expiresInSeconds = Math.max(Duration.between(now, existing.getExpiresAt()).getSeconds(), 0);

        return new RefreshTokenResult(
            issuedJwt.token(),
            newRawToken.plainText(),
            jwtProperties.accessTokenTtl().getSeconds(),
            user.mustChangePassword()
        );
    }

    private void revokeAllActiveTokensOfUser(UUID userId) {
        List<RefreshToken> active = refreshTokenRepository.findAllActiveByUserId(userId);
        if (active.isEmpty()) {
            return;
        }
        active.forEach(RefreshToken::revoke);
        refreshTokenRepository.saveAll(active);
    }

    private record RolesAndModules(List<String> roleCodes, List<String> moduleCodes) {
    }

    private RolesAndModules resolveRolesAndModules(User user) {
        List<TenantModuleEntity> enabledTenantModules =
            tenantModuleRepository.findAllByTenantIdAndEnabled(user.getTenantId(), true);
        Set<UUID> enabledModuleIds = enabledTenantModules.stream()
            .map(TenantModuleEntity::getModuleId)
            .collect(Collectors.toSet());

        List<String> moduleCodes = enabledModuleIds.isEmpty()
            ? List.of()
            : moduleRepository.findAllById(enabledModuleIds).stream().map(ModuleEntity::getCode).toList();

        List<UserRoleEntity> userRoles = userRoleRepository.findAllByUserId(user.getId());
        Set<UUID> roleIds = userRoles.stream().map(UserRoleEntity::getRoleId).collect(Collectors.toSet());

        if (roleIds.isEmpty()) {
            return new RolesAndModules(List.of(), moduleCodes);
        }

        List<String> roleCodes = roleRepository.findAllById(roleIds).stream()
            .filter(it -> it.getModuleId() == null || enabledModuleIds.contains(it.getModuleId()))
            .map(RoleEntity::getCode)
            .toList();

        return new RolesAndModules(roleCodes, moduleCodes);
    }
}
