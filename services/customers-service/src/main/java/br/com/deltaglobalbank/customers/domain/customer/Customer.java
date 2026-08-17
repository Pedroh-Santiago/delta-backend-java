package br.com.deltaglobalbank.customers.domain.customer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditEntry;
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccount;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Address;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.BirthDate;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Cpf;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Email;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.FullName;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Gender;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.MaritalStatus;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.MotherName;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Phone;
import br.com.deltaglobalbank.customers.domain.document.PersonalDocument;

public class Customer {

    private final UUID id;
    private final UUID tenantId;
    private final String nationality;
    private final Instant createdAt;
    private final UUID createdBy;

    private Cpf cpf;
    private FullName fullName;
    private BirthDate birthDate;
    private Gender gender;
    private MotherName motherName;
    private MaritalStatus maritalStatus;
    private Email email;
    private Phone phoneNumber;
    private Address address;
    private CustomerStatus status;
    private Instant updatedAt;
    private UUID updatedBy;

    private final List<BankAccount> bankAccounts;
    private final List<PersonalDocument> documents;

    private Customer(
        UUID id,
        UUID tenantId,
        Cpf cpf,
        FullName fullName,
        BirthDate birthDate,
        Gender gender,
        String nationality,
        MotherName motherName,
        MaritalStatus maritalStatus,
        Email email,
        Phone phoneNumber,
        Address address,
        CustomerStatus status,
        Instant createdAt,
        Instant updatedAt,
        UUID createdBy,
        UUID updatedBy,
        List<BankAccount> bankAccounts,
        List<PersonalDocument> documents
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.cpf = cpf;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.nationality = nationality;
        this.motherName = motherName;
        this.maritalStatus = maritalStatus;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        validateBankAccountInvariants(bankAccounts);
        validateDocumentInvariants(documents);
        this.bankAccounts = new ArrayList<>(bankAccounts);
        this.documents = new ArrayList<>(documents);
    }

    public UUID id() {
        return id;
    }

    public UUID tenantId() {
        return tenantId;
    }

    public List<BankAccount> bankAccounts() {
        return List.copyOf(bankAccounts);
    }

    public List<PersonalDocument> documents() {
        return List.copyOf(documents);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Customer customer && customer.id.equals(id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Customer(id=" + id + ", status=" + status + ")";
    }

    public CustomerSnapshot snapshot() {
        return new CustomerSnapshot(
            id,
            tenantId,
            cpf,
            fullName,
            birthDate,
            gender,
            nationality,
            motherName,
            maritalStatus,
            email,
            phoneNumber,
            address,
            status,
            createdAt,
            updatedAt,
            createdBy,
            updatedBy,
            bankAccounts(),
            documents()
        );
    }

    public static Customer create(
        UUID id,
        UUID tenantId,
        Cpf cpf,
        FullName fullName,
        BirthDate birthDate,
        Gender gender,
        String nationality,
        MotherName motherName,
        MaritalStatus maritalStatus,
        Email email,
        Phone phoneNumber,
        Address address,
        UUID createdBy,
        List<BankAccount> bankAccounts,
        List<PersonalDocument> documents
    ) {
        Instant now = Instant.now();
        return new Customer(
            id,
            tenantId,
            cpf,
            fullName,
            birthDate,
            gender,
            nationality,
            motherName,
            maritalStatus,
            email,
            phoneNumber,
            address,
            CustomerStatus.ACTIVE,
            now,
            now,
            createdBy,
            createdBy,
            bankAccounts,
            documents
        );
    }

    public static Customer create(
        UUID id,
        UUID tenantId,
        Cpf cpf,
        FullName fullName,
        BirthDate birthDate,
        Gender gender,
        MotherName motherName,
        MaritalStatus maritalStatus,
        Email email,
        Phone phoneNumber,
        Address address,
        UUID createdBy,
        List<BankAccount> bankAccounts,
        List<PersonalDocument> documents
    ) {
        return create(
            id,
            tenantId,
            cpf,
            fullName,
            birthDate,
            gender,
            "brasileira",
            motherName,
            maritalStatus,
            email,
            phoneNumber,
            address,
            createdBy,
            bankAccounts,
            documents
        );
    }

    public static Customer restore(
        UUID id,
        UUID tenantId,
        Cpf cpf,
        FullName fullName,
        BirthDate birthDate,
        String nationality,
        Gender gender,
        MotherName motherName,
        MaritalStatus maritalStatus,
        Email email,
        Phone phoneNumber,
        Address address,
        CustomerStatus status,
        Instant createdAt,
        Instant updatedAt,
        UUID createdBy,
        UUID updatedBy,
        List<BankAccount> bankAccounts,
        List<PersonalDocument> documents
    ) {
        return new Customer(
            id,
            tenantId,
            cpf,
            fullName,
            birthDate,
            gender,
            nationality,
            motherName,
            maritalStatus,
            email,
            phoneNumber,
            address,
            status,
            createdAt,
            updatedAt,
            createdBy,
            updatedBy,
            bankAccounts,
            documents
        );
    }

    private void touch(UUID updatedBy) {
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }

    private static void validateBankAccountInvariants(List<BankAccount> accounts) {
        accounts.stream()
            .collect(Collectors.groupingBy(BankAccount::purpose))
            .forEach((purpose, group) -> {
                if (group.stream().filter(BankAccount::isPrimary).count() > 1) {
                    throw new DuplicatePrimaryAccountForPurpose();
                }
            });

        boolean hasDuplicate = accounts.stream()
            .collect(Collectors.groupingBy(BankAccount::identity))
            .values().stream()
            .anyMatch(group -> group.size() > 1);
        if (hasDuplicate) {
            throw new DuplicateBankAccount();
        }
    }

    private static void validateDocumentInvariants(List<PersonalDocument> documents) {
        boolean hasDuplicate = documents.stream()
            .collect(Collectors.groupingBy(PersonalDocument::identity))
            .values().stream()
            .anyMatch(group -> group.size() > 1);
        if (hasDuplicate) {
            throw new DuplicatePersonalDocument();
        }
    }

    public void updateContact(Email email, Phone phoneNumber, UUID updatedBy) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        touch(updatedBy);
    }

