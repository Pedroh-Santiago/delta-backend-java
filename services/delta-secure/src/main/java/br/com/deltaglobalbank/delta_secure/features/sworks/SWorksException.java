package br.com.deltaglobalbank.delta_secure.features.sworks;

public abstract sealed class SWorksException extends RuntimeException
    permits
        SWorksInvalidDocument,
        SWorksAuthenticationFailed,
        SWorksNotConfigured,
        SWorksInvalidResponse,
        SWorksTokenExpired,
        SWorksAccessDenied,
        SWorksProcessNotFound,
        SWorksDocumentRejected,
        SWorksUnavailable {

    protected SWorksException(String message) {
        super(message);
    }

    protected SWorksException(String message, Throwable cause) {
        super(message, cause);
    }
}
