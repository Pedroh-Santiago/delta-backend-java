package br.com.deltaglobalbank.identity.features.bootstrap

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.SigningKeyEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantModuleEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserRoleEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaModuleRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaRoleRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaSigningKeyRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantModuleRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRepository
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.KeyGenerator
import br.com.deltaglobalbank.identity.infrastructure.security.password.TemporaryPasswordGenerator
import com.github.f4b6a3.uuid.UuidCreator
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class BootstrapUseCase(
    private val jpaTenantRepository: JpaTenantRepository,
    private val jpaUserRepository: JpaUserRepository,
    private val jpaUserRoleRepository: JpaUserRoleRepository,
    private val jpaRoleRepository: JpaRoleRepository,
    private val jpaModuleRepository: JpaModuleRepository,
    private val jpaTenantModuleRepository: JpaTenantModuleRepository,
    private val jpaSigningKeyRepository: JpaSigningKeyRepository,
    private val passwordEncoder: PasswordEncoder,
    private val keyGenerator: KeyGenerator,
    private val temporaryPasswordGenerator: TemporaryPasswordGenerator
) {

    @Transactional
    fun execute(): BootstrapResult {
        check(jpaTenantRepository.count() == 0L) {
            "Bootstrap já foi executado. Existem tenants no banco."
        }

        val signingKey = createInitialSigningKey()
        val tenant = createInitialTenant()
        enableAllModulesForTenant(tenant.id)
        val (admin, temporaryPassword) = createAdminUser(tenant.id)
        assignPlatformAdminRole(admin.id)

        return BootstrapResult(
            tenantId = tenant.id,
            tenantSlug = tenant.slug,
            adminEmail = admin.email,
            temporaryPassword = temporaryPassword,
            signingKeyId = signingKey.kid
        )
    }

    private fun createInitialSigningKey(): SigningKeyEntity {
        val keyPair = keyGenerator.generateRsaKeyPair()
        val kid = "key-${Instant.now().epochSecond}"

        val entity = SigningKeyEntity(
            id = UuidCreator.getTimeOrderedEpoch(),
            kid = kid,
            algorithm = "RS256",
            publicKey = keyGenerator.encodePublicKey(keyPair.public),
            privateKey = keyGenerator.encodePrivateKey(keyPair.private),
            status = "active",
            activatedAt = Instant.now()
        )

        return jpaSigningKeyRepository.save(entity)
    }

    private fun createInitialTenant(): TenantEntity {
        val tenant = TenantEntity(
            id = UuidCreator.getTimeOrderedEpoch(),
            name = "Delta Global Bank",
            slug = "delta-global-bank",
            status = "active"
        )
        return jpaTenantRepository.save(tenant)
    }

    private fun enableAllModulesForTenant(tenantId: UUID) {
        val modules = jpaModuleRepository.findAll()
        modules.forEach { module ->
            jpaTenantModuleRepository.save(
                TenantModuleEntity(
                    id = UuidCreator.getTimeOrderedEpoch(),
                    tenantId = tenantId,
                    moduleId = module.id,
                    enabled = true,
                    enabledAt = Instant.now()
                )
            )
        }
    }

    private fun createAdminUser(tenantId: UUID): Pair<UserEntity, String> {
        val temporaryPassword = temporaryPasswordGenerator.generatePassword()

        val user = UserEntity(
            id = UuidCreator.getTimeOrderedEpoch(),
            tenantId = tenantId,
            fullName = "Platform Admin",
            email = "admin@deltaglobalbank.com.br",
            passwordHash = passwordEncoder.encode(temporaryPassword)!!,
            status = "active",
            mustChangePassword = true,
            failedAttempts = 0
        )

        return jpaUserRepository.save(user) to temporaryPassword
    }

    private fun assignPlatformAdminRole(userId: UUID) {
        val role = jpaRoleRepository.findByCode("platform.admin")
            ?: error("Role 'platform.admin' não encontrada. Migration V2 foi aplicada?")

        jpaUserRoleRepository.save(
            UserRoleEntity(
                id = UuidCreator.getTimeOrderedEpoch(),
                userId = userId,
                roleId = role.id,
                grantedAt = Instant.now()
            )
        )
    }

}
