package br.com.deltaglobalbank.identity.features.users.me;

public final class PrincipalNotFoundException extends MeException {
    public PrincipalNotFoundException() {
        super("principal_not_found");
    }
}
