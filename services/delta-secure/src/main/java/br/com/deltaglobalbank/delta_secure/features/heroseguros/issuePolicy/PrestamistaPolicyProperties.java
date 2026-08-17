package br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "heroseguros.prestamista")
public record PrestamistaPolicyProperties(
    Map<String, Integer> typeOfProduct
) {
    public PrestamistaPolicyProperties {
        if (typeOfProduct == null) {
            typeOfProduct = Map.of();
        }
    }
}
