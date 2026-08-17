package br.com.deltaglobalbank.identity.infrastructure.security.jwt

import org.springframework.stereotype.Component
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.util.Base64

@Component
class KeyGenerator {

    fun generateRsaKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(2048)
        return generator.generateKeyPair()
    }

    fun encodePublicKey(key: PublicKey): String {
        return "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getEncoder().encodeToString(key.encoded).chunked(64).joinToString("\n") +
                "\n-----END PUBLIC KEY-----"
    }

    fun encodePrivateKey(key: PrivateKey): String {
        return "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getEncoder().encodeToString(key.encoded).chunked(64).joinToString("\n") +
                "\n-----END PRIVATE KEY-----"
    }
}