package br.com.deltaglobalbank.identity.domain.module

@JvmInline
value class ModuleCode(val value: String) {
    init {
        require(value.isNotBlank()) { "module_code_blank" }
        require(value.length <= MAX_LENGTH) { "module_code_too_long" }
    }

    override fun toString(): String = value

    companion object {
        const val MAX_LENGTH = 100
    }
}
