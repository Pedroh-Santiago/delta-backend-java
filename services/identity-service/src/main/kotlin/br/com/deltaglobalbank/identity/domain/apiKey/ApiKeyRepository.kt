package br.com.deltaglobalbank.identity.domain.apiKey

import java.util.UUID

interface ApiKeyRepository {
    fun save(apiKey: ApiKey): ApiKey
    fun findById(id: UUID): ApiKey?
    fun findByFingerprint(fingerprint: String): ApiKey?
}