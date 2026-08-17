package br.com.deltaglobalbank.customers.infrastructure.grpc

import io.grpc.BindableService
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import kotlin.collections.forEach

@Component
@Profile("!test")
class GrpcServerLifecycle(
    private val properties: GrpcServerProperties,
    private val services: List<BindableService>
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private lateinit var server: Server

    @PostConstruct
    fun start() {
        val builder = ServerBuilder.forPort(properties.port)

        services.forEach { service ->
            builder.addService(service)
            log.info("Registrado serviço gRPC: {}", service::class.simpleName)
        }

        builder.addService(ProtoReflectionService.newInstance())

        server = builder.build().start()
        log.info("gRPC server iniciado na porta {}", properties.port)
    }

    @PreDestroy
    fun stop() {
        if (this::server.isInitialized) {
            log.info("Encerrando gRPC server...")
            server.shutdown()
            if (!server.awaitTermination(properties.shutdownGraceSeconds, TimeUnit.SECONDS)) {
                log.warn("gRPC server não encerrou dentro do tempo limite; forçando shutdownNow")
                server.shutdownNow()
            }
            log.info("gRPC server encerrado")
        }
    }
}