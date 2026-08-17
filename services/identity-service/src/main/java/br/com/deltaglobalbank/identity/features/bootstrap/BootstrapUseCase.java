package br.com.deltaglobalbank.identity.features.bootstrap;

import java.time.Instant;
import java.util.UUID;

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ModuleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.RoleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.SigningKeyEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantModuleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserRoleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaModuleRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaRoleRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaSigningKeyRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantModuleRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.KeyGenerator;
import br.com.deltaglobalbank.identity.infrastructure.security.password.TemporaryPasswordGenerator;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BootstrapUseCase {

    private final JpaTenantRepository jpaTenantRepository;
    private final JpaUserRepository jpaUserRepository;
    private final JpaUserRoleRepository jpaUserRoleRepository;
    private final JpaRoleRepository jpaRoleRepository;
    private final JpaModuleRepository jpaModuleRepository;
    private final JpaTenantModuleRepository jpaTenantModuleRepository;
    private final JpaSigningKeyRepository jpaSigningKeyRepository;
    private final PasswordEncoder passwordEncoder;
    private final KeyGenerator keyGenerator;
    private final TemporaryPasswordGenerator temporaryPasswordGenerator;

    public BootstrapUseCase(
        JpaTenantRepository jpaTenantRepository,
        JpaUserRepository jpaUserRepository,
        JpaUserRoleRepository jpaUserRoleRepository,
        JpaRoleRepository jpaRoleRepository,
        JpaModuleRepository jpaModuleRepository,
        JpaTenantModuleRepository jpaTenantModuleRepository,
        JpaSigningKeyRepository jpaSigningKeyRepository,
        PasswordEncoder passwordEncoder,
        KeyGenerator keyGenerator,
        TemporaryPasswordGenerator temporaryPasswordGenerator
    ) {
        this.jpaTenantRepository = jpaTenantRepository;
        this.jpaUserRepository = jpaUserRepository;
        this.jpaUserRoleRepository = jpaUserRoleRepository;
        this.jpaRoleRepository = jpaRoleRepository;
        this.jpaModuleRepository = jpaModuleRepository;
        this.jpaTenantModuleRepository = jpaTenantModuleRepository;
        this.jpaSigningKeyRepository = jpaSigningKeyRepository;
        this.passwordEncoder = passwordEncoder;
        this.keyGenerator = keyGenerator;
        this.temporaryPasswordGenerator = temporaryPasswordGenerator;
    }

    @Transactional
    public BootstrapResult execute() {
        if (jpaTenantRepository.count() != 0L) {
            throw new IllegalStateException("Bootstrap já foi executado. Existem tenants no banco.");
        }

        SigningKeyEntity signingKey = createInitialSigningKey();
        TenantEntity tenant = createInitialTenant();
        enableAllModulesForTenant(tenant.getId());
        AdminUserCreation adminCreation = createAdminUser(tenant.getId());
        assignPlatformAdminRole(adminCreation.user().getId());

        return new BootstrapResult(
            tenant.getId(),
            tenant.getSlug(),
            adminCreation.user().getEmail(),
            adminCreation.temporaryPassword(),
            signingKey.getKid()
        );
    }

    private record AdminUserCreation(UserEntity user, String temporaryPassword) {
    }

    private SigningKeyEntity createInitialSigningKey() {
        var keyPair = keyGenerator.generateRsaKeyPair();
        String kid = "key-" + Instant.now().getEpochSecond();

        SigningKeyEntity entity = new SigningKeyEntity(
            UuidCreator.getTimeOrderedEpoch(),
            kid,
            "RS256",
            keyGenerator.encodePublicKey(keyPair.getPublic()),
            keyGenerator.encodePrivateKey(keyPair.getPrivate()),
            "active",
            Instant.now(),
            Instant.now(),
            null
        );

        return jpaSigningKeyRepository.save(entity);
    }

    private TenantEntity createInitialTenant() {
        TenantEntity tenant = new TenantEntity(
            UuidCreator.getTimeOrderedEpoch(),
            "Delta Global Bank",
            "delta-global-bank",
            "active",
            Instant.now(),
            Instant.now(),
            null
        );
        return jpaTenantRepository.save(tenant);
    }

    private void enableAllModulesForTenant(UUID tenantId) {
        for (ModuleEntity module : jpaModuleRepository.findAll()) {
            jpaTenantModuleRepository.save(new TenantModuleEntity(
                UuidCreator.getTimeOrderedEpoch(),
                tenantId,
                module.getId(),
                true,
                Instant.now(),
                Instant.now(),
                null
            ));
        }
    }

    private AdminUserCreation createAdminUser(UUID tenantId) {
        String temporaryPassword = temporaryPasswordGenerator.generatePassword();

        UserEntity user = new UserEntity(
            UuidCreator.getTimeOrderedEpoch(),
            tenantId,
            "Platform Admin",
            "admin@deltaglobalbank.com.br",
            passwordEncoder.encode(temporaryPassword),
            "active",
            true,
            null,
            null,
            0,
            null,
            Instant.now(),
            Instant.now(),
            null
        );

        return new AdminUserCreation(jpaUserRepository.save(user), temporaryPassword);
    }

    private void assignPlatformAdminRole(UUID userId) {
        RoleEntity role = jpaRoleRepository.findByCode("platform.admin");
        if (role == null) {
            throw new IllegalStateException("Role 'platform.admin' não encontrada. Migration V2 foi aplicada?");
        }

        jpaUserRoleRepository.save(new UserRoleEntity(
            UuidCreator.getTimeOrderedEpoch(),
            userId,
            role.getId(),
            Instant.now(),
            null,
            null
        ));
    }
}
