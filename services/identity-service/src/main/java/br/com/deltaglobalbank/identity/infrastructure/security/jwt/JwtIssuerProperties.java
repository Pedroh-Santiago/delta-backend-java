package br.com.deltaglobalbank.identity.infrastructure.security.jwt;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "identity.jwt")
public record JwtIssuerProperties(
    @DefaultValue("identity-service") String issuer,
    @DefaultValue("internal") String audience,
    @DefaultValue("PT15M") Duration accessTokenTtl,
    @DefaultValue("P7D") Duration refreshTokenTtl
) {
}
