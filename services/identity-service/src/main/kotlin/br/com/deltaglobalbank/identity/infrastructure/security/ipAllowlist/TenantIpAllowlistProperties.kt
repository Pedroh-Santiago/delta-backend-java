package br.com.deltaglobalbank.identity.infrastructure.security.ipAllowlist

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "identity.tenant-ip-allowlist")
data class TenantIpAllowlistProperties(
    val cacheTtlSeconds: Long = 300
) {
    init{
        require(cacheTtlSeconds in 1..3600){
            "identity.tenant-ip-allowlist.cache-ttl-seconds deve estar entre 1 e 3600"
        }
    }
}