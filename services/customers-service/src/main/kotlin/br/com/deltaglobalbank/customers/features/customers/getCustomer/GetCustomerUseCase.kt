package br.com.deltaglobalbank.customers.features.customers.getCustomer

import br.com.deltaglobalbank.customers.domain.customer.CustomerNotFound
import br.com.deltaglobalbank.customers.domain.customer.CustomerRepository
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaDocumentIssuerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

data class GetCustomerRequest(
    val customerId: UUID,
    val tenantId: UUID
)

@Service
class GetCustomerUseCase (
    private val customerRepository: CustomerRepository,
    private val jpaDocumentIssuerRepository: JpaDocumentIssuerRepository
){
    @Transactional(readOnly = true)
    fun execute(command: GetCustomerRequest): GetCustomerResponse {

        val customer = customerRepository.findById(command.customerId, command.tenantId) ?: throw CustomerNotFound()
        val issuerNameByIssuerId = jpaDocumentIssuerRepository.findAll().associate { it.id to it.name }

        val s = customer.snapshot()

        return GetCustomerResponse(
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
                    issuerNameByIssuerId[it.issuerId] ?: "OUTROS",
                    it.issuerState.value,
                    it.issuedAt,
                    it.expiresAt
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