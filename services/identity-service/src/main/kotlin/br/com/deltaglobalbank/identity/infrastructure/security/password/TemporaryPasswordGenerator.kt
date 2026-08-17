package br.com.deltaglobalbank.identity.infrastructure.security.password

import org.springframework.stereotype.Component
import java.security.SecureRandom
import kotlin.random.asKotlinRandom

@Component
class TemporaryPasswordGenerator {
    private val digits = "23456789"
    private val letters = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ"
    private val all = digits + letters

    fun generatePassword(length: Int = 16): String {
        val rng = SecureRandom().asKotlinRandom()
        val guaranteed = digits.random(rng)
        val rest = (1 until length).map { all.random(rng) }
        return (listOf(guaranteed) + rest).shuffled(rng).joinToString("")
    }
}