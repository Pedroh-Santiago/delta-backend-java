package br.com.deltaglobalbank.identity.features.tokens.revokeToken;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.token.AccessTokenRevoker;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserNotFound;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class RevokeTokenUseCase {

    private final AccessTokenRevoker accessTokenRevoker;
    private final UserRepository userRepository;

    public RevokeTokenUseCase(AccessTokenRevoker accessTokenRevoker, UserRepository userRepository) {
        this.accessTokenRevoker = accessTokenRevoker;
        this.userRepository = userRepository;
    }

    public void revokeJti(UUID jti) {
        accessTokenRevoker.revokeJti(jti);
    }

    public void revokeAllForUser(UUID userId, UUID tenantId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new UserNotFound();
        }
        if (!user.getTenantId().equals(tenantId)) {
            throw new UserNotFound();
        }
        accessTokenRevoker.revokeUser(user.getId());
    }
}
