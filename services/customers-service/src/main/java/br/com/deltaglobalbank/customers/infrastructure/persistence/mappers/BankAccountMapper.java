package br.com.deltaglobalbank.customers.infrastructure.persistence.mappers;

import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccount;
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccountPurpose;
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccountType;
import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.Agency;
import br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects.BankCode;
import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.BankAccountEntity;

public final class BankAccountMapper {

    private BankAccountMapper() {
    }

    public static BankAccount toDomain(BankAccountEntity entity) {
        return BankAccount.restore(
            entity.getId(),
            entity.getCustomerId(),
            new BankCode(entity.getBankCode()),
            new Agency(entity.getAgency()),
            entity.getAccountNumber(),
            entity.getAccountDigit(),
            BankAccountType.fromDatabaseValue(entity.getAccountType()),
            BankAccountPurpose.fromDatabaseValue(entity.getPurpose()),
            entity.isPrimary(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public static BankAccountEntity toEntity(BankAccount account) {
        return new BankAccountEntity(
            account.id(),
            account.customerId(),
            account.bankCode().value(),
            account.agency().value(),
            account.accountNumber(),
            account.accountDigit(),
            account.accountType().toDatabaseValue(),
            account.purpose().toDatabaseValue(),
            account.isPrimary(),
            account.createdAt(),
            account.updatedAt()
        );
    }
}
