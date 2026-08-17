package br.com.deltaglobalbank.internal_treasury.features.approveInternalTransferece

import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransference
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransferenceRepository
import br.com.deltaglobalbank.internal_treasury.infrastructure.messaging.TefPublisher
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ApproveInternalTransferenceUseCase (
    private val repository: InternalTransferenceRepository,
    private val tefPublisher: TefPublisher
) {
    fun execute(request: ApproveInternalTransferenceRequest) : List<UUID> {
        val approved = mutableListOf<UUID>()

        for (id in request.approved) {
            val transference = repository.findById(id)
                ?: continue

            transference.approve()
            repository.save(transference)

            tefPublisher.publishApproval(id)

            approved.add(id)
        }
        return approved
    }
}