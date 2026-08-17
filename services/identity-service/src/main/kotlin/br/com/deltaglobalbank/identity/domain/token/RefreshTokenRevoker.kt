package br.com.deltaglobalbank.identity.domain.token

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class RefreshTokenRevoker(private val refreshTokenRepository: RefreshTokenRepository) {
    fun revokeAllForUser(userId: UUID) {
        val tokens = refreshTokenRepository.findAllActiveByUserId(userId)
        tokens.forEach { it.revoke() }
        refreshTokenRepository.saveAll(tokens)
    }
}