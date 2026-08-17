package br.com.deltaglobalbank.identity.features.users.deleteUser;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.token.AccessTokenRevoker;
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRevoker;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteUserUseCase.class);

    private final AccessTokenRevoker accessTokenRevoker;
    private final RefreshTokenRevoker refreshTokenRevoker;
    private final UserRepository userRepository;

    public DeleteUserUseCase(
        AccessTokenRevoker accessTokenRevoker,
        RefreshTokenRevoker refreshTokenRevoker,
        UserRepository userRepository
    ) {
        this.accessTokenRevoker = accessTokenRevoker;
        this.refreshTokenRevoker = refreshTokenRevoker;
        this.userRepository = userRepository;
    }

    @Transactional
    public void deleteUser(UUID userId, UUID tenantId, UUID actorId) {
        User userExists = userRepository.findById(userId);
        if (userExists == null) {
            return;
        }
        if (!userExists.getTenantId().equals(tenantId)) {
            return;
        }
        userRepository.delete(userExists);
        accessTokenRevoker.revokeUser(userId);
        refreshTokenRevoker.revokeAllForUser(userId);
        log.info("user deleted userId={} tenantId={} by={}", userId, tenantId, actorId);
    }
}
