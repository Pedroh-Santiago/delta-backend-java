package br.com.deltaglobalbank.identity.infrastructure.security.jwt

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.SigningKeyEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaSigningKeyRepository
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.concurrent.ConcurrentHashMap

data class SigningKeyMaterial(
    val kid: String,
    val privateKey: RSAPrivateKey,
    val publicKey: RSAPublicKey,
    val status: String
)

@Component
class KeyManager(
    private val signingKeyRepository: JpaSigningKeyRepository,
    private val pemConverter: PemConverter
) {

    private val cache = ConcurrentHashMap<String, SigningKeyMaterial>()

    @PostConstruct
    fun warmUp() {
        refresh()
    }

    fun refresh() {
        cache.clear()
        signingKeyRepository.findAll().forEach { entity ->
            cache[entity.kid] = entity.toMaterial()
        }
    }

    fun getActiveSigningKey(): SigningKeyMaterial {
        val active = cache.values.filter { it.status == "active" }
        check(active.isNotEmpty()) {
            "Nenhuma signing key ativa encontrada. Bootstrap foi executado?"
        }
        check(active.size == 1) {
            "Múltiplas signing keys ativas encontradas. Estado inconsistente."
        }
        return active.first()
    }

    fun getByKid(kid: String): SigningKeyMaterial? {
        return cache[kid] ?: run {
            // cache miss, busca no banco (kid pode ser de chave criada recentemente)
            val entity = signingKeyRepository.findByKid(kid) ?: return null
            val material = entity.toMaterial()
            cache[kid] = material
            material
        }
    }

    fun getAllUsable(): List<SigningKeyMaterial> {
        return cache.values.filter { it.status in setOf("active", "retired") }
    }

    private fun SigningKeyEntity.toMaterial() = SigningKeyMaterial(
        kid = kid,
        privateKey = pemConverter.toPrivateKey(privateKey),
        publicKey = pemConverter.toPublicKey(publicKey),
        status = status
    )
}