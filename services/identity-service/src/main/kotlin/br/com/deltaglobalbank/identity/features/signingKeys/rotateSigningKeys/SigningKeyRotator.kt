package br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeys

import br.com.deltaglobalbank.identity.domain.token.SigningKey
import br.com.deltaglobalbank.identity.domain.token.SigningKeyRepository
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.KeyGenerator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class SigningKeyRotator(
    private val signingKeyRepository: SigningKeyRepository,
    private val keyGenerator: KeyGenerator,
) {
    @Transactional
    fun rotate(): Pair<SigningKey, SigningKey?> {
        val previous = signingKeyRepository.findFirstActive()
        previous?.let {
            it.retire()
            signingKeyRepository.save(it)
        }
        val pair = keyGenerator.generateRsaKeyPair()
        val kid = "key-${Instant.now().toEpochMilli()}"
        val newKey = SigningKey.create(
            kid = kid,
            algorithm = "RS256",
            publicKey = keyGenerator.encodePublicKey(pair.public),
            privateKey = keyGenerator.encodePrivateKey(pair.private),
        )
        signingKeyRepository.save(newKey)
        return newKey to previous
    }
}