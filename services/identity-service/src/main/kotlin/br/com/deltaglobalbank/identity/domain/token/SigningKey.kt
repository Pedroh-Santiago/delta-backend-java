package br.com.deltaglobalbank.identity.domain.token

import com.github.f4b6a3.uuid.UuidCreator
import java.time.Instant
import java.util.UUID

class SigningKey(
    val id: UUID,
    val kid: String,
    val algorithm: String,
    val publicKey: String,
    privateKey: String,
    status: SigningKeyStatus,
    val createdAt: Instant,
    val activatedAt: Instant?,
    retiredAt: Instant?
) {

    private var _privateKey: String = privateKey
    private var _status: SigningKeyStatus = status
    private var _retiredAt: Instant? = retiredAt

    fun retire() {
        _status = SigningKeyStatus.RETIRED
        _retiredAt = Instant.now()
    }

    fun revoke() {
        _status = SigningKeyStatus.REVOKED
        _retiredAt = Instant.now()
    }

    fun status(): SigningKeyStatus = _status

    fun snapshot(): SigningKeySnapshot = SigningKeySnapshot(
        id = id,
        kid = kid,
        algorithm = algorithm,
        publicKey = publicKey,
        privateKey = _privateKey,
        status = _status,
        createdAt = createdAt,
        activatedAt = activatedAt,
        retiredAt = _retiredAt
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SigningKey) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "SigningKey(id=$id, kid=$kid, status=$_status)"

    companion object {
        fun create(kid: String, algorithm: String, publicKey: String, privateKey: String): SigningKey =
            SigningKey(
                id = UuidCreator.getTimeOrderedEpoch(),
                kid = kid, algorithm = algorithm,
                publicKey = publicKey, privateKey = privateKey,
                status = SigningKeyStatus.ACTIVE,
                createdAt = Instant.now(),
                activatedAt = Instant.now(),
                retiredAt = null
            )
    }
}

data class SigningKeySnapshot(
    val id: UUID,
    val kid: String,
    val algorithm: String,
    val publicKey: String,
    val privateKey: String,
    val status: SigningKeyStatus,
    val createdAt: Instant,
    val activatedAt: Instant?,
    val retiredAt: Instant?
)
