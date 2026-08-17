package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "pix_payments")
class MakePixEntity (

    @Id
    var id: UUID,

    @Column(name = "account_id", nullable = false)
    var accountId: Long,

    @Column(name = "recipient_institution_code", nullable = false)
    var recipientInstitutionCode: String,

    @Column(name = "recipient_branch_code", nullable = false)
    var recipientBranchCode: String,

    @Column(name = "recipient_account_number", nullable = false)
    var recipientAccountNumber: String,

    @Column(name = "recipient_account_type", nullable = false)
    var recipientAccountType: String,

    @Column(name = "recipient_name", nullable = false)
    var recipientName: String,

    @Column(name = "operation_amount", nullable = false)
    var operationAmount: Long,

    @Column(name = "paid_at", nullable = false)
    var paidAt: Instant?,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,

    @Column(name = "status")
    var status: String,
    )