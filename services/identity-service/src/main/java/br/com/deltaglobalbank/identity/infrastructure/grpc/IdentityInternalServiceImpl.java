package br.com.deltaglobalbank.identity.infrastructure.grpc;

import br.com.deltaglobalbank.identity.features.exchangeApiKey.ApiClientSuspendedForExchangeException;
import br.com.deltaglobalbank.identity.features.exchangeApiKey.ApiKeyRequiredException;
import br.com.deltaglobalbank.identity.features.exchangeApiKey.ExchangeApiKeyCommand;
import br.com.deltaglobalbank.identity.features.exchangeApiKey.ExchangeApiKeyResult;
import br.com.deltaglobalbank.identity.features.exchangeApiKey.ExchangeApiKeyUseCase;
import br.com.deltaglobalbank.identity.features.exchangeApiKey.InvalidApiKeyException;
import br.com.deltaglobalbank.identity.features.exchangeApiKey.TenantInactiveForExchangeException;
import br.com.deltaglobalbank.identity.grpc.ExchangeApiKeyRequest;
import br.com.deltaglobalbank.identity.grpc.ExchangeApiKeyResponse;
import br.com.deltaglobalbank.identity.grpc.IdentityInternalServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IdentityInternalServiceImpl extends IdentityInternalServiceGrpc.IdentityInternalServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(IdentityInternalServiceImpl.class);

    private final ExchangeApiKeyUseCase exchangeApiKeyUseCase;

    public IdentityInternalServiceImpl(ExchangeApiKeyUseCase exchangeApiKeyUseCase) {
        this.exchangeApiKeyUseCase = exchangeApiKeyUseCase;
    }

    @Override
    public void exchangeApiKey(ExchangeApiKeyRequest request, StreamObserver<ExchangeApiKeyResponse> responseObserver) {
        long startedAtNanos = System.nanoTime();
        String rawKeyPrefix = request.getApiKey().length() > 12
            ? request.getApiKey().substring(0, 12)
            : request.getApiKey();
        String keyPrefix = rawKeyPrefix.isBlank() ? "n/a" : rawKeyPrefix;

        try {
            ExchangeApiKeyResult result = exchangeApiKeyUseCase.execute(
                new ExchangeApiKeyCommand(request.getApiKey(), request.getSourceIp(), request.getUserAgent())
            );

            ExchangeApiKeyResponse response = ExchangeApiKeyResponse.newBuilder()
                .setInternalToken(result.internalToken())
                .setExpiresAtEpochSeconds(result.expiresAtEpochSeconds())
                .setPrincipalId(result.principalId())
                .setTenantId(result.tenantId())
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logCall("ok", keyPrefix, request.getSourceIp(), result.principalId(), result.tenantId(),
                System.nanoTime() - startedAtNanos);

        } catch (ApiKeyRequiredException ex) {
            sendError(responseObserver, Status.INVALID_ARGUMENT, "api_key_required");
            logFailure("api_key_required", keyPrefix, request.getSourceIp(), startedAtNanos);
        } catch (InvalidApiKeyException ex) {
            sendError(responseObserver, Status.UNAUTHENTICATED, "invalid_api_key");
            logFailure("invalid_api_key", keyPrefix, request.getSourceIp(), startedAtNanos);
        } catch (ApiClientSuspendedForExchangeException ex) {
            sendError(responseObserver, Status.PERMISSION_DENIED, "api_client_suspended");
            logFailure("api_client_suspended", keyPrefix, request.getSourceIp(), startedAtNanos);
        } catch (TenantInactiveForExchangeException ex) {
            sendError(responseObserver, Status.PERMISSION_DENIED, "tenant_inactive");
            logFailure("tenant_inactive", keyPrefix, request.getSourceIp(), startedAtNanos);
        } catch (Exception ex) {
            log.error("ExchangeApiKey erro inesperado: keyPrefix={} ip={}", keyPrefix, request.getSourceIp(), ex);
            sendError(responseObserver, Status.INTERNAL, "internal_error");
        }
    }

    private void sendError(StreamObserver<?> observer, Status status, String code) {
        observer.onError(status.withDescription(code).asRuntimeException());
    }

    private void logCall(
        String outcome,
        String keyPrefix,
        String sourceIp,
        String principalId,
        String tenantId,
        long elapsedNanos
    ) {
        long elapsedMs = elapsedNanos / 1_000_000;
        log.info(
            "exchange_api_key outcome={} key_prefix={} source_ip={} principal_id={} tenant_id={} elapsed_ms={}",
            outcome, keyPrefix, sourceIp.isBlank() ? "n/a" : sourceIp, principalId, tenantId, elapsedMs
        );
    }

    private void logFailure(String outcome, String keyPrefix, String sourceIp, long startedAtNanos) {
        long elapsedMs = (System.nanoTime() - startedAtNanos) / 1_000_000;
        log.warn(
            "exchange_api_key outcome={} key_prefix={} source_ip={} elapsed_ms={}",
            outcome, keyPrefix, sourceIp.isBlank() ? "n/a" : sourceIp, elapsedMs
        );
    }
}
