package br.com.deltaglobalbank.customers.features.customers.deleteCustomer;

import java.util.List;

import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditEntry;
import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditRepository;
import br.com.deltaglobalbank.customers.domain.customer.Customer;
import br.com.deltaglobalbank.customers.domain.customer.CustomerNotFound;
import br.com.deltaglobalbank.customers.domain.customer.CustomerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DeleteCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerAuditRepository customerAuditRepository;

    public DeleteCustomerUseCase(
        CustomerRepository customerRepository,
        CustomerAuditRepository customerAuditRepository
    ) {
        this.customerRepository = customerRepository;
        this.customerAuditRepository = customerAuditRepository;
    }

    @Transactional
    public void execute(DeleteCustomerCommand command) {
        Customer customer = customerRepository.findById(command.customerId(), command.tenantId());
        if (customer == null) {
            throw new CustomerNotFound();
        }
        CustomerAuditEntry audit = customer.markAsDeleted(command.deletedBy());
        customerRepository.delete(customer);
        customerAuditRepository.saveAll(List.of(audit));
    }
}
