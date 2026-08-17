package br.com.deltaglobalbank.customers.infrastructure.grpc

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "customers.grpc")
data class GrpcServerProperties(
    val port: Int = 9091,
    val shutdownGraceSeconds: Long = 30
) {
    init {
        require(port in 1024..65535) { "customers.grpc.port deve estar entre 1024 e 65535" }
    }
}