package br.com.deltaglobalbank.products.infrastructure.persistence.mappers

import br.com.deltaglobalbank.products.domain.product.AgreementName
import br.com.deltaglobalbank.products.domain.product.DisplayName
import br.com.deltaglobalbank.products.domain.product.Product
import br.com.deltaglobalbank.products.domain.product.ProductType
import br.com.deltaglobalbank.products.infrastructure.persistence.entities.ProductEntity

fun ProductEntity.toDomain(): Product = Product(
    id = id,
    tenantId = tenantId,
    type = ProductType.fromDatabaseValue(type),
    agreementName = AgreementName(agreementName),
    displayName = DisplayName(displayName),
    minMonthlyRate = minMonthlyRate,
    maxMonthlyRate = maxMonthlyRate,
    minMonths = minMonths,
    maxMonths = maxMonths,
    minAmount = minAmount,
    maxAmount = maxAmount,
    commissionRate = commissionRate,
    active = active,
    createdAt = createdAt,
    updatedAt = updatedAt,
    createdBy = createdBy,
    updatedBy = updatedBy
)

fun Product.toEntity(): ProductEntity {
    val s = snapshot()
    return ProductEntity(
        id = s.id,
        tenantId = s.tenantId,
        type = s.type.toDatabaseValue(),
        agreementName = s.agreementName.value,
        displayName = s.displayName.value,
        minMonthlyRate = s.minMonthlyRate,
        maxMonthlyRate = s.maxMonthlyRate,
        minMonths = s.minMonths,
        maxMonths = s.maxMonths,
        minAmount = s.minAmount,
        maxAmount = s.maxAmount,
        commissionRate = s.commissionRate,
        active = s.active,
        createdAt = s.createdAt,
        updatedAt = s.updatedAt,
        createdBy = s.createdBy,
        updatedBy = s.updatedBy
    )
}

fun Product.applyTo(entity: ProductEntity): ProductEntity {
    val s = snapshot()
    entity.agreementName = s.agreementName.value
    entity.displayName = s.displayName.value
    entity.minMonthlyRate = s.minMonthlyRate
    entity.maxMonthlyRate = s.maxMonthlyRate
    entity.minMonths = s.minMonths
    entity.maxMonths = s.maxMonths
    entity.minAmount = s.minAmount
    entity.maxAmount = s.maxAmount
    entity.commissionRate = s.commissionRate
    entity.active = s.active
    entity.updatedAt = s.updatedAt
    entity.updatedBy = s.updatedBy
    return entity
}