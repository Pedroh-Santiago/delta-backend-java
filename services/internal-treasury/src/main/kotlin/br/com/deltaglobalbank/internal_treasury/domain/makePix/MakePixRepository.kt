package br.com.deltaglobalbank.internal_treasury.domain.makePix

import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransference
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface MakePixRepository {
    fun save(makePix: MakePix): MakePix
    fun findByStatus(status: PaymentsStatus, pageable: Pageable): Page<MakePix>
    fun findAll(pageable: Pageable): Page<MakePix>
    fun findById(id: UUID): MakePix?
}