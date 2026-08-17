package br.com.deltaglobalbank.customers.infrastructure.persistence.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "customer_bank_accounts")
@SQLDelete(sql = "UPDATE customers.customer_bank_accounts SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
class BankAccountEntity(
    @Id
    var id: UUID,

    @Column(name = "customer_id", nullable = false)
    var customerId: UUID,

    @Column(name = "bank_code", nullable = false)
    var bankCode: String,

    @Column(name = "agency", nullable = false)
    var agency: String,

    @Column(name = "account_number", nullable = false)
    var accountNumber: String,

    @Column(name = "account_digit")
    var accountDigit: String?,

    @Column(name = "account_type", nullable = false)
    var accountType: String,

    @Column(name = "purpose", nullable = false)
    var purpose: String,

    @Column(name = "is_primary", nullable = false)
    var isPrimary: Boolean,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
)