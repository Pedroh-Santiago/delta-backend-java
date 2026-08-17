package br.com.deltaglobalbank.customers.features.customers.updateCustomer

import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditEntry
import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditRepository
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccount
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccountPurpose
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccountType
import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.Agency
import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.BankCode
import br.com.deltaglobalbank.customers.domain.customer.CustomerNotFound
import br.com.deltaglobalbank.customers.domain.customer.CustomerRepository
import br.com.deltaglobalbank.customers.domain.customer.SubaggregateDoesNotBelongToCustomer
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Address
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.BirthDate
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
import java.time.Instant
import java.util.UUID

data class UpdateCustomerCommand(
    val customerId: UUID,
    val tenantId: UUID,
    val updatedBy: UUID,
    val request: UpdateCustomerRequest,
)

@Service
class UpdateCustomerUseCase(
    private val customerRepository: CustomerRepository,
    private val customerAuditRepository: CustomerAuditRepository,
    private val jpaDocumentIssuerRepository: JpaDocumentIssuerRepository,
) {
    @Transactional
    fun execute(command: UpdateCustomerCommand): UpdateCustomerResponse {
        val request = command.request
        val customer = customerRepository.findById(command.customerId, command.tenantId)
            ?: throw CustomerNotFound()
        val existingCpf = customer.snapshot().cpf

        val issuers = jpaDocumentIssuerRepository.findAll()
        val idByName = issuers.associate { it.name to it.id }
        val nameById = issuers.associate { it.id to it.name }

        val audits = mutableListOf<CustomerAuditEntry>()

        audits += customer.updatePersonalInfo(
            cpf = existingCpf,
            fullName = FullName(request.fullName),
            birthDate = BirthDate(request.birthDate),
            gender = Gender.fromDatabaseValue(request.gender),
            motherName = MotherName(request.motherName),
            maritalStatus = MaritalStatus.fromDatabaseValue(request.maritalStatus),
            updatedBy = command.updatedBy,
        )

        customer.updateContact(
            request.email?.let { Email(it) },
            Phone(request.phone.phoneNumber),
            command.updatedBy
        )
        customer.updateAddress(
            Address(
                request.address.cep,
                request.address.street,
                request.address.city,
                Uf(request.address.state),
                request.address.country,
                request.address.number,
                request.address.complement,
                request.address.neighborhood
                ), command.updatedBy)

        val currentAccountsById = customer.bankAccounts.associateBy { it.id }
        val referencedAccountIds = request.bankAccounts.mapNotNull { it.id }.toSet()
        val keptAccounts = customer.bankAccounts.filter { it.id !in referencedAccountIds }

        val fromPayloadAccounts = request.bankAccounts.map { req ->
            if (req.id != null) {
                val current = currentAccountsById[req.id] ?: throw SubaggregateDoesNotBelongToCustomer()
                BankAccount.restore(
                    id = req.id,
                    customerId = customer.id,
                    bankCode = BankCode(req.bankCode),
                    agency = Agency(req.agency),
                    accountNumber = req.accountNumber,
                    accountDigit = req.accountDigit,
                    accountType = BankAccountType.fromDatabaseValue(req.accountType),
                    purpose = BankAccountPurpose.fromDatabaseValue(req.purpose),
                    isPrimary = req.isPrimary,
                    createdAt = current.createdAt,
                    updatedAt = Instant.now(),
                )
            } else {
                BankAccount.create(
                    id = UuidCreator.getTimeOrderedEpoch(),
                    customerId = customer.id,
                    bankCode = BankCode(req.bankCode),
                    agency = Agency(req.agency),
                    accountNumber = req.accountNumber,
                    accountDigit = req.accountDigit,
                    accountType = BankAccountType.fromDatabaseValue(req.accountType),
                    purpose = BankAccountPurpose.fromDatabaseValue(req.purpose),
                    isPrimary = req.isPrimary,
                )
            }
        }
        audits += customer.replaceBankAccounts(keptAccounts + fromPayloadAccounts, command.updatedBy)

        val currentDocsById = customer.documents.associateBy { it.id }
        val referencedDocIds = request.documents.mapNotNull { it.id }.toSet()
        val keptDocs = customer.documents.filter { it.id !in referencedDocIds }

        val fromPayloadDocs = request.documents.map { req ->
            val issuerId = idByName[req.issuer] ?: throw IllegalArgumentException("invalid_issuer")
            if (req.id != null) {
                val current = currentDocsById[req.id] ?: throw SubaggregateDoesNotBelongToCustomer()
                PersonalDocument.restore(
                    id = req.id,
                    customerId = customer.id,
                    documentType = DocumentType.fromDatabaseValue(req.type),
                    documentNumber = req.number,
                    issuerId = issuerId,
                    issuerState = Uf(req.issuerState),
                    expiresAt = null,
                    issuedAt = req.issuedAt,
                    createdAt = current.createdAt,
                    updatedAt = Instant.now(),
                )
            } else {
                PersonalDocument.create(
                    id = UuidCreator.getTimeOrderedEpoch(),
                    customerId = customer.id,
                    documentType = DocumentType.fromDatabaseValue(req.type),
                    documentNumber = req.number,
                    issuerId = issuerId,
                    issuerState = Uf(req.issuerState),
                    expiresAt = null,
                    issuedAt = req.issuedAt,
                )
            }
        }
        customer.replaceDocuments(keptDocs + fromPayloadDocs, command.updatedBy)

        val saved = customerRepository.save(customer)

        customerAuditRepository.saveAll(audits)

        val s = saved.snapshot()
        return UpdateCustomerResponse(
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
            phone = PhoneResponse(s.phoneNumber.value),
            address = AddressResponse(
                s.address.cep,
                s.address.street,
                s.address.number,
                s.address.complement,
                s.address.neighborhood,
                s.address.city,
                s.address.state.value,
                s.address.country,
            ),
            status = s.status.toDatabaseValue(),
            documents = s.documents.map {
                DocumentResponse(
                    it.id,
                    it.documentType.toDatabaseValue(),
                    it.documentNumber,
                    nameById[it.issuerId] ?: "OUTROS",
                    it.issuerState.value,
                    it.issuedAt
                ) },
            bankAccounts = s.bankAccounts.map {
                BankAccountResponse(
                    it.id,
                    it.bankCode.value, it.agency.value,
                it.accountNumber, it.accountDigit, it.accountType.toDatabaseValue(), it.purpose.toDatabaseValue(), it.isPrimary) },
            createdAt = s.createdAt, updatedAt = s.updatedAt,
        )
    }
}