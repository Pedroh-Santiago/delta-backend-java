package br.com.deltaglobalbank.identity.infrastructure.security.password

import br.com.deltaglobalbank.identity.domain.shared.PasswordHasher
import br.com.deltaglobalbank.identity.domain.user.HashedPassword
import br.com.deltaglobalbank.identity.domain.user.Password
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class SpringPasswordHasher(
    private val passwordEncoder: PasswordEncoder
) : PasswordHasher {

    override fun hash(password: Password): HashedPassword {
        return HashedPassword(passwordEncoder.encode(password.value)!!)
    }

    override fun matches(rawPassword: String, hashedPassword: HashedPassword): Boolean {
        return passwordEncoder.matches(rawPassword, hashedPassword.value)
    }
}