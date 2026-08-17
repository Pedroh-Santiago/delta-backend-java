package br.com.deltaglobalbank.customers.features.customers.changeCustomerStatus;

import java.util.List;

import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditEntry;
import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditRepository;
import br.com.deltaglobalbank.customers.domain.customer.Customer;
import br.com.deltaglobalbank.customers.domain.customer.CustomerNotFound;
import br.com.deltaglobalbank.customers.domain.customer.CustomerRepository;
import br.com.deltaglobalbank.customers.domain.customer.CustomerSnapshot;
import br.com.deltaglobalbank.customers.domain.customer.CustomerStatus;
import br.com.deltaglobalbank.customers.domain.customer.StatusUnchanged;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeCustomerStatusUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerAuditRepository customerAuditRepository;

    public ChangeCustomerStatusUseCase(
        CustomerRepository customerRepository,
        CustomerAuditRepository customerAuditRepository
    ) {
        this.customerRepository = customerRepository;
        this.customerAuditRepository = customerAuditRepository;
    }

    @Transactional
    public ChangeCustomerStatusResponse execute(ChangeCustomerStatusCommand command) {
        CustomerStatus target = parseStatus(command.request().status());

        Customer customer = customerRepository.findById(command.customerId(), command.tenantId());
        if (customer == null) {
            throw new CustomerNotFound();
        }

        if (customer.snapshot().status() == target) {
            throw new StatusUnchanged();
        }

        CustomerAuditEntry audit = switch (target) {
            case INACTIVE -> customer.inactivate(command.updatedBy());
            case ACTIVE -> customer.reactivate(command.updatedBy());
        };

        Customer saved = customerRepository.save(customer);
        customerAuditRepository.saveAll(List.of(audit));

        CustomerSnapshot s = saved.snapshot();
        return new ChangeCustomerStatusResponse(
            s.id(),
            s.status().toDatabaseValue(),
            s.updatedAt(),
            s.updatedBy()
        );
    }

    private CustomerStatus parseStatus(String raw) {
        String normalized = raw == null ? null : raw.trim().toLowerCase();
        if ("active".equals(normalized)) {
            return CustomerStatus.ACTIVE;
        }
        if ("inactive".equals(normalized)) {
            return CustomerStatus.INACTIVE;
        }
        throw new IllegalArgumentException("invalid_status");
    }
}
