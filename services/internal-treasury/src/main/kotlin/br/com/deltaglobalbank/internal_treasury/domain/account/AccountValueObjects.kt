package br.com.deltaglobalbank.internal_treasury.domain.account

@JvmInline
value class Cpf(val value: String) {
    init {
        require(value.isNotBlank()) { "Cpf_notBlank" }
        require(value.length == MAX_LENGTH) { "Cpf_length is not $MAX_LENGTH" }
        require(value.all { it.isDigit() }) { "Cpf_onlyDigits" }
        require(isValid(value)) { "Cpf_invalid" }
    }

    companion object {
        const val MAX_LENGTH = 11
        private fun calculateDigit(cpfPart: String, initialWeight: Int): Int {
            var sum = 0
            var weight = initialWeight

            for (digit in cpfPart) {
                sum += digit.digitToInt() * weight
                weight--
            }

            val remainder = sum % 11
            return if (remainder < 2) 0 else 11 - remainder
        }

        private fun isValid(cpf: String): Boolean {

            if (cpf.all { it == cpf[0] }) return false

            val firstDigit = calculateDigit(cpf.substring(0,9), 10)
            val secondDigit = calculateDigit(cpf.substring(0,10), 11)

            return firstDigit == cpf[9].digitToInt() &&
                    secondDigit == cpf[10].digitToInt()
        }
    }
}
