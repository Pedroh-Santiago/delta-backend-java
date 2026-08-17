package br.com.deltaglobalbank.customers.features.customers.getCustomer;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.deltaglobalbank.customers.domain.customer.Customer;
import br.com.deltaglobalbank.customers.domain.customer.CustomerNotFound;
import br.com.deltaglobalbank.customers.domain.customer.CustomerRepository;
import br.com.deltaglobalbank.customers.domain.customer.CustomerSnapshot;
import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.DocumentIssuerEntity;
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaDocumentIssuerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final JpaDocumentIssuerRepository jpaDocumentIssuerRepository;

    public GetCustomerUseCase(
        CustomerRepository customerRepository,
        JpaDocumentIssuerRepository jpaDocumentIssuerRepository
    ) {
        this.customerRepository = customerRepository;
        this.jpaDocumentIssuerRepository = jpaDocumentIssuerRepository;
    }

    @Transactional(readOnly = true)
    public GetCustomerResponse execute(GetCustomerRequest command) {
        Customer customer = customerRepository.findById(command.customerId(), command.tenantId());
        if (customer == null) {
            throw new CustomerNotFound();
        }
        Map<UUID, String> issuerNameByIssuerId = jpaDocumentIssuerRepository.findAll().stream()
            .collect(Collectors.toMap(DocumentIssuerEntity::getId, DocumentIssuerEntity::getName));

        CustomerSnapshot s = customer.snapshot();

        return new GetCustomerResponse(
            s.id(),
            s.tenantId(),
            s.cpf().value(),
            s.fullName().value(),
            s.birthDate().value(),
            s.gender().toDatabaseValue(),
            s.nationality(),
            s.motherName().value(),
            s.maritalStatus().toDatabaseValue(),
            s.email() != null ? s.email().value() : null,
            new PhoneResponse(s.phoneNumber().value()),
            new AddressResponse(
                s.address().cep(),
                s.address().street(),
                s.address().number(),
                s.address().complement(),
                s.address().neighborhood(),
                s.address().city(),
                s.address().state().value(),
                s.address().country()
            ),
            s.status().toDatabaseValue(),
            s.documents().stream()
                .map(it -> new DocumentResponse(
                    it.id(),
                    it.documentType().toDatabaseValue(),
                    it.documentNumber(),
                    issuerNameByIssuerId.getOrDefault(it.issuerId(), "OUTROS"),
                    it.issuerState().value(),
                    it.issuedAt(),
                    it.expiresAt()
                ))
                .toList(),
            s.bankAccounts().stream()
                .map(it -> new BankAccountResponse(
                    it.id(),
                    it.bankCode().value(),
                    it.agency().value(),
                    it.accountNumber(),
                    it.accountDigit(),
                    it.accountType().toDatabaseValue(),
                    it.purpose().toDatabaseValue(),
                    it.isPrimary()
                ))
                .toList(),
            s.createdAt(),
            s.updatedAt()
        );
    }
}
