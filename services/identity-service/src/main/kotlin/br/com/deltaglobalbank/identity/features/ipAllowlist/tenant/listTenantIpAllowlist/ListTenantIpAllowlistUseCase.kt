package br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.listTenantIpAllowlist

import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlistRepository
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

data class ListTenantIpAllowlistQuery(val tenantId: UUID)

@Service
class ListTenantIpAllowlistUseCase(
    private val tenantIpAllowlistRepository: TenantIpAllowlistRepository,
    private val tenantRepository: TenantRepository
){
    @Transactional(readOnly = true)
    fun execute(query: ListTenantIpAllowlistQuery): ListTenantIpAllowlistResponse {
        tenantRepository.findById(query.tenantId) ?: throw TenantNotFoundException()

        val entries = tenantIpAllowlistRepository.findAllByTenantId(query.tenantId)
        val items = entries.map{ tenantIpAllowlist ->
            ListedTenantIpAllowlist(
                id = tenantIpAllowlist.id,
                tenantId = tenantIpAllowlist.tenantId,
                cidr = tenantIpAllowlist.cidr,
                description = tenantIpAllowlist.description,
                createdAt = tenantIpAllowlist.createdAt
            )
        }

        return ListTenantIpAllowlistResponse(
            items = items
        )
    }
}