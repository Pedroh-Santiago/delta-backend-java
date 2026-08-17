package br.com.deltaglobalbank.identity.infrastructure.security.cors;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "identity.cors")
public record CorsProperties(
    @DefaultValue({}) List<String> allowedOrigins,
    @DefaultValue({"GET", "POST", "PUT", "PATCH", "DELETE"}) List<String> allowedMethods,
    @DefaultValue("*") List<String> allowedHeaders,
    @DefaultValue("true") boolean allowCredentials,
    @DefaultValue("3600") long maxAge
) {
}
