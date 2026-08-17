package br.com.deltaglobalbank.identity.features.signingKeys.listSigningKeys

import br.com.deltaglobalbank.identity.domain.token.SigningKeyRepository
import br.com.deltaglobalbank.identity.domain.token.SigningKeyStatus
import br.com.deltaglobalbank.identity.features.signingKeys.toView
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListSigningKeysUseCase(
    private val signingKeyRepository: SigningKeyRepository
) {
    @Transactional(readOnly = true)
    fun execute(): ListSigningKeysResponse {
        val all = SigningKeyStatus.entries
            .flatMap { signingKeyRepository.findAllByStatus(it) }
        return ListSigningKeysResponse(items = all.map { it.toView() })
    }
}