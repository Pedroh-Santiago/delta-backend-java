package br.com.deltaglobalbank.identity.domain.token

import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
class AccessTokenRevoker(private val store: TokenRevocationStore, private val auditRepository: IssuedTokenAuditRepository) {

    fun revokeJti(jti: UUID){

        val audit = auditRepository.findByJti(jti.toString()) ?: return

        val ttl = Duration.between(Instant.now(), audit.expiresAt)

        if (ttl.isNegative || ttl.isZero) return

        store.revokeJti(jti.toString(), ttl)
    }

    fun revokeUser(userID: UUID){
        store.revokeUser(userID)
    }
}