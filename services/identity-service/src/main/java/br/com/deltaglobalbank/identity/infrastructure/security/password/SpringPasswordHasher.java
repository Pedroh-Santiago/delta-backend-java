package br.com.deltaglobalbank.identity.infrastructure.security.password;

import br.com.deltaglobalbank.identity.domain.shared.PasswordHasher;
import br.com.deltaglobalbank.identity.domain.user.HashedPassword;
import br.com.deltaglobalbank.identity.domain.user.Password;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SpringPasswordHasher implements PasswordHasher {

    private final PasswordEncoder passwordEncoder;

    public SpringPasswordHasher(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public HashedPassword hash(Password password) {
        return new HashedPassword(passwordEncoder.encode(password.value()));
    }

    @Override
    public boolean matches(String rawPassword, HashedPassword hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword.value());
    }
}
