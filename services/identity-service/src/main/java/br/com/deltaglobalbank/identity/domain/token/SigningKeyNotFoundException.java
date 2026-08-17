package br.com.deltaglobalbank.identity.domain.token;

public class SigningKeyNotFoundException extends RuntimeException {

    public SigningKeyNotFoundException() {
        super("signing_key_not_found");
    }
}
