package br.com.deltaglobalbank.identity.domain.shared

import br.com.deltaglobalbank.identity.domain.user.HashedPassword
import br.com.deltaglobalbank.identity.domain.user.Password

interface PasswordHasher {
    fun hash(password: Password): HashedPassword
    fun matches(rawPassword: String, hashedPassword: HashedPassword): Boolean
}
