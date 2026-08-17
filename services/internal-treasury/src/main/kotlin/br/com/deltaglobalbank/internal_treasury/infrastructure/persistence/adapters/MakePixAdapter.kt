package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.adapters

import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePix
import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePixRepository
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.mappers.toDomain
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.mappers.toEntity
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.repositories.JpaMakePixRepository
import org.springframework.stereotype.Component
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

@Component
class MakePixAdapter (
    private val pixRepository : JpaMakePixRepository
) : MakePixRepository {

    override fun save (makePix: MakePix): MakePix{
        return pixRepository.save(makePix.toEntity()).toDomain()
    }

    override fun findByStatus(status: PaymentsStatus, pageable: Pageable): Page<MakePix> {
        return pixRepository.findByStatus(status.name, pageable).map { it.toDomain() }
    }

    override fun findAll(pageable: Pageable): Page<MakePix> {
        return pixRepository.findAll(pageable).map { it.toDomain() }
    }

    override fun findById(id: UUID): MakePix? {
        return pixRepository.findById(id).orElse(null)?.toDomain()
    }
}