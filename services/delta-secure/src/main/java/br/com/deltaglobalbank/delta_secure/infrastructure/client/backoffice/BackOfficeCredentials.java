package br.com.deltaglobalbank.delta_secure.infrastructure.client.backoffice;

import org.springframework.boot.context.properties.bind.DefaultValue;

public record BackOfficeCredentials(
    @DefaultValue("") String username,
    @DefaultValue("") String password
) {
}
