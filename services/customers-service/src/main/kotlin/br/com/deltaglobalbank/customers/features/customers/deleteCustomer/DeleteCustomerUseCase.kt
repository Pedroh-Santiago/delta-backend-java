package br.com.deltaglobalbank.customers.features.customers.deleteCustomer

import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditRepository
import br.com.deltaglobalbank.customers.domain.customer.CustomerNotFound
import br.com.deltaglobalbank.customers.domain.customer.CustomerRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DeleteCustomerUseCase(
    private val customerRepository: CustomerRepository,
    private val customerAuditRepository: CustomerAuditRepository,
) {
    @Transactional
    fun execute(command: DeleteCustomerCommand) {
        val customer = customerRepository.findById(command.customerId, command.tenantId)
            ?: throw CustomerNotFound()
        val audit = customer.markAsDeleted(command.deletedBy)
        customerRepository.delete(customer)
        customerAuditRepository.saveAll(listOf(audit))
    }
}
data class DeleteCustomerCommand(val customerId: UUID, val tenantId: UUID, val deletedBy: UUID)