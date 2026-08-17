package br.com.deltaglobalbank.identity.features.auth.logout

import br.com.deltaglobalbank.identity.domain.token.AccessTokenRevoker
import br.com.deltaglobalbank.identity.domain.token.MissingRefreshTokenException
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRepository
import br.com.deltaglobalbank.identity.infrastructure.security.token.RefreshTokenGenerator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class LogoutUseCase(private val accessTokenRevoker: AccessTokenRevoker,private val refreshTokenGenerator: RefreshTokenGenerator,private val refreshTokenRepository: RefreshTokenRepository) {

    @Transactional
    fun logout(userId: UUID, jti: UUID, refreshToken: String?, allSessions: Boolean) {
        if (allSessions) {
            revokeAllSessions(userId)
            accessTokenRevoker.revokeUser(userId)
            return
        }
        val token = refreshToken ?: throw MissingRefreshTokenException()
        revokeSession(userId, token)
        accessTokenRevoker.revokeJti(jti)
    }

    private fun revokeSession(userId: UUID, token: String) {
        val hash = refreshTokenGenerator.hash(token)
        val refreshToken = refreshTokenRepository.findByTokenHash(hash) ?: return
        if (refreshToken.userId != userId) return
        if (refreshToken.isRevoked()) return
        refreshToken.revoke()
        refreshTokenRepository.save(refreshToken)
    }

    private fun revokeAllSessions(userId: UUID) {
        val tokens = refreshTokenRepository.findAllActiveByUserId(userId)
        tokens.forEach { it.revoke() }
        refreshTokenRepository.saveAll(tokens)
    }
}