package br.com.deltaglobalbank.identity.infrastructure.security.cookie;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "identity.refresh-cookie")
public record RefreshTokenCookieProperties(
    @DefaultValue("refreshToken") String name,
    String domain,
    @DefaultValue("true") boolean secure,
    @DefaultValue("Strict") String sameSite,
    @DefaultValue("/auth") String path
) {
}
