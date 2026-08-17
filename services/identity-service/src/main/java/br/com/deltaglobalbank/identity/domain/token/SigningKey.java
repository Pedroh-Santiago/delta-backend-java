package br.com.deltaglobalbank.identity.domain.token;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;

public class SigningKey {

    private final UUID id;
    private final String kid;
    private final String algorithm;
    private final String publicKey;
    private final Instant createdAt;
    private final Instant activatedAt;

    private String privateKey;
    private SigningKeyStatus status;
    private Instant retiredAt;

    public SigningKey(
        UUID id,
        String kid,
        String algorithm,
        String publicKey,
        String privateKey,
        SigningKeyStatus status,
        Instant createdAt,
        Instant activatedAt,
        Instant retiredAt
    ) {
        this.id = id;
        this.kid = kid;
        this.algorithm = algorithm;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.status = status;
        this.createdAt = createdAt;
        this.activatedAt = activatedAt;
        this.retiredAt = retiredAt;
    }

    public UUID getId() {
        return id;
    }

    public String getKid() {
        return kid;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getActivatedAt() {
        return activatedAt;
    }

    public void retire() {
        status = SigningKeyStatus.RETIRED;
        retiredAt = Instant.now();
    }

    public void revoke() {
        status = SigningKeyStatus.REVOKED;
        retiredAt = Instant.now();
    }

    public SigningKeyStatus status() {
        return status;
    }

    public SigningKeySnapshot snapshot() {
        return new SigningKeySnapshot(
            id,
            kid,
            algorithm,
            publicKey,
            privateKey,
            status,
            createdAt,
            activatedAt,
            retiredAt
        );
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof SigningKey signingKey && id.equals(signingKey.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "SigningKey(id=" + id + ", kid=" + kid + ", status=" + status + ")";
    }

    public static SigningKey create(String kid, String algorithm, String publicKey, String privateKey) {
        return new SigningKey(
            UuidCreator.getTimeOrderedEpoch(),
            kid,
            algorithm,
            publicKey,
            privateKey,
            SigningKeyStatus.ACTIVE,
            Instant.now(),
            Instant.now(),
            null
        );
    }
}
