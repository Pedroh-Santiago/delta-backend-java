package br.com.deltaglobalbank.customers.infrastructure.grpc;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class GrpcServerLifecycle {

    private static final Logger log = LoggerFactory.getLogger(GrpcServerLifecycle.class);

    private final GrpcServerProperties properties;
    private final List<BindableService> services;
    private Server server;

    public GrpcServerLifecycle(GrpcServerProperties properties, List<BindableService> services) {
        this.properties = properties;
        this.services = services;
    }

    @PostConstruct
    public void start() throws Exception {
        ServerBuilder<?> builder = ServerBuilder.forPort(properties.port());

        for (BindableService service : services) {
            builder.addService(service);
            log.info("Registrado serviço gRPC: {}", service.getClass().getSimpleName());
        }

        builder.addService(ProtoReflectionService.newInstance());

        server = builder.build().start();
        log.info("gRPC server iniciado na porta {}", properties.port());
    }

    @PreDestroy
    public void stop() throws InterruptedException {
        if (server != null) {
            log.info("Encerrando gRPC server...");
            server.shutdown();
            if (!server.awaitTermination(properties.shutdownGraceSeconds(), TimeUnit.SECONDS)) {
                log.warn("gRPC server não encerrou dentro do tempo limite; forçando shutdownNow");
                server.shutdownNow();
            }
            log.info("gRPC server encerrado");
        }
    }
}
