package br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.createTenantIpAllowlist

import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlist.Companion.newTenantIpAllowlist
import br.com.deltaglobalbank.identity.domain.ipAllowlist.Cidr
import br.com.deltaglobalbank.identity.domain.ipAllowlist.CidrAlreadyExistsException
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlistRepository
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository
import br.com.deltaglobalbank.identity.domain.user.TenantInactiveException
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException
import br.com.deltaglobalbank.identity.infrastructure.security.ipAllowlist.TenantIpAllowlistCache
import com.github.f4b6a3.uuid.UuidCreator
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

data class CreateTenantIpAllowlistCommand(
    val tenantId: UUID,
    val cidr: String,
    val description: String?
)

@Service
class CreateTenantIpAllowlistUsecase(
    private val tenantIpAllowlistRepository: TenantIpAllowlistRepository,
    private val tenantRepository: TenantRepository,
    private val tenantIpAllowlistCache: TenantIpAllowlistCache
){
    @Transactional
    fun execute(command: CreateTenantIpAllowlistCommand): CreateTenantIpAllowlistResponse {

        val tenant = tenantRepository.findById(command.tenantId) ?: throw TenantNotFoundException()
        if (!tenant.isActive()) throw TenantInactiveException()

        val cidr = Cidr(command.cidr).getNetworkRangeIp()
        if (tenantIpAllowlistRepository.existsByCidrAndTenantId(cidr, command.tenantId))
            throw CidrAlreadyExistsException()

        val tenantIpAllowlist = newTenantIpAllowlist(
            id = UuidCreator.getTimeOrderedEpoch(),
            tenantId = command.tenantId,
            cidr = Cidr(cidr),
            description = command.description
        )

        tenantIpAllowlistRepository.save(tenantIpAllowlist)
        tenantIpAllowlistCache.invalidate(command.tenantId)

        return CreateTenantIpAllowlistResponse(
            id = tenantIpAllowlist.id,
            tenantId = tenantIpAllowlist.tenantId,
            cidr = tenantIpAllowlist.cidr,
            description = tenantIpAllowlist.description,
            createdAt = tenantIpAllowlist.createdAt
        )

    }
}