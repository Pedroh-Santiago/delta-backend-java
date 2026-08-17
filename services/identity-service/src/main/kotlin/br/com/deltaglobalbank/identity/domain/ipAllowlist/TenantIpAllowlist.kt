package br.com.deltaglobalbank.identity.domain.ipAllowlist

import java.time.Instant
import java.util.UUID

class TenantIpAllowlist(
    val id: UUID,
    val tenantId: UUID,
    val cidr: Cidr,
    val description: String?,
    val createdAt: Instant
){
    companion object{
        fun newTenantIpAllowlist(
            id: UUID,
            tenantId: UUID,
            cidr: Cidr,
            description: String?,
        ): TenantIpAllowlist{
            val now = Instant.now()
            return TenantIpAllowlist(
                id = id,
                tenantId = tenantId,
                cidr = cidr,
                description = description,
                createdAt = now
            )
        }
    }

    override fun equals(other: Any?): Boolean{
        if (other !is TenantIpAllowlist) return false
        return this.id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    fun snapshot(): TenantIpAllowlistSnapshot = TenantIpAllowlistSnapshot(
        id = id,
        tenantId = tenantId,
        cidr = cidr.value,
        description = description,
        createdAt = createdAt
    )

    data class TenantIpAllowlistSnapshot(
        val id: UUID,
        val tenantId: UUID,
        val cidr: String,
        val description: String?,
        val createdAt: Instant
    )
}