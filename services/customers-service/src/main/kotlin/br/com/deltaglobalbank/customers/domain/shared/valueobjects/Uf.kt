package br.com.deltaglobalbank.customers.domain.shared.valueobjects

@JvmInline
value class Uf private constructor(val value: String) {
    companion object {
        operator fun invoke(raw: String): Uf {
            val uf = raw.trim().uppercase()
            require(REGEX.matches(uf)) { "uf_invalid" }
            return Uf(uf)
        }
        private val REGEX = Regex("^[A-Z]{2}$")
    }
}