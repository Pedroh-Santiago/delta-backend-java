package br.com.deltaglobalbank.identity.domain.token;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class RefreshTokenRevoker {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenRevoker(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public void revokeAllForUser(UUID userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllActiveByUserId(userId);
        tokens.forEach(RefreshToken::revoke);
        refreshTokenRepository.saveAll(tokens);
    }
}
