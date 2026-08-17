package br.com.deltaglobalbank.identity.domain.token

import java.util.UUID

interface RefreshTokenRepository {
    fun findById(id: UUID): RefreshToken?
    fun findByTokenHash(hash: String): RefreshToken?
    fun findAllByUserId(userId: UUID): List<RefreshToken>
    fun findAllActiveByUserId(userId: UUID): List<RefreshToken>
    fun save(refreshToken: RefreshToken): RefreshToken
    fun saveAll(refreshTokens: List<RefreshToken>): List<RefreshToken>
}
