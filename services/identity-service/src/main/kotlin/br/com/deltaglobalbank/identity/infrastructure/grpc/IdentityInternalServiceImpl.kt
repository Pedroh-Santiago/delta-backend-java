package br.com.deltaglobalbank.identity.infrastructure.grpc

import br.com.deltaglobalbank.identity.features.exchangeApiKey.ApiClientSuspendedForExchangeException
import br.com.deltaglobalbank.identity.features.exchangeApiKey.ApiKeyRequiredException
import br.com.deltaglobalbank.identity.features.exchangeApiKey.ExchangeApiKeyCommand
import br.com.deltaglobalbank.identity.features.exchangeApiKey.ExchangeApiKeyUseCase
import br.com.deltaglobalbank.identity.features.exchangeApiKey.InvalidApiKeyException
import br.com.deltaglobalbank.identity.features.exchangeApiKey.TenantInactiveForExchangeException
import br.com.deltaglobalbank.identity.grpc.ExchangeApiKeyRequest
import br.com.deltaglobalbank.identity.grpc.ExchangeApiKeyResponse
import br.com.deltaglobalbank.identity.grpc.IdentityInternalServiceGrpc
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class IdentityInternalServiceImpl(
    private val exchangeApiKeyUseCase: ExchangeApiKeyUseCase
) : IdentityInternalServiceGrpc.IdentityInternalServiceImplBase() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun exchangeApiKey(
        request: ExchangeApiKeyRequest,
        responseObserver: StreamObserver<ExchangeApiKeyResponse>
    ) {
        val startedAtNanos = System.nanoTime()
        val keyPrefix = request.apiKey.take(12).ifBlank { "n/a" }

        try {
            val result = exchangeApiKeyUseCase.execute(
                ExchangeApiKeyCommand(
                    apiKey = request.apiKey,
                    sourceIp = request.sourceIp,
                    userAgent = request.userAgent
                )
            )

            val response = ExchangeApiKeyResponse.newBuilder()
                .setInternalToken(result.internalToken)
                .setExpiresAtEpochSeconds(result.expiresAtEpochSeconds)
                .setPrincipalId(result.principalId)
                .setTenantId(result.tenantId)
                .build()

            responseObserver.onNext(response)
            responseObserver.onCompleted()

            logCall(
                outcome = "ok",
                keyPrefix = keyPrefix,
                sourceIp = request.sourceIp,
                principalId = result.principalId,
                tenantId = result.tenantId,
                elapsedNanos = System.nanoTime() - startedAtNanos
            )

        } catch (ex: ApiKeyRequiredException) {
            sendError(responseObserver, Status.INVALID_ARGUMENT, "api_key_required", ex)
            logFailure("api_key_required", keyPrefix, request.sourceIp, startedAtNanos)
        } catch (ex: InvalidApiKeyException) {
            sendError(responseObserver, Status.UNAUTHENTICATED, "invalid_api_key", ex)
            logFailure("invalid_api_key", keyPrefix, request.sourceIp, startedAtNanos)
        } catch (ex: ApiClientSuspendedForExchangeException) {
            sendError(responseObserver, Status.PERMISSION_DENIED, "api_client_suspended", ex)
            logFailure("api_client_suspended", keyPrefix, request.sourceIp, startedAtNanos)
        } catch (ex: TenantInactiveForExchangeException) {
            sendError(responseObserver, Status.PERMISSION_DENIED, "tenant_inactive", ex)
            logFailure("tenant_inactive", keyPrefix, request.sourceIp, startedAtNanos)
        } catch (ex: Exception) {
            log.error(
                "ExchangeApiKey erro inesperado: keyPrefix={} ip={}",
                keyPrefix,
                request.sourceIp,
                ex
            )
            sendError(responseObserver, Status.INTERNAL, "internal_error", ex)
        }
    }

    private fun sendError(
        observer: StreamObserver<*>,
        status: Status,
        code: String,
        ex: Exception
    ) {
        observer.onError(
            status
                .withDescription(code)
                .asRuntimeException()
        )
    }

    private fun logCall(
        outcome: String,
        keyPrefix: String,
        sourceIp: String,
        principalId: String,
        tenantId: String,
        elapsedNanos: Long
    ) {
        val elapsedMs = elapsedNanos / 1_000_000
        log.info(
            "exchange_api_key outcome={} key_prefix={} source_ip={} principal_id={} tenant_id={} elapsed_ms={}",
            outcome, keyPrefix, sourceIp.ifBlank { "n/a" }, principalId, tenantId, elapsedMs
        )
    }

    private fun logFailure(
        outcome: String,
        keyPrefix: String,
        sourceIp: String,
        startedAtNanos: Long
    ) {
        val elapsedMs = (System.nanoTime() - startedAtNanos) / 1_000_000
        log.warn(
            "exchange_api_key outcome={} key_prefix={} source_ip={} elapsed_ms={}",
            outcome, keyPrefix, sourceIp.ifBlank { "n/a" }, elapsedMs
        )
    }
}