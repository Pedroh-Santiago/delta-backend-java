package br.com.deltaglobalbank.identity.domain.apiKey;

public final class ApiClientNotFoundException extends ApiKeyDomainException {

    public ApiClientNotFoundException() {
        super("api_client_not_found");
    }
}
