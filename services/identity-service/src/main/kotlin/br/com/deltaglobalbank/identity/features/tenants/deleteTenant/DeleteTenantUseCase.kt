package br.com.deltaglobalbank.identity.features.tenants.deleteTenant

import br.com.deltaglobalbank.identity.domain.tenant.Tenant
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.domain.token.AccessTokenRevoker
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRevoker
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class DeleteTenantUseCase(private val tenantRepository: TenantRepository) {

    private val log = LoggerFactory.getLogger(DeleteTenantUseCase::class.java)

    @Transactional
    fun deleteTenant(tenantId: UUID, actorId: UUID) {
        val tenantExists = tenantRepository.findById(tenantId) ?: throw TenantNotFoundException()
        tenantExists.deactivate()
        tenantRepository.save(tenantExists)
        tenantRepository.delete(tenantId)
        log.info("tenant deleted tenantId={} by={}", tenantId, actorId)
    }
}