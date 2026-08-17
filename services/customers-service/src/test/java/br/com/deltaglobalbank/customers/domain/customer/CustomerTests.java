package br.com.deltaglobalbank.customers.domain.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditAction;
import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditEntry;
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccount;
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccountPurpose;
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccountType;
import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.Agency;
import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.BankCode;
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
import org.junit.jupiter.api.Test;

class CustomerTests {

    private final UUID tenantId = UUID.randomUUID();
    private final UUID actor = UUID.randomUUID();
    private final List<BankAccount> bankAccounts = List.of();
    private final List<PersonalDocument> documents = List.of();

    private Customer aCustomer(String cpf) {
        return Customer.create(
            UUID.randomUUID(), tenantId,
            new Cpf(cpf), new FullName("Maria Silva"),
            new BirthDate(LocalDate.of(1990, 1, 1)), Gender.FEMALE,
            new MotherName("Ana Silva"), MaritalStatus.SINGLE,
            new Email("maria@delta.com"), new Phone("+5511999998888"),
            new Address("01310100", "Av Paulista", "SP", new Uf("SP")),
            actor, bankAccounts, documents
        );
    }

    private Customer aCustomer() {
        return aCustomer("11144477735");
    }

    private BankAccount anAccount(boolean primary, BankAccountPurpose purpose) {
        return BankAccount.create(
            UUID.randomUUID(), UUID.randomUUID(),
            new BankCode("237"), new Agency("1234"),
            "56789", "0",
            BankAccountType.CHECKING, purpose, primary
        );
    }

    @Test
    void createStartsActive() {
        assertEquals(CustomerStatus.ACTIVE, aCustomer().snapshot().status());
    }

    @Test
    void changingCpfEmitsACpfChangedAudit() {
        Customer customer = aCustomer();
        List<CustomerAuditEntry> audits = customer.updatePersonalInfo(
            new Cpf("52998224725"), new FullName("Maria Silva"),
            new BirthDate(LocalDate.of(1990, 1, 1)), Gender.FEMALE,
            new MotherName("Ana Silva"), MaritalStatus.SINGLE,
            actor
        );
        assertEquals(1, audits.size());
        assertEquals(CustomerAuditAction.CPF_CHANGED, audits.get(0).action());
    }

    @Test
    void addingASecondPrimaryAccountForSamePurposeIsRejected() {
        Customer customer = aCustomer();
        customer.addBankAccount(anAccount(true, BankAccountPurpose.DISBURSEMENT), actor);
        assertThrows(DuplicatePrimaryAccountForPurpose.class,
            () -> customer.addBankAccount(anAccount(true, BankAccountPurpose.DISBURSEMENT), actor));
    }

    @Test
    void changingFullNameEmitsAFullNameChangedAudit() {
        Customer customer = aCustomer();
        List<CustomerAuditEntry> audits = customer.updatePersonalInfo(
            new Cpf("11144477735"), new FullName("Maria Souza"),
            new BirthDate(LocalDate.of(1990, 1, 1)), Gender.FEMALE,
            new MotherName("Ana Silva"), MaritalStatus.SINGLE,
            actor
        );
        assertEquals(1, audits.size());
        assertEquals(CustomerAuditAction.FULL_NAME_CHANGED, audits.get(0).action());
    }

    @Test
    void updatingPersonalInfoWithoutChangesEmitsNoAudit() {
        Customer customer = aCustomer();
        List<CustomerAuditEntry> audits = customer.updatePersonalInfo(
            new Cpf("11144477735"), new FullName("Maria Silva"),
            new BirthDate(LocalDate.of(1990, 1, 1)), Gender.FEMALE,
            new MotherName("Ana Silva"), MaritalStatus.SINGLE,
            actor
        );
        assertTrue(audits.isEmpty());
    }

    @Test
    void addingABankAccountEmitsABankAccountAddedAudit() {
        Customer customer = aCustomer();
        CustomerAuditEntry audit = customer.addBankAccount(anAccount(true, BankAccountPurpose.DISBURSEMENT), actor);
        assertEquals(CustomerAuditAction.BANK_ACCOUNT_ADDED, audit.action());
        assertEquals(1, customer.bankAccounts().size());
    }

    @Test
    void removingAnUnknownBankAccountIsRejected() {
        Customer customer = aCustomer();
        assertThrows(BankAccountNotFound.class, () -> customer.removeBankAccount(UUID.randomUUID(), actor));
    }

    @Test
    void inactivateSetsStatusToInactiveAndEmitsStatusChanged() {
        Customer customer = aCustomer();
        CustomerAuditEntry audit = customer.inactivate(actor);
        assertEquals(CustomerStatus.INACTIVE, customer.snapshot().status());
        assertEquals(CustomerAuditAction.STATUS_CHANGED, audit.action());
    }

    @Test
    void reactivateSetsStatusBackToActive() {
        Customer customer = aCustomer();
        customer.inactivate(actor);
        CustomerAuditEntry audit = customer.reactivate(actor);
        assertEquals(CustomerStatus.ACTIVE, customer.snapshot().status());
        assertEquals(CustomerAuditAction.STATUS_CHANGED, audit.action());
    }

    @Test
    void exposedBankAccountsListIsAReadOnlyCopy() {
        Customer customer = aCustomer();
        List<BankAccount> before = customer.bankAccounts();
        customer.addBankAccount(anAccount(false, BankAccountPurpose.PAYOFF), actor);
        assertEquals(0, before.size());
        assertEquals(1, customer.bankAccounts().size());
    }
}
