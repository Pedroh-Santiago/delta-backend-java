package br.com.deltaglobalbank.customers.domain.bankAccount;

import java.util.Arrays;
import java.util.Locale;

public enum BankAccountPurpose {
    DISBURSEMENT,
    PAYOFF;

    public String toDatabaseValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static BankAccountPurpose fromDatabaseValue(String value) {
        return Arrays.stream(values())
            .filter(purpose -> purpose.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("bank account purpose invalid: " + value));
    }
}
