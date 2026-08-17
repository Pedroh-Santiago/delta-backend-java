package br.com.deltaglobalbank.identity.infrastructure.security.exchangeApiKey;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "identity.exchange-api-key")
public record ExchangeApiKeyProperties(
    @DefaultValue("30") long cacheTtlMarginSeconds
) {
    public ExchangeApiKeyProperties {
        if (cacheTtlMarginSeconds < 0 || cacheTtlMarginSeconds > 120) {
            throw new IllegalArgumentException(
                "identity.exchange-api-key.cache-ttl-margin-seconds deve estar entre 0 e 120");
        }
    }
}
