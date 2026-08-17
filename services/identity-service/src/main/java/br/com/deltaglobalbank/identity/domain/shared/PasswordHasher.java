package br.com.deltaglobalbank.identity.domain.shared;

import br.com.deltaglobalbank.identity.domain.user.HashedPassword;
import br.com.deltaglobalbank.identity.domain.user.Password;

public interface PasswordHasher {
    HashedPassword hash(Password password);

    boolean matches(String rawPassword, HashedPassword hashedPassword);
}
