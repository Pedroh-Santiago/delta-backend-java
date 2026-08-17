package br.com.deltaglobalbank.identity.infrastructure.security.apikey

import org.springframework.stereotype.Component
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class ApiKeyFingerprinter(properties: ApiKeyProperties) {
    private val secretKey =
        SecretKeySpec(properties.fingerprintSecret.toByteArray(Charsets.UTF_8), ALGORITHM)

    fun fingerprint(plainKey: String): String {
        val mac = Mac.getInstance(ALGORITHM)
        mac.init(secretKey)
        val bytes = mac.doFinal(plainKey.toByteArray(Charsets.UTF_8))
        return bytes.toHexString()
    }

    companion object {
        private const val ALGORITHM = "HmacSHA256"
    }
}