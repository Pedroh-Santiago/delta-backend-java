package br.com.deltaglobalbank.internal_treasury.features.approvePix

import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePix
import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePixRepository
import br.com.deltaglobalbank.internal_treasury.infrastructure.messaging.PixPublisher
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ApprovePixUseCase (
    private val repository: MakePixRepository,
    private val pixPublisher: PixPublisher
) {
    fun execute(request: ApprovePixRequest): List<UUID> {
        val approved = mutableListOf<UUID>()

        for (id in request.approved) {
            val transference = repository.findById(id)
                ?: continue

            transference.approve()
            repository.save(transference)

            pixPublisher.publishApproval(id)

            approved.add(id)
        }
        return approved
    }
}