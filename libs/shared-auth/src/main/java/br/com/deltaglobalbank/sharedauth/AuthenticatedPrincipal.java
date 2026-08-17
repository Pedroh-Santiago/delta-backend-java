package br.com.deltaglobalbank.sharedauth;

import java.util.List;
import java.util.UUID;

public record AuthenticatedPrincipal(
    UUID subject,
    UUID tenantId,
    String principalType,
    List<String> roles,
    List<String> modules,
    boolean mustChangePassword,
    UUID jti
) {
    public boolean isUser() {
        return "user".equals(principalType);
    }

    public boolean isApiClient() {
        return "api_client".equals(principalType);
    }
}
