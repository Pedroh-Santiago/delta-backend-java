package br.com.deltaglobalbank.identity.domain.token

import java.util.UUID

interface SigningKeyRepository {
    fun findById(id: UUID): SigningKey?
    fun findByKid(kid: String): SigningKey?
    fun findAllByStatus(status: SigningKeyStatus): List<SigningKey>
    fun findFirstActive(): SigningKey?
    fun save(signingKey: SigningKey): SigningKey
}
