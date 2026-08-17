package br.com.deltaglobalbank.identity.features.users.resetPassword;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.token.AccessTokenRevoker;
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRevoker;
import br.com.deltaglobalbank.identity.domain.user.CannotResetOwnPasswordException;
import br.com.deltaglobalbank.identity.domain.user.Password;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserNotFound;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import br.com.deltaglobalbank.identity.infrastructure.security.password.SpringPasswordHasher;
import br.com.deltaglobalbank.identity.infrastructure.security.password.TemporaryPasswordGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResetPasswordUseCase {

    private final UserRepository userRepository;
    private final TemporaryPasswordGenerator temporaryPasswordGenerator;
    private final SpringPasswordHasher passwordHasher;
    private final AccessTokenRevoker accessTokenRevoker;
    private final RefreshTokenRevoker refreshTokenRevoker;

    public ResetPasswordUseCase(
        UserRepository userRepository,
        TemporaryPasswordGenerator temporaryPasswordGenerator,
        SpringPasswordHasher passwordHasher,
        AccessTokenRevoker accessTokenRevoker,
        RefreshTokenRevoker refreshTokenRevoker
    ) {
        this.userRepository = userRepository;
        this.temporaryPasswordGenerator = temporaryPasswordGenerator;
        this.passwordHasher = passwordHasher;
        this.accessTokenRevoker = accessTokenRevoker;
        this.refreshTokenRevoker = refreshTokenRevoker;
    }

    @Transactional
    public ResetPasswordResponse execute(UUID userId, UUID tenantId, UUID updatedBy) {
        if (updatedBy.equals(userId)) {
            throw new CannotResetOwnPasswordException();
        }

        User user = userRepository.findById(userId);
        if (user == null) {
            throw new UserNotFound();
        }
        if (!user.getTenantId().equals(tenantId)) {
            throw new UserNotFound();
        }

        String tempPassword = temporaryPasswordGenerator.generatePassword();
        user.resetPassword(passwordHasher.hash(new Password(tempPassword)));
        userRepository.save(user);

        accessTokenRevoker.revokeUser(userId);
        refreshTokenRevoker.revokeAllForUser(userId);

        return new ResetPasswordResponse(
            userId,
            user.getEmail().value(),
            tempPassword,
            true,
            user.snapshot().updatedAt()
        );
    }
}
