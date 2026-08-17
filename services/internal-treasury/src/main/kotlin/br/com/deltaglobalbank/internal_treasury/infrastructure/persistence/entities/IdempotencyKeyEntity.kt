package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "idempotency_keys")
class IdempotencyKeyEntity (

    @Id
    var key: UUID,

    @Column(name = "response", nullable = false)
    var response: String,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,

    )