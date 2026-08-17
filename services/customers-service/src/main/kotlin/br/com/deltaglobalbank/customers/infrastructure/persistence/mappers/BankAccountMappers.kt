package br.com.deltaglobalbank.customers.infrastructure.persistence.mappers

import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccount
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccountPurpose
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccountType
import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.Agency
import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.BankCode
import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.BankAccountEntity

fun BankAccountEntity.toDomain(): BankAccount = BankAccount.restore(
    id = this.id,
    customerId = this.customerId,
    bankCode = BankCode(this.bankCode),
    agency = Agency(this.agency),
    accountNumber = this.accountNumber,
    accountDigit = this.accountDigit,
    accountType = BankAccountType.fromDatabaseValue(this.accountType),
    purpose = BankAccountPurpose.fromDatabaseValue(this.purpose),
    isPrimary = this.isPrimary,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

fun BankAccount.toEntity() = BankAccountEntity(
    id = id,
    customerId = customerId,
    bankCode = bankCode.value,
    agency = agency.value,
    accountNumber = accountNumber,
    accountDigit = accountDigit,
    accountType = accountType.toDatabaseValue(),
    purpose = purpose.toDatabaseValue(),
    isPrimary = isPrimary,
    createdAt = createdAt,
    updatedAt = updatedAt
)