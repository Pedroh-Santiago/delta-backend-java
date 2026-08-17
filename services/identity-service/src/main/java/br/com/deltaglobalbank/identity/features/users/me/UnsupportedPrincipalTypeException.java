package br.com.deltaglobalbank.identity.features.users.me;

public final class UnsupportedPrincipalTypeException extends MeException {

    private final String type;

    public UnsupportedPrincipalTypeException(String type) {
        super("unsupported_principal_type");
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
