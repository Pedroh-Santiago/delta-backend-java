package br.com.deltaglobalbank.identity.domain.apiKey;

public final class ApiKeyNotFoundException extends ApiKeyDomainException {

    public ApiKeyNotFoundException() {
        super("api_key_not_found");
    }
}
