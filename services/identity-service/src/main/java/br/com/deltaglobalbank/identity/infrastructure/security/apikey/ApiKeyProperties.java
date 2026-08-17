package br.com.deltaglobalbank.identity.infrastructure.security.apikey;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "identity.api-key")
public record ApiKeyProperties(
    String environment,
    String fingerprintSecret,
    String brand
) {
    public ApiKeyProperties {
        if (!Set.of("live", "test").contains(environment)) {
            throw new IllegalArgumentException(
                "identity.api-key.environment deve ser 'live' ou 'test', recebeu '" + environment + "'");
        }
        if (fingerprintSecret.length() < 16) {
            throw new IllegalArgumentException(
                "identity.api-key.fingerprint-secret precisa ter pelo menos 16 caracteres");
        }
        if (!brand.matches("^[a-z]{2,5}$")) {
            throw new IllegalArgumentException("identity.api-key.brand deve ter 2-5 chars minúsculos");
        }
    }
}