    public void updateAddress(Address address, UUID updatedBy) {
        this.address = address;
        touch(updatedBy);
    }

    public void replaceDocuments(List<PersonalDocument> documents, UUID updatedBy) {
        validateDocumentInvariants(documents);
        this.documents.clear();
        this.documents.addAll(documents);
        touch(updatedBy);
    }

    public List<CustomerAuditEntry> updatePersonalInfo(
        Cpf cpf,
        FullName fullName,
        BirthDate birthDate,
        Gender gender,
        MotherName motherName,
        MaritalStatus maritalStatus,
        UUID updatedBy
    ) {
        List<CustomerAuditEntry> audits = new ArrayList<>();
        if (!cpf.equals(this.cpf)) {
            audits.add(CustomerAuditEntry.cpfChanged(id, tenantId, this.cpf.value(), cpf.value(), updatedBy));
            this.cpf = cpf;
        }

        if (!fullName.equals(this.fullName)) {
            audits.add(CustomerAuditEntry.fullNameChanged(id, tenantId, this.fullName.value(), fullName.value(), updatedBy));
            this.fullName = fullName;
        }

        this.birthDate = birthDate;
        this.gender = gender;
        this.motherName = motherName;
        this.maritalStatus = maritalStatus;

        touch(updatedBy);
        return audits;
    }

    public CustomerAuditEntry addBankAccount(BankAccount account, UUID updatedBy) {
        if (account.isPrimary()
            && bankAccounts.stream().anyMatch(it -> it.purpose() == account.purpose() && it.isPrimary())) {
            throw new DuplicatePrimaryAccountForPurpose();
        }

        if (bankAccounts.stream().anyMatch(it -> it.identity().equals(account.identity()))) {
            throw new DuplicateBankAccount();
        }

        bankAccounts.add(account);
        touch(updatedBy);

        return CustomerAuditEntry.bankAccountAdded(id, tenantId, account.describe(), updatedBy);
    }

    public CustomerAuditEntry removeBankAccount(UUID accountId, UUID updatedBy) {
        BankAccount account = bankAccounts.stream()
            .filter(it -> it.id().equals(accountId))
            .findFirst()
            .orElseThrow(BankAccountNotFound::new);

        bankAccounts.remove(account);
        touch(updatedBy);

        return CustomerAuditEntry.bankAccountRemoved(id, tenantId, account.describe(), updatedBy);
    }

    public List<CustomerAuditEntry> replaceBankAccounts(List<BankAccount> newAccounts, UUID updatedBy) {
        validateBankAccountInvariants(newAccounts);

        Set<UUID> newIds = newAccounts.stream().map(BankAccount::id).collect(Collectors.toSet());
        Set<UUID> oldIds = bankAccounts.stream().map(BankAccount::id).collect(Collectors.toSet());

        List<BankAccount> removed = bankAccounts.stream().filter(it -> !newIds.contains(it.id())).toList();
        List<BankAccount> added = newAccounts.stream().filter(it -> !oldIds.contains(it.id())).toList();

        List<CustomerAuditEntry> audits = new ArrayList<>();
        removed.forEach(it -> audits.add(CustomerAuditEntry.bankAccountRemoved(id, tenantId, it.describe(), updatedBy)));
        added.forEach(it -> audits.add(CustomerAuditEntry.bankAccountAdded(id, tenantId, it.describe(), updatedBy)));

        bankAccounts.clear();
        bankAccounts.addAll(newAccounts);

        if (!audits.isEmpty()) {
            touch(updatedBy);
        }
        return audits;
    }

    public CustomerAuditEntry inactivate(UUID updatedBy) {
        CustomerStatus oldValue = status;
        status = CustomerStatus.INACTIVE;
        touch(updatedBy);
        return CustomerAuditEntry.statusChanged(
            id,
            tenantId,
            oldValue.toDatabaseValue(),
            status.toDatabaseValue(),
            updatedBy
        );
    }

    public CustomerAuditEntry reactivate(UUID updatedBy) {
        CustomerStatus oldValue = status;
        status = CustomerStatus.ACTIVE;
        touch(updatedBy);
        return CustomerAuditEntry.statusChanged(
            id,
            tenantId,
            oldValue.toDatabaseValue(),
            status.toDatabaseValue(),
            updatedBy
        );
    }

    public CustomerAuditEntry markAsDeleted(UUID by) {
        touch(by);
        return CustomerAuditEntry.customerDeleted(id, tenantId, by);
    }
}
