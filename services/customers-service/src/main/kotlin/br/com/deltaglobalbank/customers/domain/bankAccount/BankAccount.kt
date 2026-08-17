package br.com.deltaglobalbank.customers.domain.bankAccount

import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.Agency
import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.BankCode
import java.util.UUID
import java.time.Instant

class BankAccount private constructor(
    val id: UUID,
    val customerId: UUID,
    val bankCode: BankCode,
    val agency: Agency,
    val accountNumber: String,
    val accountDigit: String?,
    val accountType: BankAccountType,
    val purpose: BankAccountPurpose,
    val isPrimary: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun create(
            id: UUID,
            customerId: UUID,
            bankCode: BankCode,
            agency: Agency,
            accountNumber: String,
            accountDigit: String?,
            accountType: BankAccountType,
            purpose: BankAccountPurpose,
            isPrimary: Boolean,
        ): BankAccount {
            val now = Instant.now()
            return BankAccount(
                id = id,
                customerId = customerId,
                bankCode = bankCode,
                agency = agency,
                accountNumber = accountNumber,
                accountDigit = accountDigit,
                accountType = accountType,
                purpose = purpose,
                isPrimary = isPrimary,
                createdAt = now,
                updatedAt = now
            )
        }

        fun restore(
            id: UUID,
            customerId: UUID,
            bankCode: BankCode,
            agency: Agency,
            accountNumber: String,
            accountDigit: String?,
            accountType: BankAccountType,
            purpose: BankAccountPurpose,
            isPrimary: Boolean,
            createdAt: Instant,
            updatedAt: Instant
        ): BankAccount = BankAccount(
            id = id,
            customerId = customerId,
            bankCode = bankCode,
            agency = agency,
            accountNumber = accountNumber,
            accountDigit = accountDigit,
            accountType = accountType,
            purpose = purpose,
            isPrimary = isPrimary,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    override fun equals(other: Any?) = other is BankAccount && id == other.id

    override fun hashCode() = id.hashCode()
}