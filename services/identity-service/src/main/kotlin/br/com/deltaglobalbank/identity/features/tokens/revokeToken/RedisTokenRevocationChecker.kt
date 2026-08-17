package br.com.deltaglobalbank.identity.features.tokens.revokeToken

import br.com.deltaglobalbank.identity.domain.token.TokenRevocationStore
import br.com.deltaglobalbank.sharedauth.RevocationReason
import br.com.deltaglobalbank.sharedauth.TokenRevocationChecker
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class RedisTokenRevocationChecker(private val store: TokenRevocationStore) : TokenRevocationChecker {

    override fun checkRevocation(jti: UUID, userId: UUID, issuedAt: Instant): RevocationReason? {
        return store.checkRevocation(jti.toString(), userId, issuedAt)
    }


}