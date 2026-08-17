package br.com.deltaglobalbank.identity.features.tenants.suspendTenant

import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class SuspendTenantUseCase(private val tenantRepository: TenantRepository) {

    private val log = LoggerFactory.getLogger(SuspendTenantUseCase::class.java)

    @Transactional
    fun suspendTenant(tenantId: UUID, actorId: UUID) {
        val tenantExists = tenantRepository.findById(tenantId) ?: throw TenantNotFoundException()
        tenantExists.suspend()
        tenantRepository.save(tenantExists)
        log.info("tenant suspended tenantId={} by={}", tenantId, actorId)
    }


}