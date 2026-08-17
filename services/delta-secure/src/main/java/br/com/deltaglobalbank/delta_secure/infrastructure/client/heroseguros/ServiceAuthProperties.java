package br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "heroseguros.oauth")
public record ServiceAuthProperties(
    Map<String, ConvenioCredentials> convenios
) {
    public ServiceAuthProperties {
        if (convenios == null) {
            convenios = Map.of();
        }
    }
}
