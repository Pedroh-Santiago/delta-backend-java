package br.com.deltaglobalbank.identity.features.users.createUser;

import br.com.deltaglobalbank.identity.domain.user.Password;

public record CreateUserResponse(
    CreatedUser user,
    Password temporaryPassword
) {
}
