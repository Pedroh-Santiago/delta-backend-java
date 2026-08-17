package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories

import java.util.UUID

interface ApiKeyActiveCount {
    val apiClientId: UUID
    val total: Long
}