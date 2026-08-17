package br.com.deltaglobalbank.delta_secure.features.sworks;

public final class SWorksInvalidDocument extends SWorksException {
    public SWorksInvalidDocument(String reason) {
        super(reason);
    }

    public SWorksInvalidDocument(String reason, Throwable cause) {
        super(reason, cause);
    }
}
