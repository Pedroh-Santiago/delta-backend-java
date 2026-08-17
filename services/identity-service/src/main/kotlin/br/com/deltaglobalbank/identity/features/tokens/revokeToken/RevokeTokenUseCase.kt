package br.com.deltaglobalbank.identity.features.tokens.revokeToken

import br.com.deltaglobalbank.identity.domain.token.AccessTokenRevoker
import br.com.deltaglobalbank.identity.domain.user.UserNotFound
import br.com.deltaglobalbank.identity.domain.user.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class RevokeTokenUseCase(private val accessTokenRevoker : AccessTokenRevoker, private val userRepository: UserRepository) {

    fun revokeJti(jti: UUID) {
        accessTokenRevoker.revokeJti(jti)
    }

    fun revokeAllForUser(userId: UUID, tenantId: UUID) {
        val user = userRepository.findById(userId) ?: throw UserNotFound()
        if (user.tenantId != tenantId) throw UserNotFound()
        accessTokenRevoker.revokeUser(user.id)
    }
}