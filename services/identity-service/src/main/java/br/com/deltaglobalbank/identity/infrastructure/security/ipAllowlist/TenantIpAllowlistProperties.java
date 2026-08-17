package br.com.deltaglobalbank.identity.infrastructure.security.ipAllowlist;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "identity.tenant-ip-allowlist")
public record TenantIpAllowlistProperties(
    @DefaultValue("300") long cacheTtlSeconds
) {
    public TenantIpAllowlistProperties {
        if (cacheTtlSeconds < 1 || cacheTtlSeconds > 3600) {
            throw new IllegalArgumentException(
                "identity.tenant-ip-allowlist.cache-ttl-seconds deve estar entre 1 e 3600");
        }
    }
}
