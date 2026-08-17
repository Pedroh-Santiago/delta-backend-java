package br.com.deltaglobalbank.identity.features.users.updateUser;

import br.com.deltaglobalbank.identity.domain.token.AccessTokenRevoker;
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRevoker;
import br.com.deltaglobalbank.identity.domain.user.Email;
import br.com.deltaglobalbank.identity.domain.user.EmailAlreadyExistsException;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserNotFound;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import br.com.deltaglobalbank.identity.domain.user.UserSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateUserUseCase.class);

    private final UserRepository userRepository;
    private final AccessTokenRevoker accessTokenRevoker;
    private final RefreshTokenRevoker refreshTokenRevoker;

    public UpdateUserUseCase(
        UserRepository userRepository,
        AccessTokenRevoker accessTokenRevoker,
        RefreshTokenRevoker refreshTokenRevoker
    ) {
        this.userRepository = userRepository;
        this.accessTokenRevoker = accessTokenRevoker;
        this.refreshTokenRevoker = refreshTokenRevoker;
    }

    @Transactional
    public UpdateUserResponse execute(UpdateUserCommand command) {
        User user = userRepository.findById(command.userId());
        if (user == null) {
            throw new UserNotFound();
        }

        boolean isPlatformAdmin = command.actorRoles().contains("platform.admin");
        if (!isPlatformAdmin && !user.getTenantId().equals(command.actorTenantId())) {
            throw new UserNotFound();
        }

        Email newEmail = new Email(command.email());
        boolean emailChanged = !newEmail.equals(user.getEmail());

        if (emailChanged && userRepository.existsByEmail(newEmail)) {
            throw new EmailAlreadyExistsException();
        }

        boolean changed = user.updateProfile(command.fullName(), newEmail);

        if (changed) {
            userRepository.save(user);
            if (emailChanged) {
                accessTokenRevoker.revokeUser(user.getId());
                refreshTokenRevoker.revokeAllForUser(user.getId());
            }
        }

        UserSnapshot s = user.snapshot();
        return new UpdateUserResponse(
            s.id(),
            s.fullName(),
            s.email().value(),
            s.tenantId(),
            s.status().toDatabaseValue(),
            s.updatedAt()
        );
    }
}
