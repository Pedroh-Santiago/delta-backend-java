package br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeys

import br.com.deltaglobalbank.identity.features.signingKeys.toView
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.KeyManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class RotateSigningKeyUseCase(
    private val rotation: SigningKeyRotator,
    private val keyManager: KeyManager,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun execute(rotatedBy: UUID): RotateSigningKeyResponse {
        val (newKey, previous) = rotation.rotate()

        keyManager.refresh()

        log.info("signing_key rotated newKid={} previousKid={} rotatedBy={} at={}",
            newKey.kid, previous?.kid, rotatedBy, Instant.now())

        return RotateSigningKeyResponse(
            newKey = newKey.toView(),
            previousKey = previous?.toView(),
        )
    }
}