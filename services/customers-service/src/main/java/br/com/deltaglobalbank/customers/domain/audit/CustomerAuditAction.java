package br.com.deltaglobalbank.customers.domain.audit;

import java.util.Arrays;
import java.util.Locale;

public enum CustomerAuditAction {
    BANK_ACCOUNT_ADDED,
    BANK_ACCOUNT_REMOVED,
    CPF_CHANGED,
    CUSTOMER_DELETED,
    FULL_NAME_CHANGED,
    STATUS_CHANGED;

    public String toDatabaseValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static CustomerAuditAction fromDatabaseValue(String value) {
        return Arrays.stream(values())
            .filter(action -> action.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("invalid customer audit action '" + value + "'"));
    }
}
