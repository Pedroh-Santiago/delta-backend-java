package br.com.deltaglobalbank.customers.features.customers.updateCustomer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditEntry;
import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditRepository;
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccount;
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccountPurpose;
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccountType;
import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.Agency;
import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.BankCode;
import br.com.deltaglobalbank.customers.domain.customer.Customer;
import br.com.deltaglobalbank.customers.domain.customer.CustomerNotFound;
import br.com.deltaglobalbank.customers.domain.customer.CustomerRepository;
import br.com.deltaglobalbank.customers.domain.customer.CustomerSnapshot;
import br.com.deltaglobalbank.customers.domain.customer.SubaggregateDoesNotBelongToCustomer;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Address;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.BirthDate;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Cpf;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Email;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.FullName;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Gender;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.MaritalStatus;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.MotherName;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Phone;
import br.com.deltaglobalbank.customers.domain.document.DocumentType;
import br.com.deltaglobalbank.customers.domain.document.PersonalDocument;
import br.com.deltaglobalbank.customers.domain.shared.valueobjects.Uf;
import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.DocumentIssuerEntity;
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaDocumentIssuerRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UpdateCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerAuditRepository customerAuditRepository;
    private final JpaDocumentIssuerRepository jpaDocumentIssuerRepository;

    public UpdateCustomerUseCase(
            CustomerRepository customerRepository,
            CustomerAuditRepository customerAuditRepository,
            JpaDocumentIssuerRepository jpaDocumentIssuerRepository
    ) {
        this.customerRepository = customerRepository;
        this.customerAuditRepository = customerAuditRepository;
        this.jpaDocumentIssuerRepository = jpaDocumentIssuerRepository;
    }

    @Transactional
    public UpdateCustomerResponse execute(UpdateCustomerCommand command) {
        UpdateCustomerRequest request = command.request();
        Customer customer = customerRepository.findById(command.customerId(), command.tenantId());
        if (customer == null) {
            throw new CustomerNotFound();
        }

        List<DocumentIssuerEntity> issuers = jpaDocumentIssuerRepository.findAll();
        Map<String, UUID> issuerIdByName = issuers.stream()
                .collect(Collectors.toMap(DocumentIssuerEntity::getName, DocumentIssuerEntity::getId));
        Map<UUID, String> issuerNameById = issuers.stream()
                .collect(Collectors.toMap(DocumentIssuerEntity::getId, DocumentIssuerEntity::getName));

        List<CustomerAuditEntry> audits = new ArrayList<>();
        audits.addAll(updatePersonalDetails(customer, request, command.updatedBy()));
        audits.addAll(reconcileBankAccounts(customer, request.bankAccounts(), command.updatedBy()));
        reconcileDocuments(customer, request.documents(), issuerIdByName, command.updatedBy());

        Customer saved = customerRepository.save(customer);
        customerAuditRepository.saveAll(audits);

        return assembleResponse(saved, issuerNameById);
    }

    private List<CustomerAuditEntry> updatePersonalDetails(Customer customer, UpdateCustomerRequest request, UUID updatedBy) {
        Cpf existingCpf = customer.snapshot().cpf();
        List<CustomerAuditEntry> audits = new ArrayList<>(customer.updatePersonalInfo(
                existingCpf,
                new FullName(request.fullName()),
                new BirthDate(request.birthDate()),
                Gender.fromDatabaseValue(request.gender()),
                new MotherName(request.motherName()),
                MaritalStatus.fromDatabaseValue(request.maritalStatus()),
                updatedBy
        ));

        customer.updateContact(
                request.email() != null ? new Email(request.email()) : null,
                new Phone(request.phone().phoneNumber()),
                updatedBy
        );
        customer.updateAddress(
                new Address(
                        request.address().cep(),
                        request.address().street(),
                        request.address().city(),
                        new Uf(request.address().state()),
                        request.address().country(),
                        request.address().number(),
                        request.address().complement(),
                        request.address().neighborhood()
                ),
                updatedBy
        );
        return audits;
    }

    private List<CustomerAuditEntry> reconcileBankAccounts(
            Customer customer,
            List<BankAccountRequest> requestedAccounts,
            UUID updatedBy
    ) {
        Map<UUID, BankAccount> currentAccountsById = customer.bankAccounts().stream()
                .collect(Collectors.toMap(BankAccount::id, a -> a));
        Set<UUID> referencedAccountIds = requestedAccounts.stream()
                .map(BankAccountRequest::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<BankAccount> keptAccounts = customer.bankAccounts().stream()
                .filter(a -> !referencedAccountIds.contains(a.id()))
                .toList();

        List<BankAccount> fromPayloadAccounts = requestedAccounts.stream()
                .map(req -> toBankAccount(req, customer.id(), currentAccountsById))
                .toList();

        List<BankAccount> allAccounts = new ArrayList<>(keptAccounts);
        allAccounts.addAll(fromPayloadAccounts);
        return customer.replaceBankAccounts(allAccounts, updatedBy);
    }

    private BankAccount toBankAccount(BankAccountRequest req, UUID customerId, Map<UUID, BankAccount> currentAccountsById) {
        if (req.id() != null) {
            BankAccount current = currentAccountsById.get(req.id());
            if (current == null) {
                throw new SubaggregateDoesNotBelongToCustomer();
            }
            return BankAccount.restore(
                    req.id(),
                    customerId,
                    new BankCode(req.bankCode()),
                    new Agency(req.agency()),
                    req.accountNumber(),
                    req.accountDigit(),
                    BankAccountType.fromDatabaseValue(req.accountType()),
                    BankAccountPurpose.fromDatabaseValue(req.purpose()),
                    req.isPrimary(),
                    current.createdAt(),
                    Instant.now()
            );
        }
        return BankAccount.create(
                UuidCreator.getTimeOrderedEpoch(),
                customerId,
                new BankCode(req.bankCode()),
                new Agency(req.agency()),
                req.accountNumber(),
                req.accountDigit(),
                BankAccountType.fromDatabaseValue(req.accountType()),
                BankAccountPurpose.fromDatabaseValue(req.purpose()),
                req.isPrimary()
        );
    }

    private void reconcileDocuments(
            Customer customer,
            List<DocumentRequest> requestedDocs,
            Map<String, UUID> issuerIdByName,
            UUID updatedBy
    ) {
        Map<UUID, PersonalDocument> currentDocsById = customer.documents().stream()
                .collect(Collectors.toMap(PersonalDocument::id, d -> d));
        Set<UUID> referencedDocIds = requestedDocs.stream()
                .map(DocumentRequest::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<PersonalDocument> keptDocs = customer.documents().stream()
                .filter(d -> !referencedDocIds.contains(d.id()))
                .toList();

        List<PersonalDocument> fromPayloadDocs = requestedDocs.stream()
                .map(req -> toPersonalDocument(req, customer.id(), issuerIdByName, currentDocsById))
                .toList();

        List<PersonalDocument> allDocs = new ArrayList<>(keptDocs);
        allDocs.addAll(fromPayloadDocs);
        customer.replaceDocuments(allDocs, updatedBy);
    }

    private PersonalDocument toPersonalDocument(
            DocumentRequest req,
            UUID customerId,
            Map<String, UUID> issuerIdByName,
            Map<UUID, PersonalDocument> currentDocsById
    ) {
        UUID issuerId = issuerIdByName.get(req.issuer());
        if (issuerId == null) {
            throw new IllegalArgumentException("invalid_issuer");
        }
        if (req.id() != null) {
            PersonalDocument current = currentDocsById.get(req.id());
            if (current == null) {
                throw new SubaggregateDoesNotBelongToCustomer();
            }
            return PersonalDocument.restore(
                    req.id(),
                    customerId,
                    DocumentType.fromDatabaseValue(req.type()),
                    req.number(),
                    issuerId,
                    new Uf(req.issuerState()),
                    null,
                    req.issuedAt(),
                    current.createdAt(),
                    Instant.now()
            );
        }
        return PersonalDocument.create(
                UuidCreator.getTimeOrderedEpoch(),
                customerId,
                DocumentType.fromDatabaseValue(req.type()),
                req.number(),
                issuerId,
                new Uf(req.issuerState()),
                null,
                req.issuedAt()
        );
    }

    private UpdateCustomerResponse assembleResponse(Customer saved, Map<UUID, String> issuerNameById) {
        CustomerSnapshot s = saved.snapshot();
        return new UpdateCustomerResponse(
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
                                issuerNameById.getOrDefault(it.issuerId(), "OUTROS"),
                                it.issuerState().value(),
                                it.issuedAt()
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
