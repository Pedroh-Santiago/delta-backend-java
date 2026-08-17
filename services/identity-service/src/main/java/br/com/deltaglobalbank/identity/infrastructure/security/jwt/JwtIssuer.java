package br.com.deltaglobalbank.identity.infrastructure.security.jwt;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

@Component
public class JwtIssuer {

    private final KeyManager keyManager;
    private final JwtIssuerProperties jwtProperties;

    public JwtIssuer(KeyManager keyManager, JwtIssuerProperties jwtProperties) {
        this.keyManager = keyManager;
        this.jwtProperties = jwtProperties;
    }

    public IssuedJwt issueForUser(UserClaims claims) {
        SigningKeyMaterial signingKey = keyManager.getActiveSigningKey();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtProperties.accessTokenTtl());
        UUID jti = UuidCreator.getTimeOrderedEpoch();

        String token = Jwts.builder()
            .header()
            .keyId(signingKey.kid())
            .type("JWT")
            .and()
            .issuer(jwtProperties.issuer())
            .audience().add(jwtProperties.audience()).and()
            .subject(claims.userId().toString())
            .id(jti.toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .claim("tenant_id", claims.tenantId().toString())
            .claim("principal_type", "user")
            .claim("roles", claims.roles())
            .claim("modules", claims.modules())
            .claim("must_change_password", claims.mustChangePassword())
            .signWith(signingKey.privateKey(), Jwts.SIG.RS256)
            .compact();

        return new IssuedJwt(token, jti, now, expiresAt);
    }

    public IssuedJwt issueForApiClient(ApiClientClaims claims) {
        SigningKeyMaterial signingKey = keyManager.getActiveSigningKey();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtProperties.accessTokenTtl());
        UUID jti = UuidCreator.getTimeOrderedEpoch();

        String token = Jwts.builder()
            .header()
            .keyId(signingKey.kid())
            .type("JWT")
            .and()
            .issuer(jwtProperties.issuer())
            .audience().add(jwtProperties.audience()).and()
            .subject(claims.apiClientId().toString())
            .id(jti.toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .claim("tenant_id", claims.tenantId().toString())
            .claim("principal_type", "api_client")
            .claim("roles", claims.roles())
            .claim("modules", claims.modules())
            .signWith(signingKey.privateKey(), Jwts.SIG.RS256)
            .compact();

        return new IssuedJwt(token, jti, now, expiresAt);
    }
}
