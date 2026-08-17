package br.com.deltaglobalbank.customers.domain.customer.valueobjects

@JvmInline
value class Cpf private constructor(val value: String) {
    companion object {
        operator fun invoke(value: String): Cpf {
            val digitsOnly = value.filter { it.isDigit() }
            require(digitsOnly.length == 11) { "invalid_cpf" }
            require(digitsOnly.toSet().size > 1) { "invalid_cpf" }
            val digits = digitsOnly.map { it - '0' }
            val firstVerifierDigit  = checkDigit(digits.take(9), 10)
            val secondVerifierDigit = checkDigit(digits.take(10), 11)
            require(firstVerifierDigit  == digits[9] && secondVerifierDigit == digits[10]) { "invalid_cpf" }
            return Cpf(digitsOnly)
        }

        private fun checkDigit(digits: List<Int>, startWeight: Int): Int {
            val weightedSum = digits.mapIndexed { i, digit -> digit * (startWeight - i) }.sum()
            val remainder = weightedSum % 11
            return if (remainder < 2) 0 else 11 - remainder
        }
    }
}
