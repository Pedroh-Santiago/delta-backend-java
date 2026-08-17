package br.com.deltaglobalbank.identity.features.users.suspendUser;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.token.AccessTokenRevoker;
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRevoker;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserNotFound;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SuspendUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(SuspendUserUseCase.class);

    private final RefreshTokenRevoker refreshTokenRevoker;
    private final UserRepository userRepository;
    private final AccessTokenRevoker accessTokenRevoker;

    public SuspendUserUseCase(
        RefreshTokenRevoker refreshTokenRevoker,
        UserRepository userRepository,
        AccessTokenRevoker accessTokenRevoker
    ) {
        this.refreshTokenRevoker = refreshTokenRevoker;
        this.userRepository = userRepository;
        this.accessTokenRevoker = accessTokenRevoker;
    }

    @Transactional
    public void suspendUser(UUID userId, UUID tenantId, UUID actorId) {
        User userExists = userRepository.findById(userId);
        if (userExists == null) {
            throw new UserNotFound();
        }
        if (!userExists.getTenantId().equals(tenantId)) {
            throw new UserNotFound();
        }
        userExists.suspend();
        userRepository.save(userExists);
        accessTokenRevoker.revokeUser(userId);
        refreshTokenRevoker.revokeAllForUser(userId);
        log.info("user suspended userId={} tenantId={} by={}", userId, tenantId, actorId);
    }
}
