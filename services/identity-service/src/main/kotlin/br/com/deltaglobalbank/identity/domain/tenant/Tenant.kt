package br.com.deltaglobalbank.identity.domain.tenant

import java.time.Instant
import java.util.UUID

class Tenant(
    val id: UUID,
    val name: String,
    val slug: String,
    status: TenantStatus,
    val createdAt: Instant,
    updatedAt: Instant
) {

    companion object {
        fun create(id: UUID, name: String, slug: TenantSlug): Tenant {
            require(name.length in 3..255) { "invalid_tenant_name" }
            val now = Instant.now()
            return Tenant(
                id = id,
                name = name,
                slug = slug.value,
                status = TenantStatus.ACTIVE,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    private var _status: TenantStatus = status
    private var _updatedAt: Instant = updatedAt

    fun isActive(): Boolean = _status == TenantStatus.ACTIVE

    fun activate() {
        _status = TenantStatus.ACTIVE
        _updatedAt = Instant.now()
    }

    fun suspend(){
        _status = TenantStatus.SUSPENDED
        _updatedAt = Instant.now()
    }

    fun deactivate(){
        _status = TenantStatus.INACTIVE
    }

    fun snapshot(): TenantSnapshot = TenantSnapshot(
        id = id,
        name = name,
        slug = slug,
        status = _status,
        createdAt = createdAt,
        updatedAt = _updatedAt
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tenant) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "Tenant(id=$id, slug=$slug, status=$_status)"
}

data class TenantSnapshot(
    val id: UUID,
    val name: String,
    val slug: String,
    val status: TenantStatus,
    val createdAt: Instant,
    val updatedAt: Instant
)
