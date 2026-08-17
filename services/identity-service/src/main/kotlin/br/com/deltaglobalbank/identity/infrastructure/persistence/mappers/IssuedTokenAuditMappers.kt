package br.com.deltaglobalbank.identity.infrastructure.persistence.mappers

import br.com.deltaglobalbank.identity.domain.token.IssuedTokenAudit
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.IssuedTokenAuditEntity

fun IssuedTokenAuditEntity.toDomain(): IssuedTokenAudit = IssuedTokenAudit(
    id = this.id,
    jti = this.jti,
    principalType = this.principalType,
    principalId = this.principalId,
    tenantId = this.tenantId,
    issuedAt = this.issuedAt,
    expiresAt = this.expiresAt,
    ipAddress = this.ipAddress,
    userAgent = this.userAgent
)

fun IssuedTokenAudit.toEntity(): IssuedTokenAuditEntity {
    val s = snapshot()
    return IssuedTokenAuditEntity(
        id = s.id,
        jti = s.jti,
        principalType = s.principalType,
        principalId = s.principalId,
        tenantId = s.tenantId,
        issuedAt = s.issuedAt,
        expiresAt = s.expiresAt,
        ipAddress = s.ipAddress,
        userAgent = s.userAgent
    )
}
