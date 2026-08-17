package br.com.deltaglobalbank.sharedauth;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

public class JwtValidator {

    private final JwtValidationProperties properties;
    private final JWSVerificationKeySelector<SecurityContext> keySelector;

    public JwtValidator(JwtValidationProperties properties) {
        this.properties = properties;
        JWKSource<SecurityContext> jwkSource =
            JWKSourceBuilder.<SecurityContext>create(toUrl(properties.jwksUrl())).build();
        this.keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource);
    }

    public ValidatedJwt validate(String token) {
        DefaultJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
        processor.setJWSKeySelector(keySelector);

        JWTClaimsSet claims;
        try {
            claims = processor.process(token, null);
        } catch (Exception ex) {
            throw new JwtValidationException("Token inválido: " + ex.getMessage(), ex);
        }

        if (!properties.expectedIssuer().isEmpty()
            && !properties.expectedIssuer().equals(claims.getIssuer())) {
            throw new JwtValidationException("Issuer inválido: " + claims.getIssuer());
        }

        if (!properties.expectedAudience().isEmpty()
            && !claims.getAudience().contains(properties.expectedAudience())) {
            throw new JwtValidationException("Audience inválida");
        }

        return extractValidatedJwt(claims);
    }

    private ValidatedJwt extractValidatedJwt(JWTClaimsSet claims) {
        try {
            List<String> roles = claims.getStringListClaim("roles");
            List<String> modules = claims.getStringListClaim("modules");
            Object mustChange = claims.getClaim("must_change_password");
            return new ValidatedJwt(
                UUID.fromString(claims.getJWTID()),
                UUID.fromString(claims.getSubject()),
                UUID.fromString(claims.getStringClaim("tenant_id")),
                claims.getStringClaim("principal_type"),
                roles != null ? roles : List.of(),
                modules != null ? modules : List.of(),
                mustChange instanceof Boolean b ? b : false,
                claims.getExpirationTime().toInstant(),
                claims.getIssueTime().toInstant()
            );
        } catch (java.text.ParseException ex) {
            throw new JwtValidationException("Claims inválidas: " + ex.getMessage(), ex);
        }
    }

    private static URL toUrl(String value) {
        try {
            return new URL(value);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("jwksUrl inválida: " + value, ex);
        }
    }
}
