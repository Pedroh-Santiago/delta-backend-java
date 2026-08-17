package br.com.deltaglobalbank.identity.features.auth.changePassword;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.shared.PasswordHasher;
import br.com.deltaglobalbank.identity.domain.token.RefreshToken;
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRepository;
import br.com.deltaglobalbank.identity.domain.user.CurrentPasswordIncorrectException;
import br.com.deltaglobalbank.identity.domain.user.Password;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordHasher passwordHasher;

    public ChangePasswordUseCase(
        UserRepository userRepository,
        RefreshTokenRepository refreshTokenRepository,
        PasswordHasher passwordHasher
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public void execute(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new CurrentPasswordIncorrectException();
        }

        Password currentPassword = new Password(request.currentPassword());
        Password newPassword = new Password(request.newPassword());

        user.changePassword(currentPassword, newPassword, passwordHasher);
        userRepository.save(user);

        revokeActiveRefreshTokens(userId);
    }

    private void revokeActiveRefreshTokens(UUID userId) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findAllActiveByUserId(userId);
        activeTokens.forEach(RefreshToken::revoke);
        refreshTokenRepository.saveAll(activeTokens);
    }
}
