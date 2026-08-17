package br.com.deltaglobalbank.sharedauth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "identity.jwt")
public record JwtValidationProperties(
    @DefaultValue("") String jwksUrl,
    @DefaultValue("identity-service") String expectedIssuer,
    @DefaultValue("internal") String expectedAudience
) {
}
