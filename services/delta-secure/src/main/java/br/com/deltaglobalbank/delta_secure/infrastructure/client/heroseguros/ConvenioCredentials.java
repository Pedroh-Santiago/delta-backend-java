package br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros;

import org.springframework.boot.context.properties.bind.DefaultValue;

public record ConvenioCredentials(
    @DefaultValue("") String grantType,
    @DefaultValue("") String clientId,
    @DefaultValue("") String clientSecret,
    @DefaultValue("") String username,
    @DefaultValue("") String password,
    @DefaultValue("") String scope
) {
}
