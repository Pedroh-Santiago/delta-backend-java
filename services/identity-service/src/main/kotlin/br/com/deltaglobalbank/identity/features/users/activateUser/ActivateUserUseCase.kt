package br.com.deltaglobalbank.identity.features.users.activateUser

import br.com.deltaglobalbank.identity.domain.user.UserNotFound
import br.com.deltaglobalbank.identity.domain.user.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ActivateUserUseCase(private val userRepository: UserRepository) {
    private val log = LoggerFactory.getLogger(ActivateUserUseCase::class.java)

    @Transactional
    fun activateUser(userId: UUID, tenantId: UUID, actorId: UUID) {
        val userExists = userRepository.findById(userId) ?: throw UserNotFound()
        if(userExists.tenantId != tenantId) throw UserNotFound()
        userExists.activate()
        userRepository.save(userExists)
        log.info("user activated userId={} tenantId={} by={}", userId, tenantId, actorId)
    }
}