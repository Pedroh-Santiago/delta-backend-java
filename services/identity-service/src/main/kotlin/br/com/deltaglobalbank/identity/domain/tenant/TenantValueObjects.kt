package br.com.deltaglobalbank.identity.domain.tenant

@JvmInline
value class TenantSlug(val value: String) {
    init {
        require(value.length in MIN_LENGTH..MAX_LENGTH) { "invalid_slug_length" }
        require(SLUG_REGEX.matches(value)) { "invalid_slug_format" }
        require(!value.startsWith("-") && !value.endsWith("-")) { "invalid_slug_format" }
    }

    override fun toString(): String = value

    companion object {
        const val MIN_LENGTH = 3
        const val MAX_LENGTH = 100
        private val SLUG_REGEX = Regex("^[a-z0-9-]+$")
    }
}