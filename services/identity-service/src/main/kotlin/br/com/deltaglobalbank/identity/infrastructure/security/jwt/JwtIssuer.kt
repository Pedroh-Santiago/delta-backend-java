package br.com.deltaglobalbank.identity.infrastructure.security.jwt

import com.github.f4b6a3.uuid.UuidCreator
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Date
import java.util.UUID

data class IssuedJwt(
    val token: String,
    val jti: UUID,
    val issuedAt: Instant,
    val expiresAt: Instant
)

data class UserClaims(
    val userId: UUID,
    val tenantId: UUID,
    val roles: List<String>,
    val modules: List<String>,
    val mustChangePassword: Boolean
)

data class ApiClientClaims(
    val apiClientId: UUID,
    val tenantId: UUID,
    val roles: List<String>,
    val modules: List<String>
)

@Component
class JwtIssuer(
    private val keyManager: KeyManager,

    private val jwtProperties: JwtIssuerProperties
) {

    fun issueForUser(claims: UserClaims): IssuedJwt {
        val signingKey = keyManager.getActiveSigningKey()
        val now = Instant.now()
        val expiresAt = now.plus(jwtProperties.accessTokenTtl)
        val jti = UuidCreator.getTimeOrderedEpoch()

        val token = Jwts.builder()
            .header()
            .keyId(signingKey.kid)
            .type("JWT")
            .and()
            .issuer(jwtProperties.issuer)
            .audience().add(jwtProperties.audience).and()
            .subject(claims.userId.toString())
            .id(jti.toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .claim("tenant_id", claims.tenantId.toString())
            .claim("principal_type", "user")
            .claim("roles", claims.roles)
            .claim("modules", claims.modules)
            .claim("must_change_password", claims.mustChangePassword)
            .signWith(signingKey.privateKey, Jwts.SIG.RS256)
            .compact()

        return IssuedJwt(
            token = token,
            jti = jti,
            issuedAt = now,
            expiresAt = expiresAt
        )
    }

    fun issueForApiClient(claims: ApiClientClaims): IssuedJwt {
        val signingKey = keyManager.getActiveSigningKey()
        val now = Instant.now()
        val expiresAt = now.plus(jwtProperties.accessTokenTtl)
        val jti = UuidCreator.getTimeOrderedEpoch()

        val token = Jwts.builder()
            .header()
            .keyId(signingKey.kid)
            .type("JWT")
            .and()
            .issuer(jwtProperties.issuer)
            .audience().add(jwtProperties.audience).and()
            .subject(claims.apiClientId.toString())
            .id(jti.toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .claim("tenant_id", claims.tenantId.toString())
            .claim("principal_type", "api_client")
            .claim("roles", claims.roles)
            .claim("modules", claims.modules)
            .signWith(signingKey.privateKey, Jwts.SIG.RS256)
            .compact()

        return IssuedJwt(
            token = token,
            jti = jti,
            issuedAt = now,
            expiresAt = expiresAt
        )
    }
}