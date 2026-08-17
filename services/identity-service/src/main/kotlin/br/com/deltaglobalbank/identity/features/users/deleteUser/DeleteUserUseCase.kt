package br.com.deltaglobalbank.identity.features.users.deleteUser

import br.com.deltaglobalbank.identity.domain.token.AccessTokenRevoker
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRevoker
import br.com.deltaglobalbank.identity.domain.user.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class DeleteUserUseCase(private val accessTokenRevoker: AccessTokenRevoker, private val refreshTokenRevoker: RefreshTokenRevoker,private val userRepository: UserRepository) {

    private val log = LoggerFactory.getLogger(DeleteUserUseCase::class.java)
    @Transactional
    fun deleteUser(userId: UUID, tenantId: UUID, actorId: UUID) {
        val userExists = userRepository.findById(userId) ?: return
        if (userExists.tenantId != tenantId) return
        userRepository.delete(userExists)
        accessTokenRevoker.revokeUser(userId)
        refreshTokenRevoker.revokeAllForUser(userId)
        log.info("user deleted userId={} tenantId={} by={}", userId, tenantId, actorId)
    }
}