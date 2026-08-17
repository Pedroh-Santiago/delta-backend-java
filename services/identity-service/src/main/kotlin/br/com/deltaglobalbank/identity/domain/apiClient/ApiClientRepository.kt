package br.com.deltaglobalbank.identity.domain.apiClient

import java.util.UUID

interface ApiClientRepository {
    fun findById(id: UUID): ApiClient?
    fun save(apiClient: ApiClient): ApiClient
}