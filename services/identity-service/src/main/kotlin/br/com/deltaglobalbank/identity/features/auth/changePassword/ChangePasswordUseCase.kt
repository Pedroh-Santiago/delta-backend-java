package br.com.deltaglobalbank.identity.features.auth.changePassword

import br.com.deltaglobalbank.identity.domain.shared.PasswordHasher
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRepository
import br.com.deltaglobalbank.identity.domain.user.CurrentPasswordIncorrectException
import br.com.deltaglobalbank.identity.domain.user.Password
import br.com.deltaglobalbank.identity.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ChangePasswordUseCase(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordHasher: PasswordHasher
) {

    @Transactional
    fun execute(userId: UUID, request: ChangePasswordRequest) {
        val user = userRepository.findById(userId)
            ?: throw CurrentPasswordIncorrectException()

        val currentPassword = Password(request.currentPassword)
        val newPassword = Password(request.newPassword)

        user.changePassword(currentPassword, newPassword, passwordHasher)
        userRepository.save(user)

        revokeActiveRefreshTokens(userId)
    }

    private fun revokeActiveRefreshTokens(userId: UUID) {
        val activeTokens = refreshTokenRepository.findAllActiveByUserId(userId)
        activeTokens.forEach { it.revoke() }
        refreshTokenRepository.saveAll(activeTokens)
    }
}
