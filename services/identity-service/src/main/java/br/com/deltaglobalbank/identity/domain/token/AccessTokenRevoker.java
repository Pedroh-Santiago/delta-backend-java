package br.com.deltaglobalbank.identity.domain.token;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class AccessTokenRevoker {

    private final TokenRevocationStore store;
    private final IssuedTokenAuditRepository auditRepository;

    public AccessTokenRevoker(TokenRevocationStore store, IssuedTokenAuditRepository auditRepository) {
        this.store = store;
        this.auditRepository = auditRepository;
    }

    public void revokeJti(UUID jti) {
        IssuedTokenAudit audit = auditRepository.findByJti(jti.toString());
        if (audit == null) {
            return;
        }

        Duration ttl = Duration.between(Instant.now(), audit.getExpiresAt());

        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }

        store.revokeJti(jti.toString(), ttl);
    }

    public void revokeUser(UUID userID) {
        store.revokeUser(userID);
    }
}
