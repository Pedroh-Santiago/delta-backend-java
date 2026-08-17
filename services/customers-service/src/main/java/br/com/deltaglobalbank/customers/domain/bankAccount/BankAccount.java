package br.com.deltaglobalbank.customers.domain.bankAccount;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.Agency;
import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.BankCode;

public class BankAccount {

    private final UUID id;
    private final UUID customerId;
    private final BankCode bankCode;
    private final Agency agency;
    private final String accountNumber;
    private final String accountDigit;
    private final BankAccountType accountType;
    private final BankAccountPurpose purpose;
    private final boolean isPrimary;
    private final Instant createdAt;
    private final Instant updatedAt;

    private BankAccount(
        UUID id,
        UUID customerId,
        BankCode bankCode,
        Agency agency,
        String accountNumber,
        String accountDigit,
        BankAccountType accountType,
        BankAccountPurpose purpose,
        boolean isPrimary,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "bank_account_id_required");
        this.customerId = Objects.requireNonNull(customerId, "bank_account_customer_id_required");
        this.bankCode = Objects.requireNonNull(bankCode, "bank_account_bank_code_required");
        this.agency = Objects.requireNonNull(agency, "bank_account_agency_required");
        this.accountNumber = Objects.requireNonNull(accountNumber, "bank_account_number_required");
        this.accountDigit = accountDigit;
        this.accountType = Objects.requireNonNull(accountType, "bank_account_type_required");
        this.purpose = Objects.requireNonNull(purpose, "bank_account_purpose_required");
        this.isPrimary = isPrimary;
        this.createdAt = Objects.requireNonNull(createdAt, "bank_account_created_at_required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "bank_account_updated_at_required");
    }

    public static BankAccount create(
        UUID id,
        UUID customerId,
        BankCode bankCode,
        Agency agency,
        String accountNumber,
        String accountDigit,
        BankAccountType accountType,
        BankAccountPurpose purpose,
        boolean isPrimary
    ) {
        Instant now = Instant.now();
        return new BankAccount(
            id,
            customerId,
            bankCode,
            agency,
            accountNumber,
            accountDigit,
            accountType,
            purpose,
            isPrimary,
            now,
            now
        );
    }

    public static BankAccount restore(
        UUID id,
        UUID customerId,
        BankCode bankCode,
        Agency agency,
        String accountNumber,
        String accountDigit,
        BankAccountType accountType,
        BankAccountPurpose purpose,
        boolean isPrimary,
        Instant createdAt,
        Instant updatedAt
    ) {
        return new BankAccount(
            id,
            customerId,
            bankCode,
            agency,
            accountNumber,
            accountDigit,
            accountType,
            purpose,
            isPrimary,
            createdAt,
            updatedAt
        );
    }

    public UUID id() {
        return id;
    }

    public UUID customerId() {
        return customerId;
    }

    public BankCode bankCode() {
        return bankCode;
    }

    public Agency agency() {
        return agency;
    }

    public String accountNumber() {
        return accountNumber;
    }

    public String accountDigit() {
        return accountDigit;
    }

    public BankAccountType accountType() {
        return accountType;
    }

    public BankAccountPurpose purpose() {
        return purpose;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public String describe() {
        return bankCode.value() + "/" + agency.value() + "/" + accountNumber;
    }

    public List<Object> identity() {
        return Arrays.asList(bankCode.value(), agency.value(), accountNumber, accountDigit);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BankAccount account && account.id.equals(id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
