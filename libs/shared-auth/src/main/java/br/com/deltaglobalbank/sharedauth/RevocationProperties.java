package br.com.deltaglobalbank.sharedauth;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "identity.security")
public record RevocationProperties(
    @DefaultValue({"POST", "PUT", "PATCH", "DELETE"}) List<String> revocationCheckOn,
    @DefaultValue("true") boolean failOpenOnRedisError
) {
}
