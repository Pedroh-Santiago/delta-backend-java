package br.com.deltaglobalbank.identity.infrastructure.grpc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "identity.grpc")
public record GrpcServerProperties(
    @DefaultValue("9090") int port,
    @DefaultValue("30") long shutdownGraceSeconds
) {
    public GrpcServerProperties {
        if (port != 0 && (port < 1024 || port > 65535)) {
            throw new IllegalArgumentException("identity.grpc.port deve ser 0 (porta aleatória) ou estar entre 1024 e 65535");
        }
    }
}
