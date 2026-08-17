package br.com.deltaglobalbank.identity.features.auth.login;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.deltaglobalbank.identity.domain.ipAllowlist.Cidr;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.IpNotAllowedException;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlistRepository;
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository;
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository;
import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import br.com.deltaglobalbank.identity.domain.shared.PasswordHasher;
import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.token.IssuedTokenAudit;
import br.com.deltaglobalbank.identity.domain.token.IssuedTokenAuditRepository;
import br.com.deltaglobalbank.identity.domain.token.RefreshToken;
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRepository;
import br.com.deltaglobalbank.identity.domain.user.Email;
import br.com.deltaglobalbank.identity.domain.user.InvalidCredentialsException;
import br.com.deltaglobalbank.identity.domain.user.TenantNotActiveException;
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
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginUseCase {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final TenantModuleRepository tenantModuleRepository;
    private final ModuleRepository moduleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final IssuedTokenAuditRepository issuedTokenAuditRepository;
    private final PasswordHasher passwordHasher;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final JwtIssuer jwtIssuer;
    private final JwtIssuerProperties jwtProperties;
    private final TenantIpAllowlistRepository tenantIpAllowlistRepository;
    private final TenantIpAllowlistCache tenantIpAllwlistCache;
    private final LockoutProperties lockoutProperties;
    private final FailedLoginRecorder failedLoginRecorder;

    private final SecureRandom timingRandom = new SecureRandom();

    public LoginUseCase(
        UserRepository userRepository,
        TenantRepository tenantRepository,
        RoleRepository roleRepository,
        TenantModuleRepository tenantModuleRepository,
        ModuleRepository moduleRepository,
        RefreshTokenRepository refreshTokenRepository,
        IssuedTokenAuditRepository issuedTokenAuditRepository,
        PasswordHasher passwordHasher,
        RefreshTokenGenerator refreshTokenGenerator,
        JwtIssuer jwtIssuer,
        JwtIssuerProperties jwtProperties,
        TenantIpAllowlistRepository tenantIpAllowlistRepository,
        TenantIpAllowlistCache tenantIpAllwlistCache,
        LockoutProperties lockoutProperties,
        FailedLoginRecorder failedLoginRecorder
    ) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.tenantModuleRepository = tenantModuleRepository;
        this.moduleRepository = moduleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.issuedTokenAuditRepository = issuedTokenAuditRepository;
        this.passwordHasher = passwordHasher;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.jwtIssuer = jwtIssuer;
        this.jwtProperties = jwtProperties;
        this.tenantIpAllowlistRepository = tenantIpAllowlistRepository;
        this.tenantIpAllwlistCache = tenantIpAllwlistCache;
        this.lockoutProperties = lockoutProperties;
        this.failedLoginRecorder = failedLoginRecorder;
    }

    @Transactional
    public LoginResult execute(LoginRequest request, LoginContext context) {
        Email email = parseEmailOrFail(request.email());

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw failWithTimingDelay();
        }

        Tenant tenant = tenantRepository.findById(user.getTenantId());
        if (tenant == null) {
            throw new InvalidCredentialsException();
        }

        assertIpAllowed(user.getTenantId(), context.ipAddress());

        boolean isPlatformAdmin = roleRepository.findAllByUserId(user.getId()).stream()
            .anyMatch(it -> it.getCode().value().equals("platform.admin"));

        if (!isPlatformAdmin && !tenant.isActive()) {
            throw new TenantNotActiveException();
        }

        Instant now = Instant.now();
        try {
            user.authenticate(request.password(), passwordHasher, now, lockoutProperties.toPolicy());
        } catch (InvalidCredentialsException ex) {
            failedLoginRecorder.record(user);
            throw ex;
        }

        userRepository.save(user);

        RolesAndModules resolved = resolveRolesAndModules(user);
        IssuedJwt issuedJwt = issueAccessToken(user, resolved.roleCodes(), resolved.moduleCodes());
        String refreshToken = createRefreshToken(user, context);
        auditIssuedToken(user, issuedJwt, context);

        return new LoginResult(
            issuedJwt.token(),
            refreshToken,
            jwtProperties.accessTokenTtl().getSeconds(),
            user.mustChangePassword()
        );
    }

    private Email parseEmailOrFail(String rawEmail) {
        try {
            return new Email(rawEmail);
        } catch (IllegalArgumentException ex) {
            throw failWithTimingDelay();
        }
    }

    private InvalidCredentialsException failWithTimingDelay() {
        try {
            Thread.sleep(200L + timingRandom.nextInt(150));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return new InvalidCredentialsException();
    }

    private record RolesAndModules(List<String> roleCodes, List<String> moduleCodes) {
    }

    private RolesAndModules resolveRolesAndModules(User user) {
        List<br.com.deltaglobalbank.identity.domain.module.TenantModule> enabledTenantModules =
            tenantModuleRepository.findAllByTenantIdAndEnabled(user.getTenantId(), true);
        Set<UUID> enabledModuleIds = enabledTenantModules.stream()
            .map(br.com.deltaglobalbank.identity.domain.module.TenantModule::getModuleId)
            .collect(Collectors.toSet());

        List<String> moduleCodes = enabledModuleIds.isEmpty()
            ? List.of()
            : moduleRepository.findAllByIds(enabledModuleIds).stream().map(it -> it.getCode().value()).toList();

        List<String> roleCodes = roleRepository.findAllByUserId(user.getId()).stream()
            .filter(Role::isActive)
            .filter(role -> role.getModuleId() == null || enabledModuleIds.contains(role.getModuleId()))
            .map(Role::getCode)
            .map(it -> it.value())
            .toList();

        return new RolesAndModules(roleCodes, moduleCodes);
    }

    private IssuedJwt issueAccessToken(User user, List<String> roleCodes, List<String> moduleCodes) {
        return jwtIssuer.issueForUser(new UserClaims(
            user.getId(),
            user.getTenantId(),
            roleCodes,
            moduleCodes,
            user.mustChangePassword()
        ));
    }

    private String createRefreshToken(User user, LoginContext context) {
        GeneratedRefreshToken token = refreshTokenGenerator.generate();
        Instant now = Instant.now();
        refreshTokenRepository.save(new RefreshToken(
            UuidCreator.getTimeOrderedEpoch(),
            user.getId(),
            token.hash(),
            now.plus(jwtProperties.refreshTokenTtl()),
            null,
            now,
            null,
            context.userAgent(),
            context.ipAddress()
        ));
        return token.plainText();
    }

    private void auditIssuedToken(User user, IssuedJwt issuedJwt, LoginContext context) {
        issuedTokenAuditRepository.save(new IssuedTokenAudit(
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
    }

    private void assertIpAllowed(UUID tenantId, String ipAddress) {
        List<String> cidrs = tenantIpAllwlistCache.get(tenantId);
        if (cidrs == null) {
            cidrs = tenantIpAllowlistRepository.findAllByTenantId(tenantId).stream()
                .map(it -> it.getCidr().value())
                .toList();
            tenantIpAllwlistCache.set(tenantId, cidrs);
        }

        if (cidrs.isEmpty()) {
            return;
        }
        if (ipAddress == null || cidrs.stream().noneMatch(cidr -> new Cidr(cidr).matches(ipAddress))) {
            throw new IpNotAllowedException();
        }
    }
}
