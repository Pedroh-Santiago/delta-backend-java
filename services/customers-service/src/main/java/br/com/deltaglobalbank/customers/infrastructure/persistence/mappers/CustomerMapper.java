package br.com.deltaglobalbank.customers.infrastructure.persistence.mappers;

import java.util.List;

import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccount;
import br.com.deltaglobalbank.customers.domain.customer.Customer;
import br.com.deltaglobalbank.customers.domain.customer.CustomerSnapshot;
import br.com.deltaglobalbank.customers.domain.customer.CustomerStatus;
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
import br.com.deltaglobalbank.customers.domain.shared.valueobjects.Uf;
import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.CustomerEntity;

public final class CustomerMapper {

    private CustomerMapper() {
    }

    public static Customer toDomain(
        CustomerEntity entity,
        List<BankAccount> bankAccounts,
        List<PersonalDocument> documents
    ) {
        return Customer.restore(
            entity.getId(),
            entity.getTenantId(),
            new Cpf(entity.getCpf()),
            new FullName(entity.getFullName()),
            new BirthDate(entity.getBirthDate()),
            entity.getNationality(),
            Gender.fromDatabaseValue(entity.getGender()),
            new MotherName(entity.getMotherName()),
            MaritalStatus.fromDatabaseValue(entity.getMaritalStatus()),
            entity.getEmail() != null ? new Email(entity.getEmail()) : null,
            new Phone(entity.getPhoneNumber()),
            new Address(
                entity.getAddressCep(),
                entity.getAddressStreet(),
                entity.getAddressCity(),
                new Uf(entity.getAddressState()),
                entity.getAddressCountry(),
                entity.getAddressNumber(),
                entity.getAddressComplement(),
                entity.getAddressNeighborhood()
            ),
            CustomerStatus.fromDatabaseValue(entity.getStatus()),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getCreatedBy(),
            entity.getUpdatedBy(),
            bankAccounts,
            documents
        );
    }

    public static CustomerEntity toEntity(Customer customer) {
        CustomerSnapshot s = customer.snapshot();
        return new CustomerEntity(
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
            s.phoneNumber().value(),
            s.address().cep(),
            s.address().street(),
            s.address().number(),
            s.address().complement(),
            s.address().neighborhood(),
            s.address().city(),
            s.address().state().value(),
            s.address().country(),
            s.status().toDatabaseValue(),
            s.createdAt(),
            s.updatedAt(),
            s.createdBy(),
            s.updatedBy()
        );
    }

    public static CustomerEntity applyTo(Customer customer, CustomerEntity entity) {
        CustomerSnapshot s = customer.snapshot();
        entity.setCpf(s.cpf().value());
        entity.setFullName(s.fullName().value());
        entity.setBirthDate(s.birthDate().value());
        entity.setGender(s.gender().toDatabaseValue());
        entity.setNationality(s.nationality());
        entity.setMotherName(s.motherName().value());
        entity.setMaritalStatus(s.maritalStatus().toDatabaseValue());
        entity.setEmail(s.email() != null ? s.email().value() : null);
        entity.setPhoneNumber(s.phoneNumber().value());
        entity.setAddressCep(s.address().cep());
        entity.setAddressStreet(s.address().street());
        entity.setAddressNumber(s.address().number());
        entity.setAddressComplement(s.address().complement());
        entity.setAddressNeighborhood(s.address().neighborhood());
        entity.setAddressCity(s.address().city());
        entity.setAddressState(s.address().state().value());
        entity.setAddressCountry(s.address().country());
        entity.setStatus(s.status().toDatabaseValue());
        entity.setUpdatedAt(s.updatedAt());
        entity.setUpdatedBy(s.updatedBy());
        return entity;
    }
}
