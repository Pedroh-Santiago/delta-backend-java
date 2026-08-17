package br.com.deltaglobalbank.delta_secure.infrastructure.client.backoffice;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "backoffice")
public record BackOfficeProperties(
    @DefaultValue("") String baseUrl,
    BackOfficeCredentials auth
) {
    public BackOfficeProperties {
        if (auth == null) {
            auth = new BackOfficeCredentials("", "");
        }
    }
}
