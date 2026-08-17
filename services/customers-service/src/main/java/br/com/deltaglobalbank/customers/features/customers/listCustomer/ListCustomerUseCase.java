package br.com.deltaglobalbank.customers.features.customers.listCustomer;

import java.util.List;

import br.com.deltaglobalbank.customers.domain.customer.Customer;
import br.com.deltaglobalbank.customers.domain.customer.CustomerRepository;
import br.com.deltaglobalbank.customers.domain.customer.CustomerSnapshot;
import br.com.deltaglobalbank.customers.domain.customer.CustomerStatus;
import br.com.deltaglobalbank.customers.domain.customer.InvalidStatusFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListCustomerUseCase {

    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 20;

    private final CustomerRepository customerRepository;

    public ListCustomerUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public ListCustomerResponse execute(ListCustomerQuery query) {
        int safePage = Math.max(query.page(), 0);
        int requestedSize = query.size() != null ? query.size() : DEFAULT_PAGE_SIZE;
        int safeSize = Math.min(Math.max(requestedSize, 1), MAX_PAGE_SIZE);

        String normalizedStatus = normalizeStatus(query.status());
        String normalizedCpf = blankToNull(query.cpf());
        String normalizedFullName = blankToNull(query.fullName());

        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Customer> result = customerRepository.findPage(
            query.tenantId(),
            pageable,
            normalizedStatus,
            normalizedCpf,
            normalizedFullName
        );

        List<ListedCustomer> items = result.getContent().stream()
            .map(ListCustomerUseCase::toListed)
            .toList();

        return new ListCustomerResponse(
            items,
            safePage,
            safeSize,
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    private static ListedCustomer toListed(Customer customer) {
        CustomerSnapshot s = customer.snapshot();
        return new ListedCustomer(
            s.id(),
            s.cpf().value(),
            s.fullName().value(),
            s.birthDate().value(),
            s.phoneNumber().value(),
            s.address().city(),
            s.address().state().value(),
            s.status().toDatabaseValue(),
            s.createdAt()
        );
    }

    private static String normalizeStatus(String status) {
        if (status == null) {
            return null;
        }
        try {
            return CustomerStatus.fromDatabaseValue(status).toDatabaseValue();
        } catch (IllegalArgumentException ex) {
            throw new InvalidStatusFilter();
        }
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
