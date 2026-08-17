package br.com.deltaglobalbank.sharedauth

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.source.JWKSourceBuilder
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import java.net.URL
import java.time.Instant
import java.util.UUID

class JwtValidator(private val properties: JwtValidationProperties) {

    private val jwkSource = JWKSourceBuilder.create<SecurityContext>(URL(properties.jwksUrl)).build()
    private val keySelector = JWSVerificationKeySelector<SecurityContext>(JWSAlgorithm.RS256, jwkSource)

    fun validate(token: String): ValidatedJwt {
        val processor = DefaultJWTProcessor<SecurityContext>().apply {
            jwsKeySelector = keySelector
        }

        val claims = try {
            processor.process(token, null)
        } catch (ex: Exception) {
            throw JwtValidationException("Token inválido: ${ex.message}", ex)
        }

        if (properties.expectedIssuer.isNotEmpty() && claims.issuer != properties.expectedIssuer) {
            throw JwtValidationException("Issuer inválido: ${claims.issuer}")
        }

        if (properties.expectedAudience.isNotEmpty() && !claims.audience.contains(properties.expectedAudience)) {
            throw JwtValidationException("Audience inválida")
        }

        return extractValidatedJwt(claims)
    }

    private fun extractValidatedJwt(claims: JWTClaimsSet): ValidatedJwt {
        return ValidatedJwt(
            jti = UUID.fromString(claims.jwtid),
            subject = UUID.fromString(claims.subject),
            tenantId = UUID.fromString(claims.getStringClaim("tenant_id")),
            principalType = claims.getStringClaim("principal_type"),
            roles = claims.getStringListClaim("roles") ?: emptyList(),
            modules = claims.getStringListClaim("modules") ?: emptyList(),
            mustChangePassword = (claims.getClaim("must_change_password") as? Boolean) ?: false,
            expiresAt = claims.expirationTime.toInstant(),
            issuedAt = claims.issueTime.toInstant()
        )
    }
}
