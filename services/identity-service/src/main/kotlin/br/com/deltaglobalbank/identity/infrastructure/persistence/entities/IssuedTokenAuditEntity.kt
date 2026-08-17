package br.com.deltaglobalbank.identity.infrastructure.persistence.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "issued_tokens_audit")
class IssuedTokenAuditEntity(
    @Id
    var id: UUID,

    @Column(name = "jti", nullable = false)
    var jti: UUID,

    @Column(name = "principal_type", nullable = false)
    var principalType: String,

    @Column(name = "principal_id", nullable = false)
    var principalId: UUID,

    @Column(name = "tenant_id", nullable = false)
    var tenantId: UUID,

    @Column(name = "issued_at", nullable = false)
    var issuedAt: Instant,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,

    @Column(name = "ip_address")
    var ipAddress: String? = null,

    @Column(name = "user_agent")
    var userAgent: String? = null
)
