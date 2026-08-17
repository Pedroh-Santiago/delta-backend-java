package br.com.deltaglobalbank.identity.infrastructure.security.token

import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

data class GeneratedRefreshToken(
    val plainText: String,
    val hash: String
)

@Component
class RefreshTokenGenerator {

    private val secureRandom = SecureRandom()

    fun generate(): GeneratedRefreshToken {
        val bytes = ByteArray(32)  // 256 bits de entropia
        secureRandom.nextBytes(bytes)
        val plainText = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        val hash = sha256(plainText)
        return GeneratedRefreshToken(plainText, hash)
    }

    fun hash(plainText: String): String = sha256(plainText)

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}