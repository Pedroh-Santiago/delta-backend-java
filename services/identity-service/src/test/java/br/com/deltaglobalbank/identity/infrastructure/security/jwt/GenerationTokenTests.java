package br.com.deltaglobalbank.identity.infrastructure.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GenerationTokenTests {

    private KeyManager keyManager;
    private JwtIssuerProperties jwtProperties;
    private JwtIssuer jwtIssuer;
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        publicKey = (RSAPublicKey) keyPair.getPublic();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();

        keyManager = mock(KeyManager.class);
        when(keyManager.getActiveSigningKey()).thenReturn(new SigningKeyMaterial("test", privateKey, publicKey, "ACTIVE"));

        jwtProperties = new JwtIssuerProperties("delta", "delta-api", Duration.ofSeconds(900), Duration.ofDays(7));

        jwtIssuer = new JwtIssuer(keyManager, jwtProperties);
    }

    @Test
    void mustWriteAllUserClaimsIntoTheSignedJwt() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UserClaims claims = new UserClaims(
            userId, tenantId, List.of("customers.admin", "platform.admin"), List.of("customers"), true);

        IssuedJwt issued = jwtIssuer.issueForUser(claims);

        Claims payload = Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(issued.token())
            .getPayload();

        Assertions.assertAll(
            () -> assertEquals(userId.toString(), payload.getSubject()),
            () -> assertEquals(tenantId.toString(), payload.get("tenant_id")),
            () -> assertEquals("user", payload.get("principal_type")),
            () -> assertEquals(List.of("customers.admin", "platform.admin"), payload.get("roles")),
            () -> assertEquals(List.of("customers"), payload.get("modules")),
            () -> assertEquals(true, payload.get("must_change_password")),
            () -> assertEquals(issued.jti().toString(), payload.getId())
        );
    }

    @Test
    void mustRejectTokenVerifiedWithWrongPublicKey() throws Exception {
        UserClaims claims = new UserClaims(
            UUID.randomUUID(), UUID.randomUUID(), List.of("customers.admin"), List.of("customers"), false);

        IssuedJwt issued = jwtIssuer.issueForUser(claims);

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        RSAPublicKey wrongPublicKey = (RSAPublicKey) generator.generateKeyPair().getPublic();

        Assertions.assertThrows(SignatureException.class, () -> Jwts.parser()
            .verifyWith(wrongPublicKey)
            .build()
            .parseSignedClaims(issued.token()));
    }

    @Test
    void mustSetExpirationAccordingToAccessTokenTtl() {
        UserClaims claims = new UserClaims(
            UUID.randomUUID(), UUID.randomUUID(), List.of(), List.of(), false);

        IssuedJwt issued = jwtIssuer.issueForUser(claims);

        Claims payload = Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(issued.token())
            .getPayload();

        Instant issuedAt = payload.getIssuedAt().toInstant();
        Instant expiration = payload.getExpiration().toInstant();

        long ttlSeconds = Duration.between(issuedAt, expiration).getSeconds();
        assertEquals(900, ttlSeconds);
    }
}
