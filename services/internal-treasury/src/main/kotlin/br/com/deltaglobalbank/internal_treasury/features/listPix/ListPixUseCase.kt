package br.com.deltaglobalbank.internal_treasury.features.listPix

import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePixRepository
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class ListPixUseCase (
    private val repository: MakePixRepository
) {
    fun execute(page: Int, pageSize: Int, filter: String): ListPixResponse{
        val pageable = PageRequest.of(page, pageSize)

        val result = if (filter == "all"){
            repository.findAll(pageable)
        } else {
            val status = PaymentsStatus.valueOf(filter.uppercase())
            repository.findByStatus(status, pageable)
        }

        return ListPixResponse(
            content = result.content.map {
                MakePixItem(
                    id = it.id,
                    accountId = it.accountId,
                    recipientName = it.recipientName,
                    recipientAccountNumber = it.recipientAccountNumber,
                    operationAmount = it.operationAmount,
                    status = it.status.name,
                    createdAt = it.createdAt
                )
            },
            page = result.number,
            pageSize = result.size,
            totalItems = result.totalElements,
            totalPages = result.totalPages
        )
    }
}