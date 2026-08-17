package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

import org.springframework.boot.context.properties.bind.DefaultValue;

public record SWorksCredentials(
    @DefaultValue("") String username,
    @DefaultValue("") String password,
    @DefaultValue("password") String grantType
) {
}
