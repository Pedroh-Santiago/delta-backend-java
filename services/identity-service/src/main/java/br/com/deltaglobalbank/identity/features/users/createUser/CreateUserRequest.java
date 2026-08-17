package br.com.deltaglobalbank.identity.features.users.createUser;

import java.util.List;

import br.com.deltaglobalbank.identity.domain.role.RoleCode;
import br.com.deltaglobalbank.identity.domain.user.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
    @NotNull Email email,
    @NotBlank String fullName,
    List<RoleCode> roleCodes
) {
    public CreateUserRequest {
        if (roleCodes == null) {
            roleCodes = List.of();
        }
    }
}
