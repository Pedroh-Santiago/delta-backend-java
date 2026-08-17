package br.com.deltaglobalbank.identity.features.jwks

import br.com.deltaglobalbank.identity.domain.token.SigningKey
import br.com.deltaglobalbank.identity.domain.token.SigningKeyRepository
import br.com.deltaglobalbank.identity.domain.token.SigningKeyStatus
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.PemConverter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.interfaces.RSAPublicKey
import java.util.Base64

@Service
class JwksUseCase(
    private val signingKeyRepository: SigningKeyRepository,
    private val pemConverter: PemConverter
) {

    @Transactional(readOnly = true)
    fun execute(): JwkResponse {
        val activeKeys = signingKeyRepository.findAllByStatus(SigningKeyStatus.ACTIVE)
        val retiredKeys = signingKeyRepository.findAllByStatus(SigningKeyStatus.RETIRED)

        val jwks = (activeKeys + retiredKeys).map { it.toJwk() }

        return JwkResponse(keys = jwks)
    }

    private fun SigningKey.toJwk(): Jwk {
        val publicKey = pemConverter.toPublicKey(this.publicKey) as RSAPublicKey

        return Jwk(
            kty = "RSA",
            use = "sig",
            alg = this.algorithm,
            kid = this.kid,
            n = encodeUnsignedBase64Url(publicKey.modulus.toByteArray()),
            e = encodeUnsignedBase64Url(publicKey.publicExponent.toByteArray())
        )
    }

    private fun encodeUnsignedBase64Url(bytes: ByteArray): String {
        val stripped = if (bytes.isNotEmpty() && bytes[0] == 0.toByte()) {
            bytes.copyOfRange(1, bytes.size)
        } else {
            bytes
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(stripped)
    }
}