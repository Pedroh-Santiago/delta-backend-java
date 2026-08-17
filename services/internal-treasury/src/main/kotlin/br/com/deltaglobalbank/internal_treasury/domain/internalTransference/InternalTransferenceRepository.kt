package br.com.deltaglobalbank.internal_treasury.domain.internalTransference

import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface InternalTransferenceRepository {
    fun save(internalTransference : InternalTransference): InternalTransference
    fun findByStatus(status: PaymentsStatus, pageable: Pageable): Page<InternalTransference>
    fun findAll(pageable: Pageable): Page<InternalTransference>
    fun findById(id: UUID): InternalTransference?
}