package br.com.deltaglobalbank.customers.features.customers.createCustomer;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccount;
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccountPurpose;
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccountType;
import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.Agency;
import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.BankCode;
import br.com.deltaglobalbank.customers.domain.customer.CpfAlreadyExists;
import br.com.deltaglobalbank.customers.domain.customer.Customer;
import br.com.deltaglobalbank.customers.domain.customer.CustomerRepository;
import br.com.deltaglobalbank.customers.domain.customer.CustomerSnapshot;
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
public class CreateCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final JpaDocumentIssuerRepository jpaDocumentIssuerRepository;

    public CreateCustomerUseCase(
        CustomerRepository customerRepository,
        JpaDocumentIssuerRepository jpaDocumentIssuerRepository
    ) {
        this.customerRepository = customerRepository;
        this.jpaDocumentIssuerRepository = jpaDocumentIssuerRepository;
    }

    @Transactional
    public CreateCustomerResponse execute(CreateCustomerCommand command) {
        CreateCustomerRequest request = command.request();
        Cpf cpf = new Cpf(request.cpf());
        if (customerRepository.existsByCpfAndTenantId(cpf, command.tenantId())) {
            throw new CpfAlreadyExists();
        }

        Gender gender = Gender.fromDatabaseValue(request.gender());
        MaritalStatus maritalStatus = MaritalStatus.fromDatabaseValue(request.maritalStatus());
        Email email = request.email() != null ? new Email(request.email()) : null;
        Phone phone = new Phone(request.phone().phoneNumber());
        Address address = new Address(
            request.address().cep(),
            request.address().street(),
            request.address().city(),
            new Uf(request.address().state()),
            request.address().country(),
            request.address().number(),
            request.address().complement(),
            request.address().neighborhood()
        );

        Customer customer = Customer.create(
            UuidCreator.getTimeOrderedEpoch(),
            command.tenantId(),
            cpf,
            new FullName(request.fullName()),
            new BirthDate(request.birthDate()),
            gender,
            request.nationality(),
            new MotherName(request.motherName()),
            maritalStatus,
            email,
            phone,
            address,
            command.createdBy(),
            List.of(),
            List.of()
        );

        for (BankAccountRequest acc : request.bankAccounts()) {
            BankAccount account = BankAccount.create(
                UuidCreator.getTimeOrderedEpoch(),
                customer.id(),
                new BankCode(acc.bankCode()),
                new Agency(acc.agency()),
                acc.accountNumber(),
                acc.accountDigit(),
                BankAccountType.fromDatabaseValue(acc.accountType()),
                BankAccountPurpose.fromDatabaseValue(acc.purpose()),
                acc.isPrimary()
            );
            customer.addBankAccount(account, command.createdBy());
        }

        List<PersonalDocument> documents = request.documents().stream()
            .map(doc -> {
                DocumentIssuerEntity issuer = jpaDocumentIssuerRepository.findByName(doc.issuer());
                if (issuer == null) {
                    throw new IllegalArgumentException("invalid_issuer");
                }
                return PersonalDocument.create(
                    UuidCreator.getTimeOrderedEpoch(),
                    customer.id(),
                    DocumentType.fromDatabaseValue(doc.type()),
                    doc.number(),
                    issuer.getId(),
                    new Uf(doc.issuerState()),
                    null,
                    doc.issuedAt()
                );
            })
            .toList();
        customer.replaceDocuments(documents, command.createdBy());

        Customer saved = customerRepository.save(customer);

        CustomerSnapshot s = saved.snapshot();
        return new CreateCustomerResponse(
            s.id(),
            s.tenantId(),
            s.cpf().value(),
            s.fullName().value(),
            s.birthDate().value(),
            s.status().toDatabaseValue(),
            s.createdAt()
        );
    }
}
