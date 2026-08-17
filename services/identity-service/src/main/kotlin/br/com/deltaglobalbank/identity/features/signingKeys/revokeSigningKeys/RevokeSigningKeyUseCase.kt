package br.com.deltaglobalbank.identity.features.signingKeys.revokeSigningKeys

import br.com.deltaglobalbank.identity.infrastructure.security.jwt.KeyManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class RevokeSigningKeyUseCase(
    private val revoker: SigningKeyRevoker,
    private val keyManager: KeyManager
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun execute(id: UUID, revokedBy: UUID) {
        val mutated = revoker.revoke(id)
        if (mutated) {
            keyManager.refresh()
            log.info("signing_key revoked keyId={} revokedBy={} at={}", id, revokedBy, Instant.now())
        }
    }
}