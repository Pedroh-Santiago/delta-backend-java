package br.com.deltaglobalbank.identity.features.apiClients.createApiClient;

import java.util.List;

import br.com.deltaglobalbank.identity.domain.role.RoleCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateApiClientRequest(
    @NotBlank @Size(min = 3, max = 255) String name,
    @Size(max = 2000) String description,
    List<RoleCode> roleCodes
) {
    public CreateApiClientRequest {
        if (roleCodes == null) {
            roleCodes = List.of();
        }
    }
}
