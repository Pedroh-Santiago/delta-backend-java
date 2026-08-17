package br.com.deltaglobalbank.identity.features.auth.logout;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.token.AccessTokenRevoker;
import br.com.deltaglobalbank.identity.domain.token.MissingRefreshTokenException;
import br.com.deltaglobalbank.identity.domain.token.RefreshToken;
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRepository;
import br.com.deltaglobalbank.identity.infrastructure.security.token.RefreshTokenGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutUseCase {

    private final AccessTokenRevoker accessTokenRevoker;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final RefreshTokenRepository refreshTokenRepository;

    public LogoutUseCase(
        AccessTokenRevoker accessTokenRevoker,
        RefreshTokenGenerator refreshTokenGenerator,
        RefreshTokenRepository refreshTokenRepository
    ) {
        this.accessTokenRevoker = accessTokenRevoker;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public void logout(UUID userId, UUID jti, String refreshToken, boolean allSessions) {
        if (allSessions) {
            revokeAllSessions(userId);
            accessTokenRevoker.revokeUser(userId);
            return;
        }
        if (refreshToken == null) {
            throw new MissingRefreshTokenException();
        }
        revokeSession(userId, refreshToken);
        accessTokenRevoker.revokeJti(jti);
    }

    private void revokeSession(UUID userId, String token) {
        String hash = refreshTokenGenerator.hash(token);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash);
        if (refreshToken == null) {
            return;
        }
        if (!refreshToken.getUserId().equals(userId)) {
            return;
        }
        if (refreshToken.isRevoked()) {
            return;
        }
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);
    }

    private void revokeAllSessions(UUID userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllActiveByUserId(userId);
        tokens.forEach(RefreshToken::revoke);
        refreshTokenRepository.saveAll(tokens);
    }
}
