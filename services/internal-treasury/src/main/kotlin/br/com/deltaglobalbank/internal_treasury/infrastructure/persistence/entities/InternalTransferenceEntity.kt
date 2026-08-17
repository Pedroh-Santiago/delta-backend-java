package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "internal_transferences")
class InternalTransferenceEntity (

    @Id
    var id: UUID,

    @Column(name = "requested_by_id", nullable = false)
    var requestedById: UUID,

    @Column(name = "requested_at", nullable = false)
    var requestedAt: Instant,

    @Column(name = "payer_id", nullable = false)
    var payerId: Long,

    @Column(name = "paid_at", nullable = false)
    var paidAt: Instant?,

    @Column(name = "account_number", nullable = false)
    var accountNumber: Long,

    @Column(name = "amount", nullable = false)
    var amount: Int,

    @Column(name = "description")
    var description: String,

    @Column(name = "status")
    var status: String,
)
