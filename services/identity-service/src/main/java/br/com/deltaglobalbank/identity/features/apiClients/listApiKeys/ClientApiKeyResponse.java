package br.com.deltaglobalbank.identity.features.apiClients.listApiKeys;

import java.util.List;

public record ClientApiKeyResponse(
    List<ApiKeyItemsResponse> items
) {
}
