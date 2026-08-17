package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.adapters

import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransference
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransferenceRepository
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.mappers.toDomain
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.mappers.toEntity
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.repositories.JpaInternalTransferenceRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class InternalTransferenceAdapter (
    private val internalTransferenceRepository: JpaInternalTransferenceRepository
) : InternalTransferenceRepository {

    override fun save(internalTransference: InternalTransference): InternalTransference{
        return internalTransferenceRepository.save(internalTransference.toEntity()).toDomain()
    }

    override fun findByStatus(status: PaymentsStatus, pageable: Pageable): Page<InternalTransference> {
        return internalTransferenceRepository.findByStatus(status.name, pageable)
            .map { it.toDomain() }
    }

    override fun findAll(pageable: Pageable): Page<InternalTransference> {
        return internalTransferenceRepository.findAll(pageable).map { it.toDomain() }
    }

    override fun findById(id: UUID): InternalTransference? {
        return internalTransferenceRepository.findById(id).orElse(null)?.toDomain()
    }
}