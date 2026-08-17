package br.com.deltaglobalbank.customers.domain.customer

import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditEntry
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccount
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
import java.time.Instant
import java.util.UUID

class Customer private constructor(
    val id: UUID,
    val tenantId: UUID,
    cpf: Cpf,
    fullName: FullName,
    birthDate: BirthDate,
    gender: Gender,
    val nationality: String,
    motherName: MotherName,
    maritalStatus: MaritalStatus,
    email: Email?,
    phoneNumber: Phone,
    address: Address,
    status: CustomerStatus,
    val createdAt: Instant,
    updatedAt: Instant,
    val createdBy: UUID,
    updatedBy: UUID,
    bankAccounts: List<BankAccount>,
    documents: List<PersonalDocument>
){
    private var _cpf: Cpf = cpf
    private var _fullName: FullName = fullName
    private var _birthDate: BirthDate = birthDate
    private var _gender: Gender = gender
    private var _motherName: MotherName = motherName
    private var _maritalStatus: MaritalStatus = maritalStatus
    private var _email: Email? = email
    private var _phoneNumber: Phone = phoneNumber
    private var _address: Address = address
    private var _status: CustomerStatus = status
    private var _updatedAt: Instant = updatedAt
    private var _updatedBy: UUID = updatedBy

    private val _bankAccounts = bankAccounts.toMutableList()
    private val _documents = documents.toMutableList()

    val bankAccounts: List<BankAccount> get() = _bankAccounts.toList()
    val documents: List<PersonalDocument> get() = _documents.toList()

    override fun equals(other: Any?) = other is Customer && other.id == id

    override fun hashCode() = id.hashCode()

    override fun toString() = "Customer(id=$id, status=$_status)"

    fun snapshot(): CustomerSnapshot = CustomerSnapshot(
        id = id,
        tenantId = tenantId,
        cpf = _cpf,
        fullName = _fullName,
        birthDate = _birthDate,
        gender = _gender,
        nationality = nationality,
        motherName = _motherName,
        maritalStatus = _maritalStatus,
        email = _email,
        phoneNumber = _phoneNumber,
        address = _address,
        status = _status,
        createdAt = createdAt,
        updatedAt = _updatedAt,
        createdBy = createdBy,
        updatedBy = _updatedBy,
        bankAccounts = bankAccounts,
        documents = documents,
    )

    companion object {
        fun create(
            id: UUID,
            tenantId: UUID,
            cpf: Cpf,
            fullName: FullName,
            birthDate: BirthDate,
            gender: Gender,
            nationality: String = "brasileira",
            motherName: MotherName,
            maritalStatus: MaritalStatus,
            email: Email?,
            phoneNumber: Phone,
            address: Address,
            createdBy: UUID,
            bankAccounts: List<BankAccount>,
            documents: List<PersonalDocument>
        ): Customer {
            val now = Instant.now()
            return Customer(
                id = id,
                tenantId = tenantId,
                cpf = cpf,
                fullName = fullName,
                birthDate = birthDate,
                nationality = nationality,
                gender = gender,
                motherName = motherName,
                maritalStatus = maritalStatus,
                email = email,
                phoneNumber = phoneNumber,
                address = address,
                status = CustomerStatus.ACTIVE,
                createdAt = now,
                updatedAt = now,
                createdBy = createdBy,
                updatedBy = createdBy,
                bankAccounts = bankAccounts,
                documents = documents,
            )
        }

        fun restore(
            id: UUID,
            tenantId: UUID,
            cpf: Cpf,
            fullName: FullName,
            birthDate: BirthDate,
            nationality: String,
            gender: Gender,
            motherName: MotherName,
            maritalStatus: MaritalStatus,
            email: Email?,
            phoneNumber: Phone,
            address: Address,
            status: CustomerStatus,
            createdAt: Instant,
            updatedAt: Instant,
            createdBy: UUID,
            updatedBy: UUID,
            bankAccounts: List<BankAccount>,
            documents: List<PersonalDocument>
        ): Customer = Customer(
            id = id,
            tenantId = tenantId,
            cpf = cpf,
            fullName = fullName,
            birthDate = birthDate,
            nationality = nationality,
            gender = gender,
            motherName = motherName,
            maritalStatus = maritalStatus,
            email = email,
            phoneNumber = phoneNumber,
            address = address,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
            createdBy = createdBy,
            updatedBy = updatedBy,
            bankAccounts = bankAccounts,
            documents = documents,
        )
    }

    private fun touch(updatedBy: UUID) {
        _updatedAt = Instant.now()
        _updatedBy = updatedBy
    }

    private fun describe(account: BankAccount): String =
        "${account.bankCode.value}/${account.agency.value}/${account.accountNumber}"

    private fun accountIdentity(account: BankAccount) =
        listOf(account.bankCode.value, account.agency.value, account.accountNumber, account.accountDigit)

    private fun documentIdentity(d: PersonalDocument) = listOf(d.documentType, d.documentNumber)

    fun updateContact(email: Email?, phoneNumber: Phone, updatedBy: UUID) {
        _email = email
        _phoneNumber = phoneNumber
        touch(updatedBy)
    }

    fun updateAddress(address: Address, updatedBy: UUID) {
        _address = address
        touch(updatedBy)
    }

    fun replaceDocuments(documents: List<PersonalDocument>, updatedBy: UUID) {
        if (documents.groupBy(::documentIdentity).any { it.value.size > 1 }) throw DuplicatePersonalDocument()
        _documents.clear()
        _documents.addAll(documents)
        touch(updatedBy)
    }

    fun updatePersonalInfo(
        cpf: Cpf, fullName: FullName, birthDate: BirthDate,
        gender: Gender, motherName: MotherName, maritalStatus: MaritalStatus,
        updatedBy: UUID,
    ): List<CustomerAuditEntry>{
        val audits =mutableListOf<CustomerAuditEntry>()
        if (cpf != _cpf) {
            audits += CustomerAuditEntry.cpfChanged(id, tenantId, _cpf.value, cpf.value, updatedBy)
            _cpf = cpf
        }

        if (fullName != _fullName) {
            audits += CustomerAuditEntry.fullNameChanged(id, tenantId, _fullName.value, fullName.value, updatedBy)
            _fullName = fullName
        }

        _birthDate = birthDate
        _gender = gender
        _motherName = motherName
        _maritalStatus = maritalStatus

        touch(updatedBy)
        return audits
    }

    fun addBankAccount(account: BankAccount, updatedBy: UUID): CustomerAuditEntry{
        if (account.isPrimary && _bankAccounts.any { it.purpose == account.purpose && it.isPrimary })
            throw DuplicatePrimaryAccountForPurpose()

        if (_bankAccounts.any { accountIdentity(it) == accountIdentity(account) }) throw DuplicateBankAccount()

        _bankAccounts.add(account)
        touch(updatedBy)

        return CustomerAuditEntry.bankAccountAdded(id, tenantId, describe(account), updatedBy)
    }

    fun removeBankAccount(accountId: UUID, updatedBy: UUID): CustomerAuditEntry{
        val account = _bankAccounts.firstOrNull { it.id == accountId }
            ?: throw BankAccountNotFound()

        _bankAccounts.remove(account)
        touch(updatedBy)

        return CustomerAuditEntry.bankAccountRemoved(id, tenantId, describe(account), updatedBy)
    }

    fun replaceBankAccounts(newAccounts: List<BankAccount>, updatedBy: UUID): List<CustomerAuditEntry>{
        newAccounts.groupBy { it.purpose }.forEach { (_, group) ->
            if (group.count { it.isPrimary } > 1) throw DuplicatePrimaryAccountForPurpose()
        }

        if (newAccounts.groupBy(::accountIdentity).any {it.value.size > 1}) throw DuplicateBankAccount()

        val newIds = newAccounts.map { it.id }.toSet()
        val oldIds = _bankAccounts.map { it.id }.toSet()

        val removed = _bankAccounts.filter { it.id !in newIds }
        val added = newAccounts.filter { it.id !in oldIds }

        val audits = removed.map { CustomerAuditEntry.bankAccountRemoved(id, tenantId, describe(it), updatedBy) } +
                    added.map { CustomerAuditEntry.bankAccountAdded(id, tenantId, describe(it), updatedBy) }

        _bankAccounts.clear()
        _bankAccounts.addAll(newAccounts)

        if (audits.isNotEmpty()) touch(updatedBy)
        return audits
    }

    fun inactivate(updatedBy: UUID): CustomerAuditEntry{
        val oldValue = _status
        _status = CustomerStatus.INACTIVE
        touch(updatedBy)
        return CustomerAuditEntry.statusChanged(
            id,
            tenantId,
            oldValue.toDatabaseValue(),
            _status.toDatabaseValue(),
            updatedBy
        )
    }

    fun reactivate(updatedBy: UUID): CustomerAuditEntry{
        val oldValue = _status
        _status = CustomerStatus.ACTIVE
        touch(updatedBy)
        return CustomerAuditEntry.statusChanged(
            id,
            tenantId,
            oldValue.toDatabaseValue(),
            _status.toDatabaseValue(),
            updatedBy
        )
    }

    fun markAsDeleted(by: UUID): CustomerAuditEntry {
        touch(by)
        return CustomerAuditEntry.customerDeleted(id, tenantId, by)
    }

}

data class CustomerSnapshot(
    val id: UUID,
    val tenantId: UUID,
    val cpf: Cpf,
    val fullName: FullName,
    val birthDate: BirthDate,
    val gender: Gender,
    val nationality: String,
    val motherName: MotherName,
    val maritalStatus: MaritalStatus,
    val email: Email?,
    val phoneNumber: Phone,
    val address: Address,
    val status: CustomerStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
    val createdBy: UUID,
    val updatedBy: UUID,
    val bankAccounts: List<BankAccount>,
    val documents: List<PersonalDocument>
)