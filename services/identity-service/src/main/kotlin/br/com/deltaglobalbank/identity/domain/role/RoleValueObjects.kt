package br.com.deltaglobalbank.identity.domain.role

@JvmInline
value class RoleCode(val value: String) {
    init {
        require(value.isNotBlank()) { "role_code_blank" }
        require(value.length <= MAX_LENGTH) { "role_code_too_long" }
    }

    override fun toString(): String = value

    companion object {
        const val MAX_LENGTH = 100
    }
}
