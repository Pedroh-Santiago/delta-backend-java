package br.com.deltaglobalbank.identity.domain.token

interface IssuedTokenAuditRepository {
    fun save(audit: IssuedTokenAudit)
    fun findByJti(jti: String): IssuedTokenAudit?
}
