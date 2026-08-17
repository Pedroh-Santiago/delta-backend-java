package br.com.deltaglobalbank.identity.features.tenants.activateTenant

import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ActivateTenantUseCase(private val tenantRepository: TenantRepository) {

    private val log = LoggerFactory.getLogger(ActivateTenantUseCase::class.java)

    @Transactional
    fun activateTenant(tenantId: UUID, actorId: UUID){
        val tenantExists = this.tenantRepository.findById(tenantId) ?: throw TenantNotFoundException()
        tenantExists.activate()
        tenantRepository.save(tenantExists)
        log.info("tenant activated tenantId={} by={}", tenantId, actorId)
    }
}