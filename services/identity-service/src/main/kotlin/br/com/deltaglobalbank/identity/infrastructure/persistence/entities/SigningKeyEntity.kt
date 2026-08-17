package br.com.deltaglobalbank.identity.infrastructure.persistence.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "signing_keys")
class SigningKeyEntity(
    @Id
    var id: UUID,

    @Column(name = "kid", nullable = false)
    var kid: String,

    @Column(name = "algorithm", nullable = false)
    var algorithm: String = "RS256",

    @Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
    var publicKey: String,

    @Column(name = "private_key", nullable = false, columnDefinition = "TEXT")
    var privateKey: String,

    @Column(name = "status", nullable = false)
    var status: String = "active",

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "activated_at")
    var activatedAt: Instant? = null,

    @Column(name = "retired_at")
    var retiredAt: Instant? = null
)
