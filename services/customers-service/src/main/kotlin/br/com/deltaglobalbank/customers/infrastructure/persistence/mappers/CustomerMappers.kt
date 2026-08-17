package br.com.deltaglobalbank.customers.infrastructure.persistence.mappers

import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccount
import br.com.deltaglobalbank.customers.domain.customer.Customer
import br.com.deltaglobalbank.customers.domain.customer.CustomerStatus
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Address
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.BirthDate
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Cpf
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Email
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.FullName
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Gender
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.MaritalStatus
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.MotherName
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Phone
import br.com.deltaglobalbank.customers.domain.document.PersonalDocument
import br.com.deltaglobalbank.customers.domain.shared.valueobjects.Uf
import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.CustomerEntity

fun CustomerEntity.toDomain(
    bankAccounts: List<BankAccount>,
    documents: List<PersonalDocument>,
): Customer = Customer.restore(
    id = this.id,
    tenantId = this.tenantId,
    cpf = Cpf(this.cpf),
    fullName = FullName(this.fullName),
    birthDate = BirthDate(this.birthDate),
    gender = Gender.fromDatabaseValue(this.gender),
    nationality = this.nationality,
    motherName = MotherName(this.motherName),
    maritalStatus = MaritalStatus.fromDatabaseValue(this.maritalStatus),
    email = this.email?.let { Email(it) },
    phoneNumber = Phone(this.phoneNumber),
    address = Address(
        cep = addressCep, street = addressStreet, city = addressCity,
        state = Uf(addressState), country = addressCountry,
        number = addressNumber, complement = addressComplement, neighborhood = addressNeighborhood,
    ),
    status = CustomerStatus.fromDatabaseValue(this.status),
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
    createdBy = this.createdBy,
    updatedBy = this.updatedBy,
    bankAccounts = bankAccounts,
    documents = documents,
)

fun Customer.toEntity(): CustomerEntity{
    val s = snapshot()
    return CustomerEntity(
        id = s.id,
        tenantId = s.tenantId,
        cpf = s.cpf.value,
        fullName = s.fullName.value,
        birthDate = s.birthDate.value,
        gender = s.gender.toDatabaseValue(),
        nationality = s.nationality,
        motherName = s.motherName.value,
        maritalStatus = s.maritalStatus.toDatabaseValue(),
        email = s.email?.value,
        phoneNumber = s.phoneNumber.value,
        addressCep = s.address.cep,
        addressStreet = s.address.street,
        addressNumber = s.address.number,
        addressComplement = s.address.complement,
        addressNeighborhood = s.address.neighborhood,
        addressCity = s.address.city,
        addressState = s.address.state.value,
        addressCountry = s.address.country,
        status = s.status.toDatabaseValue(),
        createdAt = s.createdAt,
        updatedAt = s.updatedAt,
        createdBy = s.createdBy,
        updatedBy = s.updatedBy,
    )
}

fun Customer.applyTo(entity: CustomerEntity): CustomerEntity{
    val s = snapshot()
    entity.cpf = s.cpf.value
    entity.fullName = s.fullName.value
    entity.birthDate = s.birthDate.value
    entity.gender = s.gender.toDatabaseValue()
    entity.nationality = s.nationality
    entity.motherName = s.motherName.value
    entity.maritalStatus = s.maritalStatus.toDatabaseValue()
    entity.email = s.email?.value
    entity.phoneNumber = s.phoneNumber.value
    entity.addressCep = s.address.cep
    entity.addressStreet = s.address.street
    entity.addressNumber = s.address.number
    entity.addressComplement = s.address.complement
    entity.addressNeighborhood = s.address.neighborhood
    entity.addressCity = s.address.city
    entity.addressState = s.address.state.value
    entity.addressCountry = s.address.country
    entity.status = s.status.toDatabaseValue()
    entity.updatedAt = s.updatedAt
    entity.updatedBy = s.updatedBy

    return entity
}