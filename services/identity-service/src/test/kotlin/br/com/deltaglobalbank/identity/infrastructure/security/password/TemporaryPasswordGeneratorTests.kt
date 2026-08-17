package br.com.deltaglobalbank.identity.infrastructure.security.password

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TemporaryPasswordGeneratorTests {

    private val generator = TemporaryPasswordGenerator()

    @Test
    fun `generated password must have default length of 16`() {
        val password = generator.generatePassword()
        assertEquals(16, password.length)
    }

    @Test
    fun `generated password must respect custom length`() {
        val password = generator.generatePassword(length = 24)
        assertEquals(24, password.length)
    }

    @Test
    fun `generated password must contain at least one digit`() {
        repeat(100) {
            val password = generator.generatePassword()
            assertTrue(password.any { it.isDigit() }, "Password without digit: $password")
        }
    }

    @Test
    fun `generated password must only contain allowed characters`() {
        val allowed = "23456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ".toSet()
        repeat(100) {
            val password = generator.generatePassword()
            assertTrue(password.all { it in allowed }, "Password with invalid char: $password")
        }
    }
}
