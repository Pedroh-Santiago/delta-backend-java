package br.com.deltaglobalbank.customers.domain.customer

import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditAction
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccount
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccountPurpose
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccountType
import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.Agency
import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.BankCode
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import java.time.LocalDate
import java.util.UUID
import org.junit.jupiter.api.Test

class CustomerTests {

    private val tenantId = UUID.randomUUID()
    private val actor = UUID.randomUUID()
    private val bankAccounts: List<BankAccount> = emptyList()
    private val documents: List<PersonalDocument> = emptyList()

    private fun aCustomer(cpf: String = "11144477735") = Customer.create(
        id = UUID.randomUUID(), tenantId = tenantId,
        cpf = Cpf(cpf), fullName = FullName("Maria Silva"),
        birthDate = BirthDate(LocalDate.of(1990, 1, 1)), gender = Gender.FEMALE,
        motherName = MotherName("Ana Silva"), maritalStatus = MaritalStatus.SINGLE,
        email = Email("maria@delta.com"), phoneNumber = Phone("+5511999998888"),
        address = Address(cep = "01310100", street = "Av Paulista", city = "SP", state = Uf("SP")),
        createdBy = actor, bankAccounts = bankAccounts, documents = documents
    )

    private fun anAccount(primary: Boolean, purpose: BankAccountPurpose) = BankAccount.create(
        id = UUID.randomUUID(), customerId = UUID.randomUUID(),
        bankCode = BankCode("237"), agency = Agency("1234"),
        accountNumber = "56789", accountDigit = "0",
        accountType = BankAccountType.CHECKING, purpose = purpose, isPrimary = primary,
    )

    @Test
    fun `create starts active`() {
        assertEquals(CustomerStatus.ACTIVE, aCustomer().snapshot().status)
    }

    @Test
    fun `changing cpf emits a CPF_CHANGED audit`() {
        val customer = aCustomer()
        val audits = customer.updatePersonalInfo(
            cpf = Cpf("52998224725"), fullName = FullName("Maria Silva"),
            birthDate = BirthDate(LocalDate.of(1990, 1, 1)), gender = Gender.FEMALE,
            motherName = MotherName("Ana Silva"), maritalStatus = MaritalStatus.SINGLE,
            updatedBy = actor,
        )
        assertEquals(1, audits.size)
        assertEquals(CustomerAuditAction.CPF_CHANGED, audits.first().action)
    }

    @Test
    fun `adding a second primary account for same purpose is rejected`() {
        val customer = aCustomer()
        customer.addBankAccount(anAccount(primary = true, purpose = BankAccountPurpose.DISBURSEMENT), actor)
        assertThrows(DuplicatePrimaryAccountForPurpose::class.java) {
            customer.addBankAccount(anAccount(primary = true, purpose = BankAccountPurpose.DISBURSEMENT), actor)
        }
    }

    @Test
    fun `changing full name emits a FULL_NAME_CHANGED audit`() {
        val customer = aCustomer()
        val audits = customer.updatePersonalInfo(
            cpf = Cpf("11144477735"), fullName = FullName("Maria Souza"),   // só o nome muda
            birthDate = BirthDate(LocalDate.of(1990, 1, 1)), gender = Gender.FEMALE,
            motherName = MotherName("Ana Silva"), maritalStatus = MaritalStatus.SINGLE,
            updatedBy = actor,
        )
        assertEquals(1, audits.size)
        assertEquals(CustomerAuditAction.FULL_NAME_CHANGED, audits.first().action)
    }

    @Test
    fun `updating personal info without changes emits no audit`() {
        val customer = aCustomer()
        val audits = customer.updatePersonalInfo(
            cpf = Cpf("11144477735"), fullName = FullName("Maria Silva"),   // valores idênticos ao fixture
            birthDate = BirthDate(LocalDate.of(1990, 1, 1)), gender = Gender.FEMALE,
            motherName = MotherName("Ana Silva"), maritalStatus = MaritalStatus.SINGLE,
            updatedBy = actor,
        )
        assertTrue(audits.isEmpty())
    }

    @Test
    fun `adding a bank account emits a BANK_ACCOUNT_ADDED audit`() {
        val customer = aCustomer()
        val audit = customer.addBankAccount(anAccount(primary = true, purpose = BankAccountPurpose.DISBURSEMENT), actor)
        assertEquals(CustomerAuditAction.BANK_ACCOUNT_ADDED, audit.action)
        assertEquals(1, customer.bankAccounts.size)
    }

    @Test
    fun `removing an unknown bank account is rejected`() {
        val customer = aCustomer()
        assertThrows(BankAccountNotFound::class.java) {
            customer.removeBankAccount(UUID.randomUUID(), actor)
        }
    }

    @Test
    fun `inactivate sets status to inactive and emits STATUS_CHANGED`() {
        val customer = aCustomer()
        val audit = customer.inactivate(actor)
        assertEquals(CustomerStatus.INACTIVE, customer.snapshot().status)
        assertEquals(CustomerAuditAction.STATUS_CHANGED, audit.action)
    }

    @Test
    fun `reactivate sets status back to active`() {
        val customer = aCustomer()
        customer.inactivate(actor)
        val audit = customer.reactivate(actor)
        assertEquals(CustomerStatus.ACTIVE, customer.snapshot().status)
        assertEquals(CustomerAuditAction.STATUS_CHANGED, audit.action)
    }

    @Test
    fun `exposed bank accounts list is a read-only copy`() {
        val customer = aCustomer()
        val before = customer.bankAccounts
        customer.addBankAccount(anAccount(primary = false, purpose = BankAccountPurpose.PAYOFF), actor)
        assertEquals(0, before.size)
        assertEquals(1, customer.bankAccounts.size)
    }
}