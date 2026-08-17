package br.com.deltaglobalbank.identity.domain.user

@JvmInline
value class Email private constructor(val value: String) {

    companion object {
        operator fun invoke(rawEmail: String): Email {
            val normalizedEmail = rawEmail.trim().lowercase()
            require(normalizedEmail.isNotBlank()) { "email_blank" }
            require(normalizedEmail.length <= MAX_LENGTH) { "email_too_long" }
            require(EMAIL_REGEX.matches(normalizedEmail)) { "email_invalid" }
            return Email(normalizedEmail)
        }

        const val MAX_LENGTH = 255
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }

    override fun toString(): String = value
}
@JvmInline
value class Password(val value: String) {
    init {
        require(value.length >= MIN_LENGTH) { "password_too_short" }
        require(value.any { it.isLetter() }) { "password_missing_letter" }
        require(value.any { it.isDigit() }) { "password_missing_digit" }
    }

    override fun toString(): String = "Password(***)"

    companion object {
        const val MIN_LENGTH = 8
    }
}

@JvmInline
value class HashedPassword(val value: String) {
    init {
        require(value.isNotBlank()) { "hashed_password_blank" }
    }

    override fun toString(): String = "HashedPassword(***)"
}
