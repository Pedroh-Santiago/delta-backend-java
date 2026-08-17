package br.com.deltaglobalbank.customers.features.customers.listCustomer

import br.com.deltaglobalbank.customers.domain.customer.CustomerRepository
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Cpf
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

data class ListCustomerQuery(
    val tenantId: UUID,
    val page: Int,
    val size: Int?
)

@Service
class ListCustomerUseCase(
    private val customerRepository: CustomerRepository
){
    companion object {
        const val MAX_PAGE_SIZE = 100
        const val DEFAULT_PAGE_SIZE = 20
    }

    @Transactional(readOnly = true)
    fun execute(query: ListCustomerQuery) : ListCustomerResponse {
        val safePage = query.page.coerceAtLeast(0)
        val safeSize = (query.size ?: DEFAULT_PAGE_SIZE).coerceIn(1, MAX_PAGE_SIZE)

        val pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        val result = customerRepository.findPage(query.tenantId, pageable)

        val items = result.content.map { customer ->
            val s = customer.snapshot()
            ListedCustomer(
                id = s.id,
                cpf = s.cpf.value,
                fullName = s.fullName.value,
                birthDate = s.birthDate.value,
                status = s.status.toDatabaseValue(),
                createdAt = s.createdAt

            )
        }

        return ListCustomerResponse(
            items = items,
            page = safePage,
            size = safeSize,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }
}