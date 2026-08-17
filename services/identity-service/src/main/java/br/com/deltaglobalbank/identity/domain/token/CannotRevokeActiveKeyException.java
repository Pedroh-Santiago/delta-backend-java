package br.com.deltaglobalbank.identity.domain.token;

public class CannotRevokeActiveKeyException extends RuntimeException {

    public CannotRevokeActiveKeyException() {
        super("cannot_revoke_active_key");
    }
}
