package br.com.deltaglobalbank.internal_treasury.features.listInternalTrasference

import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransferenceRepository
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class ListInternalTransferenceUseCase(
    private val repository: InternalTransferenceRepository
) {
    fun execute(page: Int, pageSize: Int, filter: String): ListInternalTransferenceResponse {
        val pageable = PageRequest.of(page, pageSize)

        val result = if (filter == "all") {
            repository.findAll(pageable)
        } else {
            val status = PaymentsStatus.valueOf(filter.uppercase())
            repository.findByStatus(status, pageable)
        }

        return ListInternalTransferenceResponse(
            content = result.content.map {
                InternalTransferenceItem(
                    id = it.id,
                    payerId = it.payerId,
                    accountNumber = it.accountNumber,
                    amount = it.amount,
                    status = it.status.name,
                    requestedAt = it.requestedAt
                )
            },
            page = result.number,
            pageSize = result.size,
            totalItems = result.totalElements,
            totalPages = result.totalPages
        )
    }
}