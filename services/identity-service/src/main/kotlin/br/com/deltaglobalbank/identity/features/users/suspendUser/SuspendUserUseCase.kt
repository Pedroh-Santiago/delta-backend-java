package br.com.deltaglobalbank.identity.features.users.suspendUser

import br.com.deltaglobalbank.identity.domain.token.AccessTokenRevoker
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRevoker
import br.com.deltaglobalbank.identity.domain.user.UserNotFound
import br.com.deltaglobalbank.identity.domain.user.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class SuspendUserUseCase(private val refreshTokenRevoker: RefreshTokenRevoker,private val userRepository: UserRepository, private val accessTokenRevoker: AccessTokenRevoker) {

    private val log = LoggerFactory.getLogger(SuspendUserUseCase::class.java)

    @Transactional
    fun suspendUser(userId : UUID, tenantId: UUID,  actorId: UUID){
        val userExists = userRepository.findById(userId) ?: throw UserNotFound()
        if (userExists.tenantId != tenantId) throw UserNotFound()
        userExists.suspend()
        userRepository.save(userExists)
        accessTokenRevoker.revokeUser(userId)
        refreshTokenRevoker.revokeAllForUser(userId)
        log.info("user suspended userId={} tenantId={} by={}", userId, tenantId, actorId)

    }

}