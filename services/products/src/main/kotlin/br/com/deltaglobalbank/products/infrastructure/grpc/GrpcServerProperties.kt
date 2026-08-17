package br.com.deltaglobalbank.products.infrastructure.grpc

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "products.grpc")
data class GrpcServerProperties(
    val port: Int = 9092,
    val shutdownGraceSeconds: Long = 30
) {
    init {
        require(port in 1024..65535) { "products.grpc.port deve estar entre 1024 e 65535" }
    }
}