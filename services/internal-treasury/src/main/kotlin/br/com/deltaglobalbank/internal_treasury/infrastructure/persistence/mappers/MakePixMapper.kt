package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.mappers

import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePix
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.entities.MakePixEntity

fun MakePixEntity.toDomain(): MakePix = MakePix(
    id = this.id,
    accountId = this.accountId,
    recipientInstitutionCode = this.recipientInstitutionCode,
    recipientBranchCode = this.recipientBranchCode,
    recipientAccountNumber = this.recipientAccountNumber,
    recipientAccountType = this.recipientAccountType,
    recipientName = this.recipientName,
    paidAt = this.paidAt,
    operationAmount = this.operationAmount,
    createdAt = this.createdAt,
    status = PaymentsStatus.valueOf(this.status),
)

fun MakePix.toEntity(): MakePixEntity = MakePixEntity(
    id = this.id,
    accountId = this.accountId,
    recipientInstitutionCode = this.recipientInstitutionCode,
    recipientBranchCode = this.recipientBranchCode,
    recipientAccountNumber = this.recipientAccountNumber,
    recipientAccountType = this.recipientAccountType,
    paidAt = this.paidAt,
    recipientName = this.recipientName,
    operationAmount = this.operationAmount,
    createdAt = this.createdAt,
    status = this.status.name,
)