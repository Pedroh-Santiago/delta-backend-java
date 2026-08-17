package br.com.deltaglobalbank.identity.features.signingKeys.revokeSigningKeys

import br.com.deltaglobalbank.identity.domain.token.CannotRevokeActiveKeyException
import br.com.deltaglobalbank.identity.domain.token.SigningKeyNotFoundException
import br.com.deltaglobalbank.identity.domain.token.SigningKeyRepository
import br.com.deltaglobalbank.identity.domain.token.SigningKeyStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class SigningKeyRevoker(
    private val signingKeyRepository: SigningKeyRepository
) {
    @Transactional
    fun revoke(id: UUID): Boolean {
        val key = signingKeyRepository.findById(id)
            ?: throw SigningKeyNotFoundException()

        return when (key.status()) {
            SigningKeyStatus.ACTIVE  -> throw CannotRevokeActiveKeyException()
            SigningKeyStatus.REVOKED -> false
            SigningKeyStatus.RETIRED -> {
                key.revoke()
                signingKeyRepository.save(key)
                true
            }
        }
    }
}