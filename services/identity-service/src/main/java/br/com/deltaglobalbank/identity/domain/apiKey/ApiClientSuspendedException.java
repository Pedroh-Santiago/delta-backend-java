package br.com.deltaglobalbank.identity.domain.apiKey;

public final class ApiClientSuspendedException extends ApiKeyDomainException {

    public ApiClientSuspendedException() {
        super("api_client_suspended");
    }
}
