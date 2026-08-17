package br.com.deltaglobalbank.customers.infrastructure.grpc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "customers.grpc")
public record GrpcServerProperties(
    @DefaultValue("9091") int port,
    @DefaultValue("30") long shutdownGraceSeconds
) {
    public GrpcServerProperties {
        if (port < 1024 || port > 65535) {
            throw new IllegalArgumentException("customers.grpc.port deve estar entre 1024 e 65535");
        }
    }
}
