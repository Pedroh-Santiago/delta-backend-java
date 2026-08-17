package br.com.deltaglobalbank.customers.features.customers.createCustomer

import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccount
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccountPurpose
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccountType
import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.Agency
import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.BankCode
import br.com.deltaglobalbank.customers.domain.customer.CpfAlreadyExists
import br.com.deltaglobalbank.customers.domain.customer.Customer
import br.com.deltaglobalbank.customers.domain.customer.CustomerRepository
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Address
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.BirthDate
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Cpf
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Email
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.FullName
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Gender
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.MaritalStatus
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.MotherName
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Phone
import br.com.deltaglobalbank.customers.domain.document.DocumentType
import br.com.deltaglobalbank.customers.domain.document.PersonalDocument
import br.com.deltaglobalbank.customers.domain.shared.valueobjects.Uf
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaDocumentIssuerRepository
import com.github.f4b6a3.uuid.UuidCreator
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

data class CreateCustomerCommand(
    val tenantId: UUID,
    val createdBy: UUID,
    val request: CreateCustomerRequest,
)

@Service
class CreateCustomerUseCase(
    private val customerRepository: CustomerRepository,
    private val jpaDocumentIssuerRepository: JpaDocumentIssuerRepository,
) {
    @Transactional
    fun execute(command: CreateCustomerCommand): CreateCustomerResponse {
        val request = command.request
        val cpf = Cpf(request.cpf)
        if (customerRepository.existsByCpfAndTenantId(cpf, command.tenantId))
            throw CpfAlreadyExists()

        val gender = Gender.fromDatabaseValue(request.gender)
        val maritalStatus = MaritalStatus.fromDatabaseValue(request.maritalStatus)
        val email = request.email?.let { Email(it) }
        val phone = Phone(request.phone.phoneNumber)
        val address = Address(
            cep = request.address.cep,
            street = request.address.street,
            city = request.address.city,
            state = Uf(request.address.state),
            country = request.address.country,
            number = request.address.number,
            complement = request.address.complement,
            neighborhood = request.address.neighborhood,
        )

        val customer = Customer.create(
            id = UuidCreator.getTimeOrderedEpoch(),
            tenantId = command.tenantId,
            cpf = cpf,
            fullName = FullName(request.fullName),
            birthDate = BirthDate(request.birthDate),
            gender = gender,
            motherName = MotherName(request.motherName),
            maritalStatus = maritalStatus,
            email = email,
            phoneNumber = phone,
            address = address,
            createdBy = command.createdBy,
            nationality = request.nationality,
            bankAccounts = emptyList(),
            documents = emptyList(),

        )
        request.bankAccounts.forEach { acc ->
            val account = BankAccount.create(
                id = UuidCreator.getTimeOrderedEpoch(),
                customerId = customer.id,
                bankCode = BankCode(acc.bankCode),
                agency = Agency(acc.agency),
                accountNumber = acc.accountNumber,
                accountDigit = acc.accountDigit,
                accountType = BankAccountType.fromDatabaseValue(acc.accountType),
                purpose = BankAccountPurpose.fromDatabaseValue(acc.purpose),
                isPrimary = acc.isPrimary,
            )
            customer.addBankAccount(account, command.createdBy)   // lança DuplicatePrimaryAccountForPurpose → 400
        }

        val documents = request.documents.map { doc ->
            val issuerId = jpaDocumentIssuerRepository.findByName(doc.issuer)?.id
                ?: throw IllegalArgumentException("invalid_issuer")   // 400
            PersonalDocument.create(
                id = UuidCreator.getTimeOrderedEpoch(),
                customerId = customer.id,
                documentType = DocumentType.fromDatabaseValue(doc.type),
                documentNumber = doc.number,
                issuerId = issuerId,
                issuerState = Uf(doc.issuerState),
                expiresAt = null,
                issuedAt = doc.issuedAt,
            )
        }
        customer.replaceDocuments(documents, command.createdBy)

        val saved = customerRepository.save(customer)

        val s = saved.snapshot()
        return CreateCustomerResponse(
            id = s.id, tenantId = s.tenantId,
            cpf = s.cpf.value, fullName = s.fullName.value,
            birthDate = s.birthDate.value,
            status = s.status.toDatabaseValue(),
            createdAt = s.createdAt,
        )
    }
}

