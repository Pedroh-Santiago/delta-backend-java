package br.com.deltaglobalbank.identity.infrastructure.security.apikey

import org.springframework.stereotype.Component
import java.security.SecureRandom

@Component
class ApiKeyGenerator(private val properties: ApiKeyProperties) {
    private val secureRandom = SecureRandom()
    private val alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    fun generate(): GeneratedApiKey {
        val random = buildString(KEY_RANDOM_LENGTH) {
            repeat(KEY_RANDOM_LENGTH) {
                append(alphabet[secureRandom.nextInt(alphabet.length)])
            }
        }
        val plainKey = "${properties.brand}_${properties.environment}_$random"
        val prefix = plainKey.substring(0, PREFIX_LENGTH)

        return GeneratedApiKey(
            plainKey = plainKey,
            prefix = prefix,
        )
    }

    companion object {
        const val KEY_RANDOM_LENGTH = 32
        const val PREFIX_LENGTH = 13
    }
}

data class GeneratedApiKey(
    val plainKey: String,
    val prefix: String,
)