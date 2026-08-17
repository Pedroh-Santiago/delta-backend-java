package br.com.deltaglobalbank.products.infrastructure.grpc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "products.grpc")
public record GrpcServerProperties(
    @DefaultValue("9092") int port,
    @DefaultValue("30") long shutdownGraceSeconds
) {
    public GrpcServerProperties {
        if (port < 1024 || port > 65535) {
            throw new IllegalArgumentException("products.grpc.port deve estar entre 1024 e 65535");
        }
    }
}
