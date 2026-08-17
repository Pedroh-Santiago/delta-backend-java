package br.com.deltaglobalbank.identity.infrastructure.grpc

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "identity.grpc")
data class GrpcServerProperties(
    val port: Int = 9090,
    val shutdownGraceSeconds: Long = 30
) {
    init {
        require(port in 1024..65535) { "identity.grpc.port deve estar entre 1024 e 65535" }
    }
}