package br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.deleteTenantIpAllowlist

import br.com.deltaglobalbank.identity.domain.ipAllowlist.IpAllowlistEntryNotFoundException
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlistRepository
import br.com.deltaglobalbank.identity.infrastructure.security.ipAllowlist.TenantIpAllowlistCache
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

data class DeleteTenantIpAllowlistCommand(val id:  UUID, val tenantId: UUID)

@Service
class DeleteTenantIpAllowlistUseCase(
    private val tenantIpAllowlistRepository: TenantIpAllowlistRepository,
    private val tenantIpAllowlistCache: TenantIpAllowlistCache
){
    @Transactional
    fun execute(command: DeleteTenantIpAllowlistCommand){
        val entry = tenantIpAllowlistRepository.findById(command.id) ?: throw IpAllowlistEntryNotFoundException()
        if (entry.tenantId != command.tenantId) throw IpAllowlistEntryNotFoundException()

        tenantIpAllowlistRepository.delete(command.id)
        tenantIpAllowlistCache.invalidate(entry.tenantId)
    }
}
