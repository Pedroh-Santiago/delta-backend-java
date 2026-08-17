package br.com.deltaglobalbank.customers.domain.bankAccount;

import java.util.Arrays;
import java.util.Locale;

public enum BankAccountType {
    CHECKING,
    SAVINGS;

    public String toDatabaseValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static BankAccountType fromDatabaseValue(String value) {
        return Arrays.stream(values())
            .filter(type -> type.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Bank account type invalid"));
    }
}
