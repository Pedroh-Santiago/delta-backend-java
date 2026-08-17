package br.com.deltaglobalbank.customers.domain.customer.valueobjects

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