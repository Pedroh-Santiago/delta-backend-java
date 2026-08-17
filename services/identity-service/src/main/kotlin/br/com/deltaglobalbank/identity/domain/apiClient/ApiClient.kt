package br.com.deltaglobalbank.identity.domain.apiClient

import java.time.Instant
import java.util.UUID

class ApiClient(
    val id: UUID,
    val tenantId: UUID,
    val name: String,
    val description: String?,
    status: ApiClientStatus,
    val createdAt: Instant,
    updatedAt: Instant,
) {
    private var _status: ApiClientStatus = status
    private var _updatedAt: Instant = updatedAt

    companion object {
        fun newApiClient(
            id: UUID,
            tenantId: UUID,
            name: String,
            description: String?
        ): ApiClient {
            val now = Instant.now()
            return ApiClient(
                id = id,
                tenantId = tenantId,
                name = name,
                description = description,
                status = ApiClientStatus.ACTIVE,
                createdAt = now,
                updatedAt = now,
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ApiClient) return false
        return this.id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    fun snapshot(): ApiClientSnapshot = ApiClientSnapshot(
        id = id,
        tenantId = tenantId,
        name = name,
        description = description,
        status = _status,
        createdAt = createdAt,
        updatedAt = _updatedAt
    )

    fun isActive(): Boolean {
        return _status == ApiClientStatus.ACTIVE
    }

    fun statusAsString(): String {
        return when (_status) {
            ApiClientStatus.ACTIVE -> "active"
            ApiClientStatus.SUSPENDED -> "suspended"
        }
    }

    data class ApiClientSnapshot(
        val id: UUID,
        val tenantId: UUID,
        val name: String,
        val description: String?,
        val status: ApiClientStatus,
        val createdAt: Instant,
        val updatedAt: Instant
    )
}