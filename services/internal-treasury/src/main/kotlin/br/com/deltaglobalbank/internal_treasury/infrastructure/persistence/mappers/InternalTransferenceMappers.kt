package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.mappers

import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransference
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.entities.InternalTransferenceEntity

fun InternalTransferenceEntity.toDomain(): InternalTransference = InternalTransference(
    id = this.id,
    requestedById = this.requestedById,
    requestedAt = this.requestedAt,
    payerId = this.payerId,
    paidAt = this.paidAt,
    status = PaymentsStatus.valueOf(this.status),
    accountNumber = this.accountNumber,
    amount = this.amount,
    description = this.description
)

fun InternalTransference.toEntity(): InternalTransferenceEntity = InternalTransferenceEntity (
    id = this.id,
    requestedById = this.requestedById,
    requestedAt = this.requestedAt,
    payerId = this.payerId,
    paidAt = this.paidAt,
    status = this.status.name,
    accountNumber = this.accountNumber,
    amount = this.amount,
    description = this.description
)
